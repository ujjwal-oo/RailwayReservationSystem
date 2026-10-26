package factorymethod;

public class SleeperTicketCreator extends TicketCreator {
    @Override
    public Ticket createTicket() {
        return new SleeperTicket();
    }
}
