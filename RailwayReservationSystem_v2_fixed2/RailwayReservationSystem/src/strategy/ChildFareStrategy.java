package strategy;

/** Half fare for children aged 5 to 11. */
public class ChildFareStrategy implements FareStrategy {
    public String category() { return "CHILD"; }
    public double discountPercent() { return 50; }
}
