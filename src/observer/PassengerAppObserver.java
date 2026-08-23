package observer;

import model.Booking;

/**
 * ConcreteObserver: simulates updating the passenger's in-app booking status.
 */
public class PassengerAppObserver implements ReservationObserver {
    @Override
    public void update(Booking booking) {
        System.out.println("[Observer:App] Passenger app updated -> " + booking.getPassenger().getName() +
                "'s booking is now " + booking.getStatus() + " (PNR " + booking.getPnr() + ")");
    }
}
