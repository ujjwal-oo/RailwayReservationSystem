package bridge;

public class NetBankingGateway implements PaymentGateway {
    @Override
    public boolean processTransaction(double amount) {
        System.out.println("[Bridge:NetBankingGateway] Processing Rs." + amount + " via Net Banking...");
        return true;
    }
}
