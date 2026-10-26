package strategy;

import model.Booking;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Default allocation for passengers with no preference. It hands out
 * middle/upper berths first and keeps lower berths for last, so lower
 * berths stay available for senior citizens.
 */
public class StandardBerthStrategy implements BerthAllocationStrategy {

    public String name() { return "STANDARD"; }

    private static int rank(String type) {
        switch (type) {
            case Berth.SEAT: return 0;
            case Berth.MIDDLE: return 1;
            case Berth.UPPER: return 2;
            case Berth.SIDE_UPPER: return 3;
            case Berth.SIDE_LOWER: return 4;
            default: return 5; // LOWER
        }
    }

    public Optional<Berth> choose(List<Berth> free, Booking booking) {
        return free.stream().min(Comparator.comparingInt(b -> rank(b.getType())));
    }
}
