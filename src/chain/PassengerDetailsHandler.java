package chain;

import model.Booking;

public class PassengerDetailsHandler extends ValidationHandler {
    @Override
    public boolean handle(Booking booking) {
        if (booking.getPassenger() == null ||
            booking.getPassenger().getName() == null ||
            booking.getPassenger().getName().trim().isEmpty()) {
            System.out.println("[Chain] REJECTED at PassengerDetailsHandler: Invalid passenger details.");
            return false;
        }
        if (booking.getPassenger().getAge() <= 0 || booking.getPassenger().getAge() > 120) {
            System.out.println("[Chain] REJECTED at PassengerDetailsHandler: Invalid age.");
            return false;
        }
        System.out.println("[Chain] PassengerDetailsHandler: OK -> passing to next handler.");
        return super.handle(booking);
    }
}
