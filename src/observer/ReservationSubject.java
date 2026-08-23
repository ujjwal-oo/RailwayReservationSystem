package observer;

import model.Booking;
import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER PATTERN - Subject
 * Maintains the list of observers and notifies all of them with the
 * updated Booking object whenever a reservation is confirmed/changed.
 */
public class ReservationSubject {
    private final List<ReservationObserver> observers = new ArrayList<>();

    public void attach(ReservationObserver observer) {
        observers.add(observer);
    }

    public void detach(ReservationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Booking booking) {
        for (ReservationObserver o : observers) {
            o.update(booking);
        }
    }
}
