package strategy;

/**
 * STRATEGY PATTERN (1/2) - Strategy interface for fare calculation.
 * Each concrete strategy encapsulates one pricing rule.
 */
public interface FareStrategy {
    String category();
    /** Positive = concession (% off), negative = surcharge (% extra). */
    double discountPercent();

    default double calculate(double baseFare) {
        return Math.round(baseFare * (1 - discountPercent() / 100.0));
    }
}
