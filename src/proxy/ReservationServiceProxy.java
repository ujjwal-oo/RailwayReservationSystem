package proxy;

import chain.PassengerDetailsHandler;
import chain.PaymentValidationHandler;
import chain.SeatAvailabilityHandler;
import chain.ValidationHandler;
import model.Booking;
import observer.ReservationSubject;

/**
 * Proxy: sits in front of RealReservationService. Before delegating the
 * actual DB insert, it:
 *   1. Runs the Chain-of-Responsibility validation pipeline.
 *   2. Logs access attempts.
 *   3. Only on success, notifies Observers (Passenger/SMS/Email) with the
 *      Booking OBJECT (not a String).
 */
public class ReservationServiceProxy implements ReservationService {

    private final RealReservationService realService = new RealReservationService();
    private final ReservationSubject reservationSubject;
    private final ValidationHandler chainHead;

    public ReservationServiceProxy(ReservationSubject reservationSubject) {
        this.reservationSubject = reservationSubject;

        // Build the Chain of Responsibility once
        ValidationHandler passengerCheck = new PassengerDetailsHandler();
        ValidationHandler seatCheck = new SeatAvailabilityHandler();
        ValidationHandler paymentCheck = new PaymentValidationHandler();
        passengerCheck.setNext(seatCheck).setNext(paymentCheck);
        this.chainHead = passengerCheck;
    }

    @Override
    public boolean reserve(Booking booking) {
        System.out.println("\n[Proxy] Incoming reservation request for " + booking.getPassenger().getName());
        System.out.println("[Proxy] Running validation chain...");

        boolean valid = chainHead.handle(booking);
        if (!valid) {
            System.out.println("[Proxy] Reservation REJECTED by validation chain.");
            return false;
        }

        System.out.println("[Proxy] Validation passed. Delegating to RealReservationService.");
        boolean success = realService.reserve(booking);

        if (success) {
            reservationSubject.notifyObservers(booking); // Observer pattern trigger
        }
        return success;
    }
}
