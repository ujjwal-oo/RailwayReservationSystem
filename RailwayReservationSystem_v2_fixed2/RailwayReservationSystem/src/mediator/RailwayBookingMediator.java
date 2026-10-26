package mediator;

import memento.BookingHistory;
import model.Booking;
import prototype.BookingPrototypeRegistry;
import repository.BookingRepository;

/**
 * MEDIATOR PATTERN - ConcreteMediator.
 *
 * Colleagues: FareDesk (Strategy), ReservationDesk (Proxy + Chain + Memento),
 *             BerthDesk (Strategy), PaymentDesk (Bridge), NotificationDesk (Observer).
 *
 * They are completely unaware of each other. All the workflow rules live in
 * notify():
 *
 *   NEW BOOKING : fare -> reserve -> (berth if confirmed) -> payment -> notify
 *   CANCEL      : cancel (snapshot saved) -> notify
 *   UNDO CANCEL : restore snapshot -> (re-allot berth if it was taken) -> notify
 */
public class RailwayBookingMediator implements BookingMediator {

    private final BookingRepository repository = new BookingRepository();
    private final BookingHistory history = new BookingHistory();
    private final BookingPrototypeRegistry prototypes = new BookingPrototypeRegistry(repository);

    private final FareDesk fareDesk = new FareDesk();
    private final ReservationDesk reservationDesk = new ReservationDesk(repository, history, prototypes);
    private final BerthDesk berthDesk = new BerthDesk(repository);
    private final PaymentDesk paymentDesk = new PaymentDesk();
    private final NotificationDesk notificationDesk = new NotificationDesk();

    public RailwayBookingMediator() {
        fareDesk.setMediator(this);
        reservationDesk.setMediator(this);
        berthDesk.setMediator(this);
        paymentDesk.setMediator(this);
        notificationDesk.setMediator(this);
    }

    // ---------------- entry points ----------------

    public synchronized BookingContext book(Booking booking) {
        BookingContext ctx = new BookingContext(BookingContext.Flow.NEW_BOOKING, booking);
        fareDesk.calculate(ctx);
        return ctx;
    }

    public synchronized BookingContext cancel(int pnr) {
        Booking b = repository.findByPnr(pnr);
        BookingContext ctx = new BookingContext(BookingContext.Flow.CANCEL, b);
        if (b == null) { ctx.fail("PNR not found"); return ctx; }
        if ("CANCELLED".equals(b.getStatus())) { ctx.fail("Ticket is already cancelled"); return ctx; }
        reservationDesk.cancel(ctx);
        return ctx;
    }

    public synchronized BookingContext undoCancel(int pnr) {
        Booking b = repository.findByPnr(pnr);
        BookingContext ctx = new BookingContext(BookingContext.Flow.UNDO_CANCEL, b);
        if (b == null) { ctx.fail("PNR not found"); return ctx; }
        if (!"CANCELLED".equals(b.getStatus())) { ctx.fail("Only a cancelled ticket can be restored"); return ctx; }
        reservationDesk.restore(ctx);
        return ctx;
    }

    // ---------------- the mediation logic ----------------

    @Override
    public void notify(Colleague sender, BookingEvent event, BookingContext ctx) {
        Booking b = ctx.getBooking();
        BookingContext.Flow flow = ctx.getFlow();
        System.out.println("[Mediator] " + sender.getClass().getSimpleName() + " -> " + event);

        switch (event) {
            case FARE_CALCULATED:
                reservationDesk.reserve(ctx);
                break;

            case RESERVATION_REJECTED:
                break; // ctx already marked failed

            case RESERVED:
                if (flow == BookingContext.Flow.NEW_BOOKING) {
                    if ("CONFIRMED".equals(b.getStatus())) berthDesk.allocate(ctx);
                    else paymentDesk.charge(ctx);          // waitlisted: no berth yet
                } else {
                    notificationDesk.broadcast(ctx);       // waitlisted after undo
                }
                break;

            case BERTH_ALLOCATED:
                if (flow == BookingContext.Flow.NEW_BOOKING) paymentDesk.charge(ctx);
                else notificationDesk.broadcast(ctx);
                break;

            case BERTH_UNAVAILABLE:
                reservationDesk.waitlist(ctx);              // -> RESERVED (waitlisted)
                break;

            case PAYMENT_DONE:
                notificationDesk.broadcast(ctx);
                break;

            case PAYMENT_FAILED:
                reservationDesk.rollback(ctx);
                break;

            case CANCELLED:
                notificationDesk.broadcast(ctx);
                break;

            case RESTORED:
                if (ctx.isNeedsBerth()) berthDesk.allocate(ctx);
                else notificationDesk.broadcast(ctx);
                break;
        }
    }

    // ---------------- read helpers for the web layer ----------------

    public BookingRepository getRepository() { return repository; }
    public BookingHistory getHistory() { return history; }
    public BookingPrototypeRegistry getPrototypes() { return prototypes; }
    public String explainBerth(Booking b) { return berthDesk.explain(b); }
}
