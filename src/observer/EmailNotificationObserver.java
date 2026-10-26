package observer;

import model.Booking;
import singleton.DatabaseConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class EmailNotificationObserver implements ReservationObserver {
    @Override
    public void update(Booking booking) {
        String message = "Booking Confirmation - PNR " + booking.getPnr() + ": " + booking;
        System.out.println("[Observer:Email] -> " + booking.getPassenger().getEmail() + " : " + message);
        logToDb(booking.getPnr(), "EMAIL", message);
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
            System.out.println("[Observer:Email] Failed to log notification: " + e.getMessage());
        }
    }
}
