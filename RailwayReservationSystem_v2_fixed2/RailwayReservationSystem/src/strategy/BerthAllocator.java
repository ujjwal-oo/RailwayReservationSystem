package strategy;

import model.Booking;

import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN (2/2) - Context.
 * Picks the allocation strategy for a passenger and runs it.
 *
 *   age >= 60            -> LowerBerthPriorityStrategy  (old get lower berth if available)
 *   explicit preference  -> PreferredBerthStrategy
 *   otherwise            -> StandardBerthStrategy
 */
public class BerthAllocator {

    public BerthAllocationStrategy select(Booking booking) {
        if (booking.getPassenger().getAge() >= FareContext.SENIOR_AGE) {
            return new LowerBerthPriorityStrategy();
        }
        String pref = booking.getBerthPreference();
        if (pref != null && !pref.equals("NONE")) return new PreferredBerthStrategy();
        return new StandardBerthStrategy();
    }

    public Optional<Berth> allocate(Booking booking, List<Berth> freeBerths) {
        return select(booking).choose(freeBerths, booking);
    }

    /** Human-readable explanation of what happened, shown on the ticket. */
    public String explain(Booking b) {
        if ("CANCELLED".equals(b.getStatus())) return "Berth released.";
        if ("WAITLISTED".equals(b.getStatus())) return "Waitlisted - no berth allotted yet.";
        if (!b.hasBerth()) return "Berth not allotted.";
        if (Berth.SEAT.equals(b.getBerthType())) return "Unreserved seat allotted.";
        boolean lower = Berth.LOWER.equals(b.getBerthType()) || Berth.SIDE_LOWER.equals(b.getBerthType());
        boolean senior = b.getPassenger().getAge() >= FareContext.SENIOR_AGE;
        String type = b.getBerthType().replace('_', ' ').toLowerCase();
        if (senior) {
            return lower ? "Lower berth allotted (senior citizen priority)."
                    : "Senior citizen priority applied, but no lower berth was free - allotted " + type + " berth.";
        }
        String pref = b.getBerthPreference();
        if (pref != null && !pref.equals("NONE")) {
            return pref.equals(b.getBerthType())
                    ? "Your " + type + " berth preference was met."
                    : "Preferred berth was not free - allotted " + type + " berth.";
        }
        return "Berth allotted automatically.";
    }
}
