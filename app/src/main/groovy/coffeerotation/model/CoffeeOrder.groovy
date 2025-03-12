package coffeerotation.model

import java.text.SimpleDateFormat

/**
 * Represents a group coffee order for a specific day.
 * Tracks which coworker paid for the order and the total cost.
 */
class CoffeeOrder {
    /** Unique identifier for the order */
    String id

    /** Date when the order was placed */
    Date orderDate

    /** List of coworkers who ordered coffee */
    List<Coworker> participants = []

    /** The coworker who paid for this order */
    Coworker paidBy

    /** Total cost of the order */
    BigDecimal totalCost

    /** Map to store custom beverages for this specific order */
    Map<String, String> customBeverages = [:]

    /** Map to store custom prices for this specific order */
    Map<String, BigDecimal> customPrices = [:]

    /**
     * Constructor with required fields
     */
    CoffeeOrder(Date orderDate, List<Coworker> participants, Coworker paidBy) {
        this.id = UUID.randomUUID().toString()
        this.orderDate = orderDate
        this.participants = participants
        this.paidBy = paidBy
        calculateTotalCost()
    }

    /**
     * Constructor with custom beverage information
     */
    CoffeeOrder(Date orderDate, List<Coworker> participants, Coworker paidBy,
                Map<String, String> customBeverages, Map<String, BigDecimal> customPrices) {
        this.id = UUID.randomUUID().toString()
        this.orderDate = orderDate
        this.participants = participants
        this.paidBy = paidBy
        this.customBeverages = customBeverages
        this.customPrices = customPrices
        calculateTotalCost()
    }

    /**
     * Calculate the total cost based on all participants' beverage prices
     */
    private void calculateTotalCost() {
        this.totalCost = participants.sum { coworker ->
            customPrices.containsKey(coworker.id) ?
                    customPrices[coworker.id] :
                    coworker.beveragePrice
        } ?: 0.0
    }

    /**
     * Record this payment in the payer's history
     */
    void recordPayment() {
        paidBy.recordPayment(totalCost, orderDate)
    }

    /**
     * Get a map of each participant and their beverage cost
     * @return Map with coworker name as key and price as value
     */
    Map<String, BigDecimal> getItemizedCosts() {
        return participants.collectEntries { coworker ->
            def price = customPrices.containsKey(coworker.id) ?
                    customPrices[coworker.id] :
                    coworker.beveragePrice

            [(coworker.name): price]
        }
    }

    /**
     * Get a description of each participant's beverage
     * @return Map with coworker name as key and beverage description as value
     */
    Map<String, String> getItemizedBeverages() {
        return participants.collectEntries { coworker ->
            def beverage = customBeverages.containsKey(coworker.id) ?
                    customBeverages[coworker.id] :
                    coworker.preferredBeverage

            [(coworker.name): beverage]
        }
    }

    @Override
    String toString() {
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd")
        return "Coffee order on ${dateFormat.format(orderDate)} - Paid by: ${paidBy.name}, Total: \$${totalCost}"
    }
}