package builder;

import model.Booking;
import model.Passenger;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * BUILDER PATTERN
 * ---------------
 * A Booking has many fields (passenger, contact, train, route, class,
 * payment, date, fare, berth preference). Instead of a 7+ argument
 * constructor, the Builder lets the caller set them step by step, applies
 * sensible defaults, normalises values (upper-case class, trimmed text) and
 * validates that nothing mandatory is missing before the object is created.
 *
 * Participants:
 *  - Builder  : BookingBuilder
 *  - Product  : model.Booking
 *  - Director : the WebServer / Main code that drives the builder
 */
public class BookingBuilder {

    private static final List<String> BERTH_PREFS =
            Arrays.asList("NONE", "LOWER", "MIDDLE", "UPPER", "SIDE_LOWER", "SIDE_UPPER");

    private String name;
    private int age = -1;
    private String gender = "";
    private String mobile = "";
    private String email = "";
    private String trainName;
    private String source;
    private String destination;
    private String travelClass;
    private String paymentMode = "UPI";
    private String journeyDate;
    private double baseFare = 1500.0;
    private String berthPreference = "NONE";

    public BookingBuilder passenger(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender == null ? "" : gender.trim();
        return this;
    }

    public BookingBuilder contact(String mobile, String email) {
        this.mobile = mobile == null ? "" : mobile.trim();
        this.email = email == null ? "" : email.trim();
        return this;
    }

    public BookingBuilder train(String trainName) { this.trainName = trainName; return this; }

    public BookingBuilder route(String source, String destination) {
        this.source = source;
        this.destination = destination;
        return this;
    }

    public BookingBuilder travelClass(String travelClass) { this.travelClass = travelClass; return this; }
    public BookingBuilder paymentMode(String paymentMode) { this.paymentMode = paymentMode; return this; }
    public BookingBuilder journeyDate(String journeyDate) { this.journeyDate = journeyDate; return this; }
    public BookingBuilder baseFare(double baseFare) { this.baseFare = baseFare; return this; }

    public BookingBuilder berthPreference(String pref) {
        this.berthPreference = (pref == null || pref.trim().isEmpty()) ? "NONE" : pref.trim().toUpperCase();
        return this;
    }

    /** Validates and assembles the finished Booking. */
    public Booking build() {
        require(name, "Passenger name");
        if (age < 0) throw new IllegalStateException("Age is required");
        require(trainName, "Train");
        require(source, "Source");
        require(destination, "Destination");
        require(travelClass, "Travel class");
        if (!BERTH_PREFS.contains(berthPreference)) {
            throw new IllegalStateException("Unknown berth preference: " + berthPreference);
        }

        Passenger p = new Passenger(name.trim(), age, gender, mobile, email);
        Booking b = new Booking(p, trainName.trim(), source.trim(), destination.trim(),
                travelClass.trim().toUpperCase(), paymentMode.trim().toUpperCase(), baseFare);
        b.setBerthPreference(berthPreference);
        b.setJourneyDate(journeyDate == null || journeyDate.trim().isEmpty()
                ? LocalDate.now().toString() : journeyDate.trim());
        return b;
    }

    private static void require(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(label + " is required");
        }
    }
}
