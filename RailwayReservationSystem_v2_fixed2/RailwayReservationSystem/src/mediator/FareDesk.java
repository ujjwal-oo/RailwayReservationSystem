package mediator;

import model.Booking;
import strategy.FareContext;
import strategy.FareStrategy;

/** Colleague: calculates the fare using the Strategy pattern. */
public class FareDesk extends Colleague {

    private final FareContext fareContext = new FareContext();

    public void calculate(BookingContext ctx) {
        Booking b = ctx.getBooking();
        FareStrategy s = fareContext.apply(b);
        System.out.println("[FareDesk] " + s.category() + " strategy: base Rs." + b.getBaseFare() +
                " -> Rs." + b.getFare());
        mediator.notify(this, BookingEvent.FARE_CALCULATED, ctx);
    }
}
