package bridge;

/**
 * RefinedAbstraction: adds Tatkal-style priority-fee handling on top of
 * whatever concrete gateway is bridged in, without any of the gateway
 * classes needing to know about "fast track" logic.
 */
public class FastTrackPayment extends Payment {

    private static final double PRIORITY_FEE = 20.0;

    public FastTrackPayment(PaymentGateway gateway) {
        super(gateway);
    }

    @Override
    public boolean pay(double amount) {
        double total = amount + PRIORITY_FEE;
        System.out.println("[Bridge:FastTrackPayment] Adding priority fee Rs." + PRIORITY_FEE);
        return gateway.processTransaction(total);
    }
}
