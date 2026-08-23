package bridge;

public class CreditCardGateway implements PaymentGateway {
    @Override
    public boolean processTransaction(double amount) {
        System.out.println("[Bridge:CreditCardGateway] Processing Rs." + amount + " via Credit Card network...");
        return true;
    }
}
