package factorymethod;

import model.Booking;

public class GeneralTicket implements Ticket {
    @Override
    public void printTicket(Booking booking) {
        System.out.println("===== GENERAL TICKET =====");
        System.out.println(booking);
        System.out.println("No seat guarantee. Standing room only if unreserved.");
        System.out.println("===========================");
    }

    @Override
    public double calculateSurcharge(double baseFare) {
        return 0.0; // no surcharge
    }
}
