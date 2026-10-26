package factorymethod;

import model.Booking;

/**
 * FACTORY METHOD PATTERN - Creator (abstract)
 * Participants (Creator side): TicketCreator (Creator), GeneralTicketCreator /
 * SleeperTicketCreator / ACTicketCreator / TatkalTicketCreator (ConcreteCreators)
 *
 * The factory method createTicket() is overridden by each subclass to
 * decide WHICH concrete Ticket product gets instantiated, while
 * bookTicket() (the template-ish helper) stays common to all creators.
 */
public abstract class TicketCreator {

    // The Factory Method
    public abstract Ticket createTicket();

    public double issueTicket(Booking booking) {
        Ticket ticket = createTicket();          // delegated to subclass
        double surcharge = ticket.calculateSurcharge(booking.getFare());
        ticket.printTicket(booking);
        System.out.println("Surcharge applied: " + surcharge);
        return booking.getFare() + surcharge;
    }
}
