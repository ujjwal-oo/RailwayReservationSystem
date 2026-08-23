package factorymethod;

import model.Booking;

public class ACTicket implements Ticket {
    @Override
    public void printTicket(Booking booking) {
        System.out.println("===== AC CLASS TICKET =====");
        System.out.println(booking);
        System.out.println("Bedding included. Pantry service available.");
        System.out.println("============================");
    }

    @Override
    public double calculateSurcharge(double baseFare) {
        return baseFare * 0.75;
    }
}
