package mediator;

import memento.BookingHistory;
import memento.BookingMemento;
import model.Booking;
import prototype.BookingPrototypeRegistry;
import proxy.ReservationService;
import proxy.ReservationServiceProxy;
import repository.BookingRepository;
import strategy.Berth;

import java.util.Set;

/**
 * Colleague: owns reservation state. Uses the Proxy (+ Chain of
 * Responsibility) to create bookings and the Memento caretaker to make
 * cancellations undoable.
 */
public class ReservationDesk extends Colleague {

    private final ReservationService proxy = new ReservationServiceProxy(); // validation + DB
    private final BookingRepository repository;
    private final BookingHistory history;
    private final BookingPrototypeRegistry prototypes;

    public ReservationDesk(BookingRepository repository, BookingHistory history,
                           BookingPrototypeRegistry prototypes) {
        this.repository = repository;
        this.history = history;
        this.prototypes = prototypes;
    }

    public void reserve(BookingContext ctx) {
        Booking b = ctx.getBooking();
        if (!proxy.reserve(b)) {
            ctx.fail(b.getFailureReason() != null ? b.getFailureReason()
                    : "Booking rejected by validation chain");
            mediator.notify(this, BookingEvent.RESERVATION_REJECTED, ctx);
            return;
        }
        prototypes.register(b);
        mediator.notify(this, BookingEvent.RESERVED, ctx);
    }

    /** No berth was free after all: keep the booking but waitlist it. */
    public void waitlist(BookingContext ctx) {
        Booking b = ctx.getBooking();
        b.clearBerth();
        b.setStatus("WAITLISTED");
        repository.updateState(b);
        mediator.notify(this, BookingEvent.RESERVED, ctx);
    }

    /** Payment failed: undo the reservation. */
    public void rollback(BookingContext ctx) {
        Booking b = ctx.getBooking();
        b.clearBerth();
        b.setStatus("CANCELLED");
        repository.updateState(b);
        ctx.fail("Payment failed - booking cancelled");
    }

    // ---------- MEMENTO usage ----------
    public void cancel(BookingContext ctx) {
        Booking b = ctx.getBooking();
        history.save(b.getPnr(), b.createMemento("BEFORE_CANCEL")); // Caretaker stores the snapshot
        b.clearBerth();
        b.setStatus("CANCELLED");
        repository.updateState(b);
        System.out.println("[ReservationDesk] PNR " + b.getPnr() + " cancelled (snapshot saved).");
        mediator.notify(this, BookingEvent.CANCELLED, ctx);
    }

    public void restore(BookingContext ctx) {
        Booking b = ctx.getBooking();
        BookingMemento m = history.pop(b.getPnr());
        if (m == null) {
            ctx.fail("Nothing to undo for this booking");
            return;
        }
        b.restoreFromMemento(m); // Originator reads the snapshot
        if ("CONFIRMED".equals(b.getStatus()) && b.hasBerth()) {
            Set<String> taken = repository.takenBerths(b.getTrainName(), b.getJourneyDate(),
                    b.getTravelClass(), b.getPnr());
            if (taken.contains(b.getCoach() + "-" + b.getBerthNo())) {
                b.clearBerth();            // berth was given to someone else meanwhile
                ctx.setNeedsBerth(true);
            }
        } else if ("CONFIRMED".equals(b.getStatus())) {
            ctx.setNeedsBerth(true);
        }
        repository.updateState(b);
        System.out.println("[ReservationDesk] PNR " + b.getPnr() + " restored to " + b.getStatus());
        mediator.notify(this, BookingEvent.RESTORED, ctx);
    }
}
