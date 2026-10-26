package strategy;

/** Tatkal quota: 30% premium (matches TatkalTicket's surcharge). */
public class TatkalFareStrategy implements FareStrategy {
    public String category() { return "TATKAL"; }
    public double discountPercent() { return -30; }
}
