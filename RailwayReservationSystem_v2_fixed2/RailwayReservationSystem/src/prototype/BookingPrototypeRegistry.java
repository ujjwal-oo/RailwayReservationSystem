package prototype;

import model.Booking;
import repository.BookingRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * PROTOTYPE PATTERN - Registry / Client
 * Keeps existing bookings as prototypes. A new booking is made by cloning a
 * registered prototype and changing only what differs (journey date, route,
 * passenger) instead of re-entering every field.
 *
 *  - Prototype          : prototype.Prototype / model.Booking
 *  - ConcretePrototype  : model.Booking, model.Passenger
 *  - Registry           : BookingPrototypeRegistry
 */
public class BookingPrototypeRegistry {

    private final Map<Integer, Booking> prototypes = new HashMap<>();
    private final BookingRepository repository;

    public BookingPrototypeRegistry(BookingRepository repository) {
        this.repository = repository;
    }

    public void register(Booking booking) {
        prototypes.put(booking.getPnr(), booking.copy());
    }

    /** A fresh un-booked clone of the booking with this PNR, or null if unknown. */
    public Booking getClone(int pnr) {
        Booking proto = prototypes.get(pnr);
        if (proto == null) {
            Booking stored = repository.findByPnr(pnr);
            if (stored == null) return null;
            register(stored);
            proto = prototypes.get(pnr);
        }
        return proto.copy();
    }

    /** Clone with source/destination swapped and a new date. */
    public Booking returnJourney(int pnr, String journeyDate) {
        Booking c = getClone(pnr);
        if (c == null) return null;
        String from = c.getSource();
        c.setSource(c.getDestination());
        c.setDestination(from);
        c.setJourneyDate(journeyDate);
        return c;
    }
}
