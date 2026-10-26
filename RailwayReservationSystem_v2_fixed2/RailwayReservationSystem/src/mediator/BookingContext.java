package mediator;

import model.Booking;

/**
 * State of ONE request travelling through the mediator. Keeps the mediator
 * itself stateless.
 */
public class BookingContext {

    public enum Flow { NEW_BOOKING, CANCEL, UNDO_CANCEL }

    private final Flow flow;
    private Booking booking;
    private boolean success = true;
    private String message = "";
    private boolean needsBerth = false; // set when an undo restored a booking whose berth was taken

    public BookingContext(Flow flow, Booking booking) {
        this.flow = flow;
        this.booking = booking;
    }

    public Flow getFlow() { return flow; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isNeedsBerth() { return needsBerth; }
    public void setNeedsBerth(boolean needsBerth) { this.needsBerth = needsBerth; }

    public void fail(String message) { this.success = false; this.message = message; }
    public void info(String message) { this.message = message; }
}
