package chain;

import model.Booking;

/**
 * CHAIN OF RESPONSIBILITY PATTERN
 * --------------------------------
 * Abstract Handler. Each concrete handler validates one aspect of a Booking
 * request and either passes it along the chain or rejects it outright.
 *
 * Participants:
 *  - Handler: ValidationHandler
 *  - ConcreteHandlers: PassengerDetailsHandler, SeatAvailabilityHandler,
 *                       FareValidationHandler, PaymentValidationHandler
 *  - Client: ReservationServiceProxy (builds and triggers the chain)
 */
public abstract class ValidationHandler {

    protected ValidationHandler nextHandler;

    public ValidationHandler setNext(ValidationHandler next) {
        this.nextHandler = next;
        return next; // allows fluent chaining: h1.setNext(h2).setNext(h3);
    }

    public boolean handle(Booking booking) {
        if (nextHandler != null) {
            return nextHandler.handle(booking);
        }
        return true; // end of chain, nothing rejected it
    }
}
