
import java.sql.*;
import java.util.ArrayList;

public class CheckDatabase {

    public static void main(String[] args) {

        // Use the railway database
        String dbPath = new java.io.File("railway.db").getAbsolutePath();
        String url = "jdbc:sqlite:" + dbPath;

        System.out.println("Database location: " + dbPath);
        try {
            // Explicitly load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(url)) {

                System.out.println();
                System.out.println("==============================================================");
                System.out.println("                 RAILWAY RESERVATION SYSTEM");
                System.out.println("                    DATABASE REPORT");
                System.out.println("==============================================================");

                // Find all tables
                String tableQuery =
                        "SELECT name FROM sqlite_master " +
                                "WHERE type='table' AND name NOT LIKE 'sqlite_%' " +
                                "ORDER BY name";

                try (Statement stmt = conn.createStatement();
                     ResultSet tables = stmt.executeQuery(tableQuery)) {

                    boolean foundTable = false;

                    while (tables.next()) {

                        foundTable = true;
                        String tableName = tables.getString("name");

                        System.out.println();
                        System.out.println("==============================================================");
                        System.out.println(" TABLE: " + tableName.toUpperCase());
                        System.out.println("==============================================================");

                        displayTable(conn, tableName);
                    }

                    if (!foundTable) {
                        System.out.println();
                        System.out.println("No tables found in railway.db");
                        System.out.println("Check whether this is the correct database file.");
                    }
                }

                System.out.println();
                System.out.println("==============================================================");
                System.out.println("                  DATABASE CHECK COMPLETE");
                System.out.println("==============================================================");
                System.out.println();
            }

        } catch (ClassNotFoundException e) {

            System.out.println("ERROR: SQLite JDBC driver is missing from the runtime classpath.");
            System.out.println("Check Project Structure > Modules > Dependencies.");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("DATABASE ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Display one complete table
    private static void displayTable(Connection conn, String tableName)
            throws SQLException {

        // Quote the table name to handle unusual names safely
        String dataQuery = "SELECT * FROM \"" +
                tableName.replace("\"", "\"\"") + "\"";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(dataQuery)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            String[] columnNames = new String[columnCount];

            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = meta.getColumnName(i + 1);
            }

            // Store table data
            ArrayList<String[]> rows = new ArrayList<>();

            while (rs.next()) {

                String[] row = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {

                    String value = rs.getString(i + 1);
                    row[i] = (value == null) ? "NULL" : value;
                }

                rows.add(row);
            }

            // Calculate the width of every column
            int[] widths = new int[columnCount];

            for (int i = 0; i < columnCount; i++) {

                widths[i] = columnNames[i].length();

                for (String[] row : rows) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }

                widths[i] += 2;
            }

            // Print column headings
            printSeparator(widths);

            System.out.print("|");

            for (int i = 0; i < columnCount; i++) {
                System.out.printf(
                        " %-" + (widths[i] - 1) + "s|",
                        columnNames[i].toUpperCase()
                );
            }

            System.out.println();
            printSeparator(widths);

            // Print rows
            if (rows.isEmpty()) {

                System.out.println("No records found.");

            } else {

                for (String[] row : rows) {

                    System.out.print("|");

                    for (int i = 0; i < columnCount; i++) {
                        System.out.printf(
                                " %-" + (widths[i] - 1) + "s|",
                                row[i]
                        );
                    }

                    System.out.println();
                }
            }

            printSeparator(widths);
            System.out.println("Total records: " + rows.size());
        }
    }

    // Print table separator
    private static void printSeparator(int[] widths) {

        System.out.print("+");

        for (int width : widths) {
            System.out.print("-".repeat(width));
            System.out.print("+");
        }

        System.out.println();
    }
}

