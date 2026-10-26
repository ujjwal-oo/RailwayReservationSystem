package strategy;

import model.Booking;

import java.util.List;
import java.util.Optional;

/**
 * Senior citizens: give a lower berth (LOWER, then SIDE_LOWER) if one is
 * free; otherwise fall back to the best remaining berth.
 */
public class LowerBerthPriorityStrategy implements BerthAllocationStrategy {

    public String name() { return "LOWER_PRIORITY"; }

    public Optional<Berth> choose(List<Berth> free, Booking booking) {
        for (Berth b : free) if (Berth.LOWER.equals(b.getType())) return Optional.of(b);
        for (Berth b : free) if (Berth.SIDE_LOWER.equals(b.getType())) return Optional.of(b);
        return free.stream().findFirst();
    }
}
