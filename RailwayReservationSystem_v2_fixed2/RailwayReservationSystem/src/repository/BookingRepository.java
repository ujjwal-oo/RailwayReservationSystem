package repository;

import model.Booking;
import model.Passenger;
import singleton.DatabaseConnectionManager;
import strategy.Berth;
import strategy.BerthLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads/updates bookings through the Singleton connection. */
public class BookingRepository {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    public Booking findByPnr(int pnr) {
        String sql = "SELECT * FROM bookings WHERE pnr = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Passenger p = new Passenger(rs.getString("passenger_name"), rs.getInt("age"),
                        rs.getString("gender"), rs.getString("mobile"), rs.getString("email"));
                Booking b = new Booking(p, rs.getString("train_name"), rs.getString("source"),
                        rs.getString("destination"), rs.getString("travel_class"),
                        rs.getString("payment_mode"), rs.getDouble("fare"));
                b.setPnr(rs.getInt("pnr"));
                b.setStatus(rs.getString("status"));
                b.setFare(rs.getDouble("fare"));
                double base = rs.getDouble("base_fare");
                b.setBaseFare(base > 0 ? base : rs.getDouble("fare"));
                String fc = rs.getString("fare_category");
                b.setFareCategory(fc == null ? "REGULAR" : fc);
                String jd = rs.getString("journey_date");
                if (jd != null) b.setJourneyDate(jd);
                String bp = rs.getString("berth_preference");
                b.setBerthPreference(bp == null ? "NONE" : bp);
                b.setCoach(rs.getString("coach"));
                b.setBerthNo(rs.getInt("berth_no"));
                b.setBerthType(rs.getString("berth_type"));
                return b;
            }
        } catch (Exception e) {
            System.out.println("[Repository] findByPnr failed: " + e.getMessage());
            return null;
        }
    }

    public int countConfirmed(String train, String date, String cls) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE train_name=? AND journey_date=? " +
                "AND UPPER(travel_class)=UPPER(?) AND status='CONFIRMED'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, train);
            ps.setString(2, date);
            ps.setString(3, cls);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            System.out.println("[Repository] countConfirmed failed: " + e.getMessage());
            return 0;
        }
    }

    /** Keys ("B1-4") of berths held by confirmed bookings, optionally excluding one PNR. */
    public Set<String> takenBerths(String train, String date, String cls, int excludePnr) {
        Set<String> taken = new HashSet<>();
        String sql = "SELECT coach, berth_no FROM bookings WHERE train_name=? AND journey_date=? " +
                "AND UPPER(travel_class)=UPPER(?) AND status='CONFIRMED' AND berth_no > 0 AND pnr <> ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, train);
            ps.setString(2, date);
            ps.setString(3, cls);
            ps.setInt(4, excludePnr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) taken.add(rs.getString(1) + "-" + rs.getInt(2));
            }
        } catch (Exception e) {
            System.out.println("[Repository] takenBerths failed: " + e.getMessage());
        }
        return taken;
    }

    public List<Berth> freeBerths(String train, String date, String cls, int excludePnr) {
        Set<String> taken = takenBerths(train, date, cls, excludePnr);
        List<Berth> free = new ArrayList<>();
        for (Berth b : BerthLayout.forClass(cls)) if (!taken.contains(b.key())) free.add(b);
        return free;
    }

    /** {capacity, available, lowerAvailable} */
    public int[] availability(String train, String date, String cls) {
        List<Berth> free = freeBerths(train, date, cls, 0);
        int lower = 0;
        for (Berth b : free) if (b.isLower()) lower++;
        return new int[]{BerthLayout.capacity(cls), free.size(), lower};
    }

    public void updateState(Booking b) {
        String sql = "UPDATE bookings SET status=?, coach=?, berth_no=?, berth_type=?, fare=? WHERE pnr=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, b.getStatus());
            ps.setString(2, b.getCoach());
            ps.setInt(3, b.getBerthNo());
            ps.setString(4, b.getBerthType());
            ps.setDouble(5, b.getFare());
            ps.setInt(6, b.getPnr());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[Repository] updateState failed: " + e.getMessage());
        }
    }
}
