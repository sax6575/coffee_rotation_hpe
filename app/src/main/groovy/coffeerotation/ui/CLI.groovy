package coffeerotation.ui

import coffeerotation.model.Coworker
import coffeerotation.service.RotationManager

/**
 * Command Line Interface for the Coffee Rotation application.
 * Handles user interaction through the console.
 */
class CLI {
    private RotationManager rotationManager
    private Scanner scanner

    CLI() {
        rotationManager = new RotationManager()
        scanner = new Scanner(System.in)
    }

    /**
     * Start the CLI application
     */
    void start() {
        println "==============================================="
        println "         Welcome to Coffee Rotation           "
        println "==============================================="
        println ""

        boolean running = true
        while (running) {
            displayMenu()
            int choice = readIntInput("Enter your choice", 0, 6)

            switch (choice) {
                case 1:
                    addCoworker()
                    break
                case 2:
                    listCoworkers()
                    break
                case 3:
                    createOrder()
                    break
                case 4:
                    whoShouldPayNext()
                    break
                case 5:
                    viewPaymentSummary()
                    break
                case 6:
                    removeCoworker()
                    break
                case 0:
                    running = false
                    println "Thanks for using Coffee Rotation!"
                    break
                default:
                    println "Invalid choice. Please try again."
            }

            println ""
        }
    }

    /**
     * Display the main menu options
     */
    private void displayMenu() {
        println "Main Menu:"
        println "1. Add a Coworker"
        println "2. List Coworkers"
        println "3. Create Coffee Order"
        println "4. Who Should Pay Next?"
        println "5. View Payment Summary"
        println "6. Remove a Coworker"
        println "0. Exit"
    }

    /**
     * Add a new coworker to the system
     */
    private void addCoworker() {
        println "\n=== Add a Coworker ==="

        String name = readStringInput("Enter coworker name")
        String beverage = readStringInput("Enter preferred coffee drink")
        BigDecimal price = readDecimalInput("Enter beverage price")

        Coworker coworker = rotationManager.addCoworker(name, beverage, price)
        println "\nCoworker added successfully!"
        println coworker.toString()
    }

    /**
     * List all coworkers in the system
     */
    private void listCoworkers() {
        println "\n=== Coworkers ==="

        def coworkers = rotationManager.getAllCoworkers()
        if (coworkers.isEmpty()) {
            println "No coworkers found. Add some first!"
            return
        }

        coworkers.eachWithIndex { coworker, index ->
            println "${index + 1}. ${coworker}"
        }
    }

    /**
     * Create a new coffee order for today
     */
    private void createOrder() {
        println "\n=== Create New Coffee Order ==="

        def coworkers = rotationManager.getAllCoworkers()
        if (coworkers.isEmpty()) {
            println "No coworkers found. Add some first!"
            return
        }

        // Display all coworkers
        coworkers.eachWithIndex { coworker, index ->
            println "${index + 1}. ${coworker}"
        }

        // Select participants
        println "\nSelect participants (comma-separated numbers, e.g., 1,2,3):"
        String participantsInput = scanner.nextLine().trim()
        List<Integer> participantIndices = participantsInput.split(",")
                .collect { it.trim() }
                .findAll { it.isInteger() }
                .collect { Integer.parseInt(it) - 1 }

        List<Coworker> participants = participantIndices
                .findAll { it >= 0 && it < coworkers.size() }
                .collect { coworkers[it] }

        if (participants.isEmpty()) {
            println "No valid participants selected!"
            return
        }

        // Ask if everyone is ordering their usual drink
        println "\nIs everyone ordering their usual drink? (y/n)"
        String usualDrinks = scanner.nextLine().trim().toLowerCase()

        Map<String, String> customBeverages = [:]
        Map<String, BigDecimal> customPrices = [:]

        if (usualDrinks != 'y' && usualDrinks != 'yes') {
            // Collect custom orders for this specific order
            participants.each { coworker ->
                println "\nFor ${coworker.name}:"
                println "  Default: ${coworker.preferredBeverage} (\$${coworker.beveragePrice})"
                println "  Order something different? (y/n)"

                String different = scanner.nextLine().trim().toLowerCase()
                if (different == 'y' || different == 'yes') {
                    String beverage = readStringInput("Enter beverage")
                    BigDecimal price = readDecimalInput("Enter price")

                    customBeverages[coworker.id] = beverage
                    customPrices[coworker.id] = price
                }
            }
        }

        // Select who pays
        println "\nWho paid for this order? (enter number):"
        int payerIndex = readIntInput("Enter payer number", 1, coworkers.size()) - 1
        Coworker payer = coworkers[payerIndex]

        // Create the order
        def order = rotationManager.createCustomOrder(
                participants,
                payer,
                customBeverages,
                customPrices
        )

        if (order) {
            println "\nOrder created successfully!"
            println order.toString()
            println "Total: \$${order.totalCost}"
            println "\nItemized:"

            order.getItemizedCosts().each { name, cost ->
                println "  $name: \$${cost}"
            }
        } else {
            println "Failed to create order. Please try again."
        }
    }

    /**
     * Show who should pay next based on the fairness algorithm
     */
    private void whoShouldPayNext() {
        println "\n=== Who Should Pay Next? ==="

        Coworker nextPayer = rotationManager.suggestNextPayer()
        if (nextPayer) {
            println "${nextPayer.name} should pay next!"
            println "(Prefers ${nextPayer.preferredBeverage} at \$${nextPayer.beveragePrice})"
        } else {
            println "No coworkers found. Add some first!"
        }
    }

    /**
     * Display a summary of payment statistics and balances
     */
    private void viewPaymentSummary() {
        println "\n=== Payment Summary ==="

        def summary = rotationManager.getPaymentSummary()
        def stats = summary.statistics

        println "Total spent so far: \$${stats.totalSpent}"
        println "Average order cost: \$${stats.averageOrderCost}"
        println "Number of orders: ${stats.orderCount}"
        println "Fair share per person: \$${summary.fairShare}"

        if (stats.orderCount > 0) {
            println "\nMost expensive drink: ${stats.mostExpensiveDrink.beverage} (\$${stats.mostExpensiveDrink.price}) by ${stats.mostExpensiveDrink.coworker}"
            println "Least expensive drink: ${stats.leastExpensiveDrink.beverage} (\$${stats.leastExpensiveDrink.price}) by ${stats.leastExpensiveDrink.coworker}"

            println "\nIndividual payment balances:"
            summary.balances.each { name, balance ->
                String status = balance >= 0 ? "ahead" : "behind"
                println "  $name: \$${Math.abs(balance)} $status"
            }

            println "\nSuggested next payer: ${summary.suggestedPayer}"
        }
    }

    /**
     * Remove a coworker from the system
     */
    private void removeCoworker() {
        println "\n=== Remove a Coworker ==="

        def coworkers = rotationManager.getAllCoworkers()
        if (coworkers.isEmpty()) {
            println "No coworkers found. Add some first!"
            return
        }

        // Display all coworkers
        coworkers.eachWithIndex { coworker, index ->
            println "${index + 1}. ${coworker}"
        }

        // Select coworker to remove
        int coworkerIndex = readIntInput("Enter coworker number to remove", 1, coworkers.size())
        Coworker selectedCoworker = rotationManager.getCoworkerByIndex(coworkerIndex)

        if (!selectedCoworker) {
            println "Invalid coworker selection!"
            return
        }

        // Confirm removal
        println "\nYou are about to remove: ${selectedCoworker.name}"
        println "Are you sure? (y/n)"
        String confirm = scanner.nextLine().trim().toLowerCase()

        if (confirm == 'y' || confirm == 'yes') {
            boolean removed = rotationManager.removeCoworker(selectedCoworker.id)

            if (removed) {
                println "Coworker removed successfully!"
            } else {
                println "Could not remove coworker. They may have paid for orders previously."
                println "Removing them would affect the payment history accuracy."
            }
        } else {
            println "Removal cancelled."
        }
    }

    // Helper methods for input

    /**
     * Read a string from the console with validation
     */
    private String readStringInput(String prompt) {
        String input = ""
        while (input.isEmpty()) {
            println "$prompt:"
            input = scanner.nextLine().trim()
            if (input.isEmpty()) {
                println "Input cannot be empty. Please try again."
            }
        }
        return input
    }

    /**
     * Read an integer within a specified range
     */
    private int readIntInput(String prompt, int min, int max) {
        while (true) {
            println "$prompt (${min}-${max}):"
            try {
                String input = scanner.nextLine().trim()
                int value = Integer.parseInt(input)
                if (value >= min && value <= max) {
                    return value
                } else {
                    println "Please enter a number between $min and $max."
                }
            } catch (NumberFormatException e) {
                println "Please enter a valid number."
            }
        }
    }

    /**
     * Read a decimal value for prices
     */
    private BigDecimal readDecimalInput(String prompt) {
        while (true) {
            println "$prompt:"
            try {
                String input = scanner.nextLine().trim()
                BigDecimal value = new BigDecimal(input)
                if (value >= 0) {
                    return value
                } else {
                    println "Please enter a non-negative number."
                }
            } catch (NumberFormatException e) {
                println "Please enter a valid decimal number (e.g., 3.50)."
            }
        }
    }
}