package strategy;

/** Concession for passengers aged 60 and above. */
public class SeniorCitizenFareStrategy implements FareStrategy {
    public String category() { return "SENIOR_CITIZEN"; }
    public double discountPercent() { return 40; }
}
