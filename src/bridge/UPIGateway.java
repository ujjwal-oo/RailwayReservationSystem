package bridge;

public class UPIGateway implements PaymentGateway {
    @Override
    public boolean processTransaction(double amount) {
        System.out.println("[Bridge:UPIGateway] Processing Rs." + amount + " via UPI (NPCI switch)...");
        return true;
    }
}
