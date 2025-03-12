package coffeerotation.model

/**
 * Represents a coworker who participates in the coffee rotation.
 * Each coworker has a name, preferred coffee beverage, and a history of payments.
 */
class Coworker {
    /** Unique identifier for the coworker */
    String id

    /** Name of the coworker */
    String name

    /** The coworker's preferred coffee beverage */
    String preferredBeverage

    /** Price of the coworker's preferred beverage */
    BigDecimal beveragePrice

    /** Running total amount this coworker has paid */
    BigDecimal totalPaid = 0.0

    /** Number of times this coworker has paid for the group */
    int paymentCount = 0

    /** List of dates when this coworker paid */
    List<Date> paymentDates = []

    /**
     * Constructor with required fields
     */
    Coworker(String name, String preferredBeverage, BigDecimal beveragePrice) {
        this.id = UUID.randomUUID().toString()
        this.name = name
        this.preferredBeverage = preferredBeverage
        this.beveragePrice = beveragePrice
    }

    /**
     * Records a payment made by this coworker
     * @param amount The total amount paid
     * @param date The date of payment (defaults to today)
     */
    void recordPayment(BigDecimal amount, Date date = new Date()) {
        totalPaid += amount
        paymentCount++
        paymentDates.add(date)
    }

    /**
     * Calculates how much this coworker is "ahead" or "behind" in payments
     * @param totalGroupCost Total cost of all beverages for the group over time
     * @param coworkerCount Number of coworkers in the group
     * @return Positive if ahead (paid more than fair share), negative if behind
     */
    BigDecimal getPaymentBalance(BigDecimal totalGroupCost, int coworkerCount) {
        BigDecimal fairShare = totalGroupCost / coworkerCount
        return totalPaid - fairShare
    }

    /**
     * Get the number of days since this coworker last paid
     * @return Days since last payment, or Integer.MAX_VALUE if never paid
     */
    int getDaysSinceLastPayment() {
        if (paymentDates.isEmpty()) {
            return Integer.MAX_VALUE  // Never paid
        }

        Date lastPayment = paymentDates.max()
        Date today = new Date()

        // Calculate days between dates
        return (today - lastPayment)
    }

    @Override
    String toString() {
        return "${name} (prefers ${preferredBeverage} at \$${beveragePrice})"
    }
}