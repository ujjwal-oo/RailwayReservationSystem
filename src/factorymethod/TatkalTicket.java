package factorymethod;

import model.Booking;

public class TatkalTicket implements Ticket {
    @Override
    public void printTicket(Booking booking) {
        System.out.println("===== TATKAL TICKET =====");
        System.out.println(booking);
        System.out.println("Emergency quota. Non-refundable.");
        System.out.println("==========================");
    }

    @Override
    public double calculateSurcharge(double baseFare) {
        return baseFare * 0.30;
    }
}
