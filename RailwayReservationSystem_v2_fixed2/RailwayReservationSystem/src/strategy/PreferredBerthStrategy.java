package strategy;

import model.Booking;

import java.util.List;
import java.util.Optional;

/** Honour the passenger's stated preference if possible, else standard order. */
public class PreferredBerthStrategy implements BerthAllocationStrategy {

    public String name() { return "PREFERENCE"; }

    public Optional<Berth> choose(List<Berth> free, Booking booking) {
        String pref = booking.getBerthPreference();
        for (Berth b : free) if (b.getType().equals(pref)) return Optional.of(b);
        return new StandardBerthStrategy().choose(free, booking);
    }
}
