package factorymethod;

import model.Booking;

public class SleeperTicket implements Ticket {
    @Override
    public void printTicket(Booking booking) {
        System.out.println("===== SLEEPER CLASS TICKET =====");
        System.out.println(booking);
        System.out.println("Berth allotted. Bedding not included.");
        System.out.println("=================================");
    }

    @Override
    public double calculateSurcharge(double baseFare) {
        return baseFare * 0.20;
    }
}
