package coffeerotation.model

class Coworker {

    String id
    String name
    String preferredBeverage
    BigDecimal beveragePrice
    BigDecimal totalPaid = 0.0
    int paymentCount = 0
    List<Date> paymentDates = []

    Coworker(String name, String, String preferredBeverage, BigDecimal beveragePrice) {
        this.id = UUID.randomUUID().toString()
        this.name = name
        this.preferredBeverage = preferredBeverage
        this.beveragePrice = beveragePrice
    }

    void recordPayment(BigDecimal amount, Date date = new Date()) {
        totalPaid += amount
        paymentCount++
        paymentDates.add(date)
    }

    BigDecimal getPaymentBalance(BigDecimal totalGroupCost, int coworkerCount) {
        BigDecimal fairShare = totalGroupCost / coworkerCount
        return totalPaid - fairShare
    }

    int getDaysSinceLastPayment() {
        if (paymentDates.isEmpty()) {
            return Integer.MAX_VALUE
        }

        Date lastPayment = paymentDates.max()
        Date today = new Date()

        return (today - lastPayment)
    }

    @Override
    String toString() {
        return "{name} (prefers ${preferredBeverage} at \$${beveragePrice}"
    }
}
