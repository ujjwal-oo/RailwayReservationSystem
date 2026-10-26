package chain;

import model.Booking;
import java.util.HashMap;
import java.util.Map;

public class SeatAvailabilityHandler extends ValidationHandler {

    // Simulated seat inventory per class
    private static final Map<String, Integer> AVAILABLE_SEATS = new HashMap<>();
    static {
        AVAILABLE_SEATS.put("GENERAL", 50);
        AVAILABLE_SEATS.put("SLEEPER", 30);
        AVAILABLE_SEATS.put("AC", 10);
        AVAILABLE_SEATS.put("TATKAL", 5);
    }

    @Override
    public boolean handle(Booking booking) {
        String cls = booking.getTravelClass().toUpperCase();
        Integer seats = AVAILABLE_SEATS.get(cls);
        if (seats == null) {
            System.out.println("[Chain] REJECTED at SeatAvailabilityHandler: Unknown class " + cls);
            return false;
        }
        if (seats <= 0) {
            System.out.println("[Chain] " + cls + " full -> booking will be WAITLISTED downstream.");
            booking.setStatus("WAITLISTED");
        } else {
            AVAILABLE_SEATS.put(cls, seats - 1);
            System.out.println("[Chain] SeatAvailabilityHandler: Seat allotted in " + cls +
                    " (" + (seats - 1) + " left) -> passing to next handler.");
        }
        return super.handle(booking);
    }
}
