package chain;

import model.Booking;
import java.util.Arrays;
import java.util.List;

public class PaymentValidationHandler extends ValidationHandler {

    private static final List<String> VALID_MODES = Arrays.asList("UPI", "CREDIT_CARD", "NET_BANKING");

    @Override
    public boolean handle(Booking booking) {
        if (booking.getFare() <= 0) {
            System.out.println("[Chain] REJECTED at PaymentValidationHandler: Invalid fare amount.");
            return false;
        }
        if (!VALID_MODES.contains(booking.getPaymentMode().toUpperCase())) {
            System.out.println("[Chain] REJECTED at PaymentValidationHandler: Unsupported payment mode.");
            return false;
        }
        System.out.println("[Chain] PaymentValidationHandler: OK -> passing to next handler.");
        return super.handle(booking);
    }
}
