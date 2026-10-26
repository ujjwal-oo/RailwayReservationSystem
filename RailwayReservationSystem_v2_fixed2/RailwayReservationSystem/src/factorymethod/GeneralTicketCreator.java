package factorymethod;

public class GeneralTicketCreator extends TicketCreator {
    @Override
    public Ticket createTicket() {
        return new GeneralTicket();
    }
}
