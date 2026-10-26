package mediator;

import bridge.CreditCardGateway;
import bridge.FastTrackPayment;
import bridge.NetBankingGateway;
import bridge.Payment;
import bridge.PaymentGateway;
import bridge.UPIGateway;
import model.Booking;

/** Colleague: charges the passenger through the Bridge (Payment x Gateway). */
public class PaymentDesk extends Colleague {

    public void charge(BookingContext ctx) {
        Booking b = ctx.getBooking();
        PaymentGateway gateway;
        switch (b.getPaymentMode().toUpperCase()) {
            case "CREDIT_CARD": gateway = new CreditCardGateway(); break;
            case "NET_BANKING": gateway = new NetBankingGateway(); break;
            default:            gateway = new UPIGateway();
        }
        Payment payment = "TATKAL".equalsIgnoreCase(b.getTravelClass())
                ? new FastTrackPayment(gateway) : new Payment(gateway);
        boolean ok = payment.pay(b.getFare());
        mediator.notify(this, ok ? BookingEvent.PAYMENT_DONE : BookingEvent.PAYMENT_FAILED, ctx);
    }
}
