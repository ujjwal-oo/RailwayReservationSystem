package singleton;

import java.sql.*;

/**
 * SINGLETON PATTERN
 * ------------------
 * Ensures exactly ONE connection manager (and one underlying SQLite connection)
 * exists for the entire application. Every other pattern (Proxy, Observer,
 * Abstract Factory, Bridge) reads/writes through this single instance,
 * avoiding conflicting or duplicate connections to railway.db.
 *
 * Participants:
 *  - Singleton class: DatabaseConnectionManager
 *  - Private constructor + static getInstance() + private static instance field
 */
public class DatabaseConnectionManager {

    private static DatabaseConnectionManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:railway.db";

    // Private constructor prevents external instantiation
    private DatabaseConnectionManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("[Singleton] New SQLite connection created -> railway.db");
            initializeSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // Thread-safe lazy initialization (double-checked locking)
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "pnr INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "passenger_name TEXT," +
                    "age INTEGER," +
                    "gender TEXT," +
                    "mobile TEXT," +
                    "email TEXT," +
                    "train_name TEXT," +
                    "source TEXT," +
                    "destination TEXT," +
                    "travel_class TEXT," +
                    "payment_mode TEXT," +
                    "fare REAL," +
                    "status TEXT," +
                    "journey_date TEXT," +
                    "berth_preference TEXT," +
                    "coach TEXT," +
                    "berth_no INTEGER DEFAULT 0," +
                    "berth_type TEXT," +
                    "base_fare REAL," +
                    "fare_category TEXT" +
                    ")");

            // Migrate databases created by the older version of the project
            String[][] newColumns = {
                    {"journey_date", "TEXT"}, {"berth_preference", "TEXT"}, {"coach", "TEXT"},
                    {"berth_no", "INTEGER DEFAULT 0"}, {"berth_type", "TEXT"},
                    {"base_fare", "REAL"}, {"fare_category", "TEXT"}
            };
            java.util.Set<String> existing = new java.util.HashSet<>();
            try (ResultSet rs = st.executeQuery("PRAGMA table_info(bookings)")) {
                while (rs.next()) existing.add(rs.getString("name"));
            }
            for (String[] col : newColumns) {
                if (!existing.contains(col[0])) {
                    st.execute("ALTER TABLE bookings ADD COLUMN " + col[0] + " " + col[1]);
                    System.out.println("[Singleton] Migrated: added column " + col[0]);
                }
            }

            st.execute("CREATE TABLE IF NOT EXISTS notification_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pnr INTEGER," +
                    "channel TEXT," +
                    "message TEXT," +
                    "sent_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")");
        }
        System.out.println("[Singleton] Schema verified/created.");
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[Singleton] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
