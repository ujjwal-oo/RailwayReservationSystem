package factorymethod;

/**
 * Small helper (Simple Factory) that picks the right ConcreteCreator based on
 * class string. This is NOT the pattern itself -- it's a convenience used by
 * the client (Main) to obtain the correct TicketCreator without a chain of
 * if/else at the call site. The actual Factory Method pattern is the
 * createTicket() override relationship between TicketCreator and its subclasses.
 */
public class TicketCreatorFactory {
    public static TicketCreator getCreator(String travelClass) {
        switch (travelClass.toUpperCase()) {
            case "SLEEPER": return new SleeperTicketCreator();
            case "AC": return new ACTicketCreator();
            case "TATKAL": return new TatkalTicketCreator();
            case "GENERAL":
            default: return new GeneralTicketCreator();
        }
    }
}
