package mediator;

import observer.EmailNotificationObserver;
import observer.PassengerAppObserver;
import observer.ReservationSubject;
import observer.SMSNotificationObserver;

/** Colleague: wraps the Observer subject (app + SMS + email). */
public class NotificationDesk extends Colleague {

    private final ReservationSubject subject = new ReservationSubject();

    public NotificationDesk() {
        subject.attach(new PassengerAppObserver());
        subject.attach(new SMSNotificationObserver());
        subject.attach(new EmailNotificationObserver());
    }

    public void broadcast(BookingContext ctx) {
        subject.notifyObservers(ctx.getBooking());
    }
}
