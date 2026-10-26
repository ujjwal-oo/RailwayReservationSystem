package memento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MEMENTO PATTERN - Memento
 * Immutable snapshot of the mutable part of a Booking (status, berth, fare).
 * Only the Originator (Booking) reads it back; the Caretaker just stores it.
 */
public final class BookingMemento {
    private final String label;
    private final String status;
    private final String coach;
    private final int berthNo;
    private final String berthType;
    private final double fare;
    private final String savedAt;

    public BookingMemento(String label, String status, String coach, int berthNo,
                          String berthType, double fare) {
        this.label = label;
        this.status = status;
        this.coach = coach;
        this.berthNo = berthNo;
        this.berthType = berthType;
        this.fare = fare;
        this.savedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM HH:mm:ss"));
    }

    public String getLabel() { return label; }
    public String getStatus() { return status; }
    public String getCoach() { return coach; }
    public int getBerthNo() { return berthNo; }
    public String getBerthType() { return berthType; }
    public double getFare() { return fare; }
    public String getSavedAt() { return savedAt; }
}
