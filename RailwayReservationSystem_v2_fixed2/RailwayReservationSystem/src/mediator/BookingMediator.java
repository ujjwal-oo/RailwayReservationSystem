package mediator;

/**
 * MEDIATOR PATTERN - Mediator interface.
 * Colleagues never call each other; they report events here and the
 * mediator decides which colleague acts next.
 */
public interface BookingMediator {
    void notify(Colleague sender, BookingEvent event, BookingContext ctx);
}
