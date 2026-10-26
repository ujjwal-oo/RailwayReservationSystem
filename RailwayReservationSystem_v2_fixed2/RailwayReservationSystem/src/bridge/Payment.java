package bridge;

/**
 * BRIDGE PATTERN - Abstraction
 * Holds a reference ("bridge") to a PaymentGateway implementor rather
 * than inheriting from it. Refined abstractions add behaviour on top
 * (e.g. FastTrackPayment could add retry/priority logic) while reusing
 * whichever gateway is plugged in.
 *
 * Participants:
 *  - Abstraction: Payment
 *  - RefinedAbstraction: FastTrackPayment
 *  - Implementor: PaymentGateway
 *  - ConcreteImplementors: UPIGateway, CreditCardGateway, NetBankingGateway
 */
public class Payment {
    protected PaymentGateway gateway; // the bridge

    public Payment(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public boolean pay(double amount) {
        return gateway.processTransaction(amount);
    }
}
