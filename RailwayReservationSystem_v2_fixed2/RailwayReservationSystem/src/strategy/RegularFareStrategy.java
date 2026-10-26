package strategy;

public class RegularFareStrategy implements FareStrategy {
    public String category() { return "REGULAR"; }
    public double discountPercent() { return 0; }
}
