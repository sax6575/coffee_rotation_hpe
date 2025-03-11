package coffeerotation.service

import coffeerotation.model.Coworker
import coffeerotation.model.CoffeeOrder

/**
 * Service responsible for calculating payment balances and determining fairness
 * in the coffee rotation system.
 */
class PaymentCalculator {
    /** List of all recorded orders */
    private List<CoffeeOrder> orderHistory = []

    /**
     * Get the list of all recorded orders
     * @return List of all coffee orders
     */
    List<CoffeeOrder> getOrderHistory() {
        return orderHistory
    }

    /** List of all coworkers in the rotation */
    private List<Coworker> coworkers = []

    /**
     * Constructor accepting a list of coworkers
     */
    PaymentCalculator(List<Coworker> coworkers) {
        this.coworkers = coworkers
    }

    /**
     * Add a new order to the history and record the payment
     * @param order The new coffee order to add
     */
    void recordOrder(CoffeeOrder order) {
        orderHistory.add(order)
        order.recordPayment()
    }

    /**
     * Calculate the total spent across all orders
     * @return Total spent on coffee
     */
    BigDecimal getTotalGroupSpending() {
        return orderHistory.sum { it.totalCost } ?: 0.0
    }

    /**
     * Get the fair share amount each coworker should have paid
     * @return The fair share amount per person
     */
    BigDecimal getFairShareAmount() {
        if (coworkers.size() == 0) return 0.0
        return getTotalGroupSpending() / coworkers.size()
    }

    /**
     * Calculate each coworker's balance (how much they're ahead or behind)
     * @return Map of coworker name to balance amount (positive is ahead, negative is behind)
     */
    Map<String, BigDecimal> getPaymentBalances() {
        BigDecimal totalSpent = getTotalGroupSpending()
        int coworkerCount = coworkers.size()

        return coworkers.collectEntries { coworker ->
            [(coworker.name): coworker.getPaymentBalance(totalSpent, coworkerCount)]
        }
    }

    /**
     * Calculate who should pay next based on payment history and fairness
     * @return The coworker who should pay next
     */
    Coworker suggestNextPayer() {
        if (coworkers.isEmpty()) {
            return null
        }

        // Get balances for each coworker
        def balances = getPaymentBalances()

        // First prioritize anyone who hasn't paid yet
        def neverPaid = coworkers.findAll { it.paymentCount == 0 }
        if (neverPaid) {
            return neverPaid[0]
        }

        // Next prioritize those who are most "behind" in payments (lowest balance)
        return coworkers.min { balances[it.name] }
    }

    /**
     * Get summary statistics about the payment history
     * @return Map containing various statistics
     */
    Map<String, Object> getPaymentStatistics() {
        if (coworkers.isEmpty() || orderHistory.isEmpty()) {
            return [
                    totalSpent: 0.0,
                    averageOrderCost: 0.0,
                    orderCount: 0,
                    mostExpensiveDrink: null,
                    leastExpensiveDrink: null
            ]
        }

        def mostExpensiveCoworker = coworkers.max { it.beveragePrice }
        def leastExpensiveCoworker = coworkers.min { it.beveragePrice }

        return [
                totalSpent: getTotalGroupSpending(),
                averageOrderCost: getTotalGroupSpending() / orderHistory.size(),
                orderCount: orderHistory.size(),
                mostExpensiveDrink: [
                        coworker: mostExpensiveCoworker.name,
                        beverage: mostExpensiveCoworker.preferredBeverage,
                        price: mostExpensiveCoworker.beveragePrice
                ],
                leastExpensiveDrink: [
                        coworker: leastExpensiveCoworker.name,
                        beverage: leastExpensiveCoworker.preferredBeverage,
                        price: leastExpensiveCoworker.beveragePrice
                ]
        ]
    }
}