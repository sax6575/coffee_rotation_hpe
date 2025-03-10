package coffeerotation.model

class CoffeeOrder {

    String id
    Date orderDate
    List<Coworker> participants = []
    Coworker paidBy
    BigDecimal totalCost

    CoffeeOrder(Date orderDate, List<Coworker> participants, Coworker paidBy) {
        this.id = UUID.randomUUID().toString()
        this.orderDate = orderDate
        this.participants = participants
        this.paidBy = paidBy
        calculateTotalCost()
    }

    private void calculateTotalCost() {
        this.totalCost = participants.sum { it.beveragePrice } ?: 0 as BigDecimal
    }

    void recordPayment() {
        paidBy.recordPayment(totalCost, orderDate)
    }

    Map<String, BigDecimal> getItemizedCosts() {
        return participants.collectEntries { coworker ->
            [(coworker.name): coworker.beveragePrice]
        }
    }

    @Override
    String toString() {
        return "Coffee order on ${orderDate.format('yyyy-MM-dd')} - Paid by: ${paidBy.name}, Total: \$${totalCost}"
    }
}
