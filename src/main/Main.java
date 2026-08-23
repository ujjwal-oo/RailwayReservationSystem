package main;

import abstractfactory.*;
import bridge.*;
import factorymethod.*;
import model.Booking;
import model.Passenger;
import observer.*;
import proxy.ReservationService;
import proxy.ReservationServiceProxy;
import singleton.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Client / demo driver for the Railway Reservation System mini project.
 * Exercises all seven design patterns end-to-end against a real SQLite DB.
 */
public class Main {

    public static void main(String[] args) {

        // ---------- 1. SINGLETON ----------
        System.out.println("\n################ SINGLETON DEMO ################");
        DatabaseConnectionManager db1 = DatabaseConnectionManager.getInstance();
        DatabaseConnectionManager db2 = DatabaseConnectionManager.getInstance();
        System.out.println("Same instance? " + (db1 == db2));

        // ---------- OBSERVER setup (used by Proxy on success) ----------
        ReservationSubject subject = new ReservationSubject();
        subject.attach(new PassengerAppObserver());
        subject.attach(new SMSNotificationObserver());
        subject.attach(new EmailNotificationObserver());

        // ---------- PROXY (wraps CHAIN OF RESPONSIBILITY internally) ----------
        System.out.println("\n################ PROXY + CHAIN OF RESPONSIBILITY DEMO ################");
        ReservationService reservationService = new ReservationServiceProxy(subject);

        Passenger p1 = new Passenger("Rohan Deshmukh", 24, "M", "9876543210", "rohan@example.com");
        Booking booking1 = new Booking(p1, "Duronto Express", "Nagpur", "Mumbai", "AC", "UPI", 1200.0);
        reservationService.reserve(booking1);

        Passenger p2 = new Passenger("", -5, "M", "9999999999", "bad@example.com"); // invalid on purpose
        Booking badBooking = new Booking(p2, "Duronto Express", "Nagpur", "Mumbai", "SLEEPER", "UPI", 500.0);
        reservationService.reserve(badBooking); // should be rejected by chain

        Passenger p3 = new Passenger("Sneha Patil", 30, "F", "9123456780", "sneha@example.com");
        Booking booking2 = new Booking(p3, "Nagpur Garib Rath", "Nagpur", "Pune", "SLEEPER", "NET_BANKING", 450.0);
        reservationService.reserve(booking2);

        // ---------- FACTORY METHOD ----------
        System.out.println("\n################ FACTORY METHOD DEMO ################");
        TicketCreator creator1 = TicketCreatorFactory.getCreator(booking1.getTravelClass());
        double finalFare1 = creator1.issueTicket(booking1);
        System.out.println("Final payable for booking1: Rs." + finalFare1);

        TicketCreator creator2 = TicketCreatorFactory.getCreator(booking2.getTravelClass());
        double finalFare2 = creator2.issueTicket(booking2);
        System.out.println("Final payable for booking2: Rs." + finalFare2);

        // ---------- ABSTRACT FACTORY ----------
        System.out.println("\n################ ABSTRACT FACTORY DEMO ################");
        TravelClassFactory acFactory = new ACClassFactory();
        acFactory.createSeatAmenity().describe();
        acFactory.createMealService().serve();

        TravelClassFactory sleeperFactory = new SleeperClassFactory();
        sleeperFactory.createSeatAmenity().describe();
        sleeperFactory.createMealService().serve();

        // ---------- BRIDGE ----------
        System.out.println("\n################ BRIDGE DEMO ################");
        Payment upiPayment = new Payment(new UPIGateway());
        upiPayment.pay(finalFare1);

        Payment fastTrackCardPayment = new FastTrackPayment(new CreditCardGateway());
        fastTrackCardPayment.pay(finalFare2);

        // ---------- Dump DB contents to prove persistence ----------
        System.out.println("\n################ DATABASE CONTENTS (bookings) ################");
        printTable("bookings");
        System.out.println("\n################ DATABASE CONTENTS (notification_log) ################");
        printTable("notification_log");

        DatabaseConnectionManager.getInstance().closeConnection();
    }

    private static void printTable(String tableName) {
        Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)) {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    row.append(rs.getMetaData().getColumnName(i)).append("=").append(rs.getString(i));
                    if (i < columnCount) row.append(" | ");
                }
                System.out.println(row);
            }
        } catch (Exception e) {
            System.out.println("Error reading table " + tableName + ": " + e.getMessage());
        }
    }
}
