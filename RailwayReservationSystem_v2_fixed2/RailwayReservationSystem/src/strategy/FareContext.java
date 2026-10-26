package strategy;

import model.Booking;

/**
 * STRATEGY PATTERN (1/2) - Context.
 * Chooses the fare strategy from the passenger's age and class at runtime
 * and delegates the calculation to it.
 */
public class FareContext {

    public static final int SENIOR_AGE = 60;

    public FareStrategy select(int age, String travelClass) {
        if ("TATKAL".equalsIgnoreCase(travelClass)) return new TatkalFareStrategy();
        if (age >= SENIOR_AGE) return new SeniorCitizenFareStrategy();
        if (age >= 5 && age <= 11) return new ChildFareStrategy();
        return new RegularFareStrategy();
    }

    /** Applies the selected strategy to the booking (sets category + final fare). */
    public FareStrategy apply(Booking booking) {
        FareStrategy s = select(booking.getPassenger().getAge(), booking.getTravelClass());
        booking.setFareCategory(s.category());
        booking.setFare(s.calculate(booking.getBaseFare()));
        return s;
    }
}
