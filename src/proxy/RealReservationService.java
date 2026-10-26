package proxy;

import model.Booking;
import singleton.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * RealSubject: performs the actual reservation logic and persists the
 * booking into the SQLite database via the Singleton connection manager.
 */
public class RealReservationService implements ReservationService {

    @Override
    public boolean reserve(Booking booking) {
        Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        String sql = "INSERT INTO bookings (passenger_name, age, gender, mobile, email, " +
                "train_name, source, destination, travel_class, payment_mode, fare, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getPassenger().getName());
            ps.setInt(2, booking.getPassenger().getAge());
            ps.setString(3, booking.getPassenger().getGender());
            ps.setString(4, booking.getPassenger().getMobileNumber());
            ps.setString(5, booking.getPassenger().getEmail());
            ps.setString(6, booking.getTrainName());
            ps.setString(7, booking.getSource());
            ps.setString(8, booking.getDestination());
            ps.setString(9, booking.getTravelClass());
            ps.setString(10, booking.getPaymentMode());
            ps.setDouble(11, booking.getFare());
            if (booking.getStatus().equals("PENDING")) {
                booking.setStatus("CONFIRMED");
            }
            ps.setString(12, booking.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    booking.setPnr(rs.getInt(1));
                }
            }
            System.out.println("[RealReservationService] Booking persisted to railway.db with PNR " + booking.getPnr());
            return true;
        } catch (Exception e) {
            System.out.println("[RealReservationService] Failed to persist booking: " + e.getMessage());
            return false;
        }
    }
}
