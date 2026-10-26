package factorymethod;

import model.Booking;

/**
 * FACTORY METHOD PATTERN - Product interface
 * Participants (Product side): Ticket (Product), GeneralTicket / SleeperTicket /
 * ACTicket / TatkalTicket (ConcreteProducts)
 */
public interface Ticket {
    void printTicket(Booking booking);
    double calculateSurcharge(double baseFare);
}
