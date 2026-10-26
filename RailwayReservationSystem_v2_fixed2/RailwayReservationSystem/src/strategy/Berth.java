package strategy;

/** One physical berth / seat in a coach. */
public class Berth {
    public static final String LOWER = "LOWER", MIDDLE = "MIDDLE", UPPER = "UPPER",
            SIDE_LOWER = "SIDE_LOWER", SIDE_UPPER = "SIDE_UPPER", SEAT = "SEAT";

    private final String coach;
    private final int number;
    private final String type;

    public Berth(String coach, int number, String type) {
        this.coach = coach;
        this.number = number;
        this.type = type;
    }

    public String getCoach() { return coach; }
    public int getNumber() { return number; }
    public String getType() { return type; }
    public String key() { return coach + "-" + number; }
    public boolean isLower() { return LOWER.equals(type) || SIDE_LOWER.equals(type); }
}
