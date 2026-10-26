package strategy;

import model.Booking;

import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN (2/2) - Strategy interface for berth allocation.
 * Given the free berths, each strategy decides which one to give.
 */
public interface BerthAllocationStrategy {
    String name();
    Optional<Berth> choose(List<Berth> freeBerths, Booking booking);
}
