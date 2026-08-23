package factorymethod;

public class ACTicketCreator extends TicketCreator {
    @Override
    public Ticket createTicket() {
        return new ACTicket();
    }
}
