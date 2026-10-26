package model;

/**
 * Booking is the core domain object. An instance of this class (not a String)
 * is what gets passed around to Observers, Handlers, and persisted to SQLite.
 */
public class Booking {
    private int pnr;
    private Passenger passenger;
    private String trainName;
    private String source;
    private String destination;
    private String travelClass;   // SLEEPER, AC, GENERAL, TATKAL
    private String paymentMode;   // UPI, CREDIT_CARD, NET_BANKING
    private double fare;
    private String status;        // PENDING, CONFIRMED, WAITLISTED, CANCELLED

    public Booking(Passenger passenger, String trainName, String source, String destination,
                    String travelClass, String paymentMode, double fare) {
        this.passenger = passenger;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.travelClass = travelClass;
        this.paymentMode = paymentMode;
        this.fare = fare;
        this.status = "PENDING";
    }

    public int getPnr() { return pnr; }
    public void setPnr(int pnr) { this.pnr = pnr; }
    public Passenger getPassenger() { return passenger; }
    public String getTrainName() { return trainName; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getTravelClass() { return travelClass; }
    public String getPaymentMode() { return paymentMode; }
    public double getFare() { return fare; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "PNR:" + pnr + " | " + passenger.getName() + " | " + trainName +
               " | " + source + "->" + destination + " | " + travelClass +
               " | Fare:" + fare + " | " + status;
    }
}
