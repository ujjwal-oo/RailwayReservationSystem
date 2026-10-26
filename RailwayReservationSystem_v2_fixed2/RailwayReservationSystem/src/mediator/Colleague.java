package mediator;

/** MEDIATOR PATTERN - abstract Colleague: knows only its mediator. */
public abstract class Colleague {
    protected BookingMediator mediator;

    public void setMediator(BookingMediator mediator) { this.mediator = mediator; }
}
