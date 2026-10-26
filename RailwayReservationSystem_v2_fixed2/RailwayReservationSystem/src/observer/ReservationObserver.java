package observer;

import model.Booking;

/**
 * OBSERVER PATTERN - Observer interface
 * update() takes the Booking OBJECT, per instructor feedback from the
 * Faculty Leave Management System practical (pass objects, not strings).
 */
public interface ReservationObserver {
    void update(Booking booking);
}
