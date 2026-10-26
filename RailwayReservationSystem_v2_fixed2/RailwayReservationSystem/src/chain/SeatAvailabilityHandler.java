package chain;

import model.Booking;
import repository.BookingRepository;
import strategy.BerthLayout;

/**
 * Checks real seat inventory (per train + journey date + class) from the
 * database. If the class is full the booking is marked WAITLISTED instead
 * of being rejected.
 */
public class SeatAvailabilityHandler extends ValidationHandler {

    private final BookingRepository repository = new BookingRepository();

    @Override
    public boolean handle(Booking booking) {
        String cls = booking.getTravelClass().toUpperCase();
        int capacity = BerthLayout.capacity(cls);
        if (capacity == 0) {
            System.out.println("[Chain] REJECTED at SeatAvailabilityHandler: Unknown class " + cls);
            booking.setFailureReason("Unknown travel class: " + cls);
            return false;
        }
        int used = repository.countConfirmed(booking.getTrainName(), booking.getJourneyDate(), cls);
        if (used >= capacity) {
            System.out.println("[Chain] " + cls + " full on " + booking.getJourneyDate() +
                    " -> booking will be WAITLISTED.");
            booking.setStatus("WAITLISTED");
        } else {
            System.out.println("[Chain] SeatAvailabilityHandler: " + (capacity - used) + " of " +
                    capacity + " " + cls + " berths free on " + booking.getJourneyDate() +
                    " -> passing to next handler.");
        }
        return super.handle(booking);
    }
}
