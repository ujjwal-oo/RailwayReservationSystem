package observer;

import model.Booking;
import singleton.DatabaseConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * ConcreteObserver: simulates SMS notification and logs it to SQLite
 * via the same Singleton connection.
 */
public class SMSNotificationObserver implements ReservationObserver {
    @Override
    public void update(Booking booking) {
        String message = "Dear " + booking.getPassenger().getName() +
                ", your ticket PNR " + booking.getPnr() + " on " + booking.getTrainName() +
                " is " + booking.getStatus() + ".";
        System.out.println("[Observer:SMS] -> " + booking.getPassenger().getMobileNumber() + " : " + message);
        logToDb(booking.getPnr(), "SMS", message);
    }

    private void logToDb(int pnr, String channel, String message) {
        Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        String sql = "INSERT INTO notification_log (pnr, channel, message) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pnr);
            ps.setString(2, channel);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[Observer:SMS] Failed to log notification: " + e.getMessage());
        }
    }
}
