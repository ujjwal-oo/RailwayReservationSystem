package model;

import memento.BookingMemento;
import prototype.Prototype;

import java.time.LocalDate;

/**
 * Booking is the core domain object. An instance of this class (not a String)
 * is what gets passed around to Observers, Handlers, and persisted to SQLite.
 *
 * It plays two extra roles in the newer patterns:
 *  - PROTOTYPE  : copy() produces a fresh, un-booked clone (used for
 *                 "return journey" / "same journey for another passenger").
 *  - MEMENTO    : ORIGINATOR - createMemento()/restoreFromMemento() snapshot
 *                 and restore the mutable booking state (status + berth + fare).
 */
public class Booking implements Prototype<Booking> {

    private int pnr;
    private Passenger passenger;
    private String trainName;
    private String source;
    private String destination;
    private String travelClass;   // SLEEPER, AC, GENERAL, TATKAL
    private String paymentMode;   // UPI, CREDIT_CARD, NET_BANKING
    private String journeyDate;   // yyyy-MM-dd
    private double baseFare;      // fare before any concession / surcharge
    private double fare;          // final payable fare (set by a FareStrategy)
    private String fareCategory;  // REGULAR, SENIOR_CITIZEN, CHILD, TATKAL
    private String berthPreference; // NONE, LOWER, MIDDLE, UPPER, SIDE_LOWER, SIDE_UPPER
    private String coach;         // e.g. B1, S2
    private int berthNo;          // 0 = not allotted
    private String berthType;     // LOWER, MIDDLE, UPPER, SIDE_LOWER, SIDE_UPPER, SEAT
    private String status;        // PENDING, CONFIRMED, WAITLISTED, CANCELLED

    private transient String failureReason; // why a request was rejected (not persisted)

    public Booking(Passenger passenger, String trainName, String source, String destination,
                   String travelClass, String paymentMode, double fare) {
        this.passenger = passenger;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.travelClass = travelClass;
        this.paymentMode = paymentMode;
        this.baseFare = fare;
        this.fare = fare;
        this.fareCategory = "REGULAR";
        this.berthPreference = "NONE";
        this.journeyDate = LocalDate.now().toString();
        this.status = "PENDING";
    }

    // ---------------- PROTOTYPE ----------------
    /** Deep copy that is "un-booked": no PNR, no berth, status PENDING. */
    @Override
    public Booking copy() {
        Booking c = new Booking(passenger.copy(), trainName, source, destination,
                travelClass, paymentMode, baseFare);
        c.journeyDate = journeyDate;
        c.berthPreference = berthPreference;
        return c;
    }

    // ---------------- MEMENTO (Originator) ----------------
    public BookingMemento createMemento(String label) {
        return new BookingMemento(label, status, coach, berthNo, berthType, fare);
    }

    public void restoreFromMemento(BookingMemento m) {
        this.status = m.getStatus();
        this.coach = m.getCoach();
        this.berthNo = m.getBerthNo();
        this.berthType = m.getBerthType();
        this.fare = m.getFare();
    }

    public void clearBerth() {
        this.coach = null;
        this.berthNo = 0;
        this.berthType = null;
    }

    public boolean hasBerth() { return coach != null && berthNo > 0; }

    // ---------------- getters / setters ----------------
    public int getPnr() { return pnr; }
    public void setPnr(int pnr) { this.pnr = pnr; }
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
    public String getTrainName() { return trainName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getTravelClass() { return travelClass; }
    public String getPaymentMode() { return paymentMode; }
    public String getJourneyDate() { return journeyDate; }
    public void setJourneyDate(String journeyDate) { this.journeyDate = journeyDate; }
    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
    public String getFareCategory() { return fareCategory; }
    public void setFareCategory(String fareCategory) { this.fareCategory = fareCategory; }
    public String getBerthPreference() { return berthPreference; }
    public void setBerthPreference(String berthPreference) { this.berthPreference = berthPreference; }
    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }
    public int getBerthNo() { return berthNo; }
    public void setBerthNo(int berthNo) { this.berthNo = berthNo; }
    public String getBerthType() { return berthType; }
    public void setBerthType(String berthType) { this.berthType = berthType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getSeatLabel() {
        return hasBerth() ? coach + "-" + berthNo + " (" + berthType + ")" : "Not allotted";
    }

    @Override
    public String toString() {
        return "PNR:" + pnr + " | " + passenger.getName() + " | " + trainName +
               " | " + source + "->" + destination + " | " + travelClass +
               " | " + getSeatLabel() +
               " | Fare:" + fare + " | " + status;
    }
}
