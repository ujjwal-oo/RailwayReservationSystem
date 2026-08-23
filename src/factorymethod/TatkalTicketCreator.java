package factorymethod;

public class TatkalTicketCreator extends TicketCreator {
    @Override
    public Ticket createTicket() {
        return new TatkalTicket();
    }
}
