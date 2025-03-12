package coffeerotation.ui

import coffeerotation.model.Coworker
import coffeerotation.model.CoffeeOrder
import coffeerotation.service.RotationManager

class CLI {
    private RotationManager rotationManager
    private Scanner scanner

    CLI() {
        rotationManager = new RotationManager()
        scanner = new Scanner(System.in)
    }

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
                case 0:
                    running = false
                    println "Thanks for using Coffee Rotation Calculator"
                    break
                default:
                    println "Invalid choice, please try again"
            }

            println ""

        }

    }

    private void displayMenu() {
        println "Main Menu:"
        println "1. Add a Coworker"
        println "2. List Coworkers"
        println "3. Create Coffee Order"
        println "4. Who Pays Next?"
        println "5. View Payment Summary"
        println "0. Exit"

    }

    private void addCoworker() {
        println "\n=== Add a Coworker ==="

        String name = readStringInput("Enter coworker name")
        String beverage = readStringInput("Enter preferred coffee drink")
        BigDecimal price = readDecimalInput("Enter beverage price")

        Coworker coworker = rotationManager.addCoworker(name, beverage, price)
        println "\nCoworker addedd successfully!"
        println coworker.toString()
    }

    private void listCoworkers() {
        println "\n=== Coworkers ==="

        def coworkers = rotationManager.getAllCoworkers()
        if (coworkers.isEmpty()) {
            println "No coworkers found, please add some!"
            return
        }

        coworkers.eachWithIndex{ coworker, index ->
            println "${index + 1}. ${coworker}"
        }
    }

    private void createOrder() {
        println "\n=== Create New Coffee Order ==="

        def coworkers = rotationManager.getAllCoworkers()
        if (coworkers.isEmpty()) {
            println "No coworkers found, please add some!"
        }

        coworkers.eachWithIndex{ coworker, index ->
            println "${index + 1}. ${coworker}"
        }

        println "\nSelect participants (comma-separated numbers, e.g., 1,2,3):"
        String participantsInput = scanner.nextLine().trim()
        List<Integer> participantIndices = participantsInput.split(",")
                .collect { it.trim() }
                .findAll { it.isInteger() }
                .collect { Integer.parseInt(it) - 1 }

        List<String> participantIds = participantIndices
                .findAll { it >= 0 && it < coworkers.size() }
                .collect { coworkers[it].id }

        if (participantIds.isEmpty()) {
            println "No valid participants selected!"
        }

        println "\nWho paid for this order? (enter number):"
        int payerIndex = readIntInput("Enter payer number", 1, coworkers.size()) - 1
        String payerId = coworkers[payerIndex].id

        def order = rotationManager.createOrder()


    }
}
