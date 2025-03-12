package coffeerotation.service

import coffeerotation.model.Coworker
import coffeerotation.model.CoffeeOrder
import coffeerotation.service.PaymentCalculator

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Service responsible for managing the coffee rotation system.
 * Handles loading/saving data and provides the main interface for the application.
 */
class RotationManager {
    /** File path for storing coworkers data */
    private static final String COWORKERS_FILE = "coworkers.json"

    /** File path for storing order history */
    private static final String ORDERS_FILE = "orders.json"

    /** The list of coworkers participating in the rotation */
    private List<Coworker> coworkers = []

    /** Calculator for payment logic */
    private PaymentCalculator calculator

    /** Default constructor */
    RotationManager() {
        calculator = new PaymentCalculator([]) // Initialize with empty list first
        loadData() // This will populate coworkers and then load orders into calculator
        calculator = new PaymentCalculator(coworkers) // Update with loaded coworkers
        calculator = new PaymentCalculator(coworkers) // Update with loaded coworkers
    }

    /**
     * Add a new coworker to the rotation
     * @param name The coworker's name
     * @param beverage Their preferred coffee beverage
     * @param price The price of their beverage
     * @return The newly created Coworker
     */
    Coworker addCoworker(String name, String beverage, BigDecimal price) {
        def coworker = new Coworker(name, beverage, price)
        coworkers.add(coworker)
        saveData()
        return coworker
    }

    /**
     * Remove a coworker from the rotation
     * @param coworkerId The ID of the coworker to remove
     * @return true if removed successfully, false otherwise
     */
    boolean removeCoworker(String coworkerId) {
        def coworkerToRemove = coworkers.find { it.id == coworkerId }
        if (!coworkerToRemove) {
            return false
        }

        // Check if this coworker paid for any orders
        boolean paidForOrders = calculator.getOrderHistory().any { it.paidBy.id == coworkerId }
        if (paidForOrders) {
            // Disallow removal if they paid for orders (would break the payment history)
            return false
        }

        // Remove the coworker
        coworkers.removeAll { it.id == coworkerId }
        saveData()

        // Refresh the calculator with updated coworker list
        calculator = new PaymentCalculator(coworkers)

        return true
    }

    /**
     * Get a specific coworker by ID
     * @param coworkerId The ID of the coworker to find
     * @return The coworker or null if not found
     */
    Coworker getCoworkerById(String coworkerId) {
        return coworkers.find { it.id == coworkerId }
    }

    /**
     * Get a specific coworker by index (1-based for user-friendliness)
     * @param index The 1-based index of the coworker
     * @return The coworker or null if index is invalid
     */
    Coworker getCoworkerByIndex(int index) {
        int arrayIndex = index - 1
        if (arrayIndex < 0 || arrayIndex >= coworkers.size()) {
            return null
        }
        return coworkers[arrayIndex]
    }

    /**
     * Get all coworkers in the rotation
     * @return List of all coworkers
     */
    List<Coworker> getAllCoworkers() {
        return coworkers
    }

    /**
     * Create a new coffee order for today
     * @param participantIds List of coworker IDs who participated in this order
     * @param payerId ID of the coworker who paid
     * @return The newly created order or null if invalid parameters
     */
    CoffeeOrder createOrder(List<String> participantIds, String payerId) {
        def participants = coworkers.findAll { participantIds.contains(it.id) }
        def payer = coworkers.find { it.id == payerId }

        if (participants.isEmpty() || !payer) {
            return null
        }

        def order = new CoffeeOrder(new Date(), participants, payer)
        calculator.recordOrder(order)
        saveData()
        return order
    }

    /**
     * Create a new custom coffee order for today where coworkers might order different drinks
     * @param participants List of coworkers who participated
     * @param payer The coworker who paid
     * @param customBeverages Map of coworker ID to custom beverage name (optional)
     * @param customPrices Map of coworker ID to custom price (optional)
     * @return The newly created order
     */
    CoffeeOrder createCustomOrder(
            List<Coworker> participants,
            Coworker payer,
            Map<String, String> customBeverages = [:],
            Map<String, BigDecimal> customPrices = [:]
    ) {
        if (participants.isEmpty() || !payer) {
            return null
        }

        // Create temporary coworkers with custom beverages and prices for this order
        def orderParticipants = participants.collect { coworker ->
            if (customBeverages.containsKey(coworker.id) || customPrices.containsKey(coworker.id)) {
                // Create a temporary coworker with custom properties just for this order
                def beverage = customBeverages.containsKey(coworker.id) ?
                        customBeverages[coworker.id] : coworker.preferredBeverage

                def price = customPrices.containsKey(coworker.id) ?
                        customPrices[coworker.id] : coworker.beveragePrice

                def tempCoworker = new Coworker(coworker.name, beverage, price)
                tempCoworker.id = coworker.id
                return tempCoworker
            } else {
                return coworker
            }
        }

        def order = new CoffeeOrder(new Date(), orderParticipants, payer)
        calculator.recordOrder(order)
        saveData()
        return order
    }

    /**
     * Suggest who should pay for coffee next
     * @return The coworker who should pay next based on fairness algorithm
     */
    Coworker suggestNextPayer() {
        return calculator.suggestNextPayer()
    }

    /**
     * Get payment statistics and balance information
     * @return Map containing payment statistics
     */
    Map getPaymentSummary() {
        return [
                statistics: calculator.getPaymentStatistics(),
                balances: calculator.getPaymentBalances(),
                fairShare: calculator.getFairShareAmount(),
                suggestedPayer: calculator.suggestNextPayer()?.name
        ]
    }

    /**
     * Load coworkers and orders data from persistent storage
     */
    private void loadData() {
        // Load coworkers
        def coworkersFile = new File(COWORKERS_FILE)
        if (coworkersFile.exists()) {
            try {
                def slurper = new JsonSlurper()
                def coworkersData = slurper.parse(coworkersFile)

                coworkers = coworkersData.collect { data ->
                    def coworker = new Coworker(
                            data.name,
                            data.preferredBeverage,
                            new BigDecimal(data.beveragePrice.toString())
                    )
                    coworker.id = data.id
                    coworker.totalPaid = new BigDecimal(data.totalPaid.toString())
                    coworker.paymentCount = data.paymentCount
                    coworker.paymentDates = data.paymentDates.collect { new Date(it) }
                    return coworker
                }
            } catch (Exception e) {
                println "Error loading coworkers: ${e.message}"
                coworkers = []
            }
        }

        // Load orders
        def ordersFile = new File(ORDERS_FILE)
        List<CoffeeOrder> orders = []

        if (ordersFile.exists()) {
            try {
                def slurper = new JsonSlurper()
                def ordersData = slurper.parse(ordersFile)

                orders = ordersData.collect { data ->
                    // Find the payer and participants by their IDs
                    def payer = coworkers.find { it.id == data.payerId }
                    def participants = coworkers.findAll { data.participantIds.contains(it.id) }

                    // Skip if we can't find the referenced coworkers
                    if (!payer || participants.isEmpty()) return null

                    // Create the order
                    def order = new CoffeeOrder(
                            new Date(data.orderDate),
                            participants,
                            payer
                    )
                    order.id = data.id
                    return order
                }.findAll { it != null } // Remove any null entries

                // Add orders to the calculator
                orders.each { calculator.recordOrder(it) }
            } catch (Exception e) {
                println "Error loading orders: ${e.message}"
            }
        }
    }

    /**
     * Save coworkers and orders data to persistent storage
     */
    private void saveData() {
        // Save coworkers
        def coworkersFile = new File(COWORKERS_FILE)
        try {
            def coworkersData = coworkers.collect { coworker ->
                [
                        id: coworker.id,
                        name: coworker.name,
                        preferredBeverage: coworker.preferredBeverage,
                        beveragePrice: coworker.beveragePrice,
                        totalPaid: coworker.totalPaid,
                        paymentCount: coworker.paymentCount,
                        paymentDates: coworker.paymentDates.collect { it.time }
                ]
            }

            def builder = new JsonBuilder(coworkersData)
            coworkersFile.text = builder.toPrettyString()
        } catch (Exception e) {
            println "Error saving coworkers: ${e.message}"
        }

        // Save orders
        def ordersFile = new File(ORDERS_FILE)
        try {
            def ordersData = calculator.getOrderHistory().collect { order ->
                [
                        id: order.id,
                        orderDate: order.orderDate.time,
                        payerId: order.paidBy.id,
                        totalCost: order.totalCost,
                        participantIds: order.participants.collect { it.id }
                ]
            }

            def builder = new JsonBuilder(ordersData)
            ordersFile.text = builder.toPrettyString()
        } catch (Exception e) {
            println "Error saving orders: ${e.message}"
        }
    }
}