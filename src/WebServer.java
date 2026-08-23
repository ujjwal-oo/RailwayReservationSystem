import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import singleton.DatabaseConnectionManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebServer {

    private static final int PORT = 8081;

    // Your frontend folder
    private static final String FRONTEND_FOLDER = "frontend";

    public static void main(String[] args) throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(8081),
                        0
                );


        // ==========================================
        // FRONTEND
        // ==========================================

        server.createContext("/", WebServer::serveFrontend);


        // ==========================================
        // BOOKING API
        // ==========================================

        server.createContext(
                "/api/book",
                WebServer::bookTicket
        );


        server.setExecutor(null);


        System.out.println();
        System.out.println("=================================");
        System.out.println("Railway Reservation Server Started");
        System.out.println("Open: http://127.0.0.1:8081");
        System.out.println("=================================");
        System.out.println();


        server.start();
    }


    // =====================================================
    // SERVE HTML / CSS / JS FILES
    // =====================================================

    private static void serveFrontend(
            HttpExchange exchange
    ) throws IOException {

        String requestPath =
                exchange.getRequestURI().getPath();


        // "/" means index.html

        if (requestPath.equals("/")) {

            requestPath = "/index.html";

        }


        // Remove first /

        String fileName =
                requestPath.substring(1);


        // Prevent access outside frontend folder

        if (fileName.contains("..")) {

            send404(exchange);

            return;
        }


        Path filePath =
                Paths.get(
                        FRONTEND_FOLDER,
                        fileName
                );


        File file =
                filePath.toFile();


        if (!file.exists() || !file.isFile()) {

            send404(exchange);

            return;
        }


        byte[] content =
                Files.readAllBytes(filePath);


        String contentType =
                getContentType(fileName);


        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType
                );


        exchange.sendResponseHeaders(
                200,
                content.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(content);

        output.close();
    }


    // =====================================================
    // CONTENT TYPE
    // =====================================================

    private static String getContentType(
            String fileName
    ) {

        String lower =
                fileName.toLowerCase();


        if (lower.endsWith(".html")) {

            return "text/html; charset=UTF-8";
        }


        if (lower.endsWith(".css")) {

            return "text/css; charset=UTF-8";
        }


        if (lower.endsWith(".js")) {

            return "application/javascript; charset=UTF-8";
        }


        if (lower.endsWith(".json")) {

            return "application/json; charset=UTF-8";
        }


        if (lower.endsWith(".png")) {

            return "image/png";
        }


        if (lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg")) {

            return "image/jpeg";
        }


        if (lower.endsWith(".svg")) {

            return "image/svg+xml";
        }


        if (lower.endsWith(".ico")) {

            return "image/x-icon";
        }


        return "application/octet-stream";
    }


    // =====================================================
    // BOOK TICKET API
    // =====================================================

    private static void bookTicket(
            HttpExchange exchange
    ) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendJson(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST required\"}"
            );

            return;
        }


        try {

            // ------------------------------------------
            // Read JSON
            // ------------------------------------------

            String json =
                    new String(
                            exchange.getRequestBody()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );


            System.out.println();
            System.out.println("========== NEW BOOKING ==========");
            System.out.println(json);


            // ------------------------------------------
            // Extract values
            // ------------------------------------------

            String passengerName =
                    getJsonValue(
                            json,
                            "passenger_name"
                    );


            String ageString =
                    getJsonValue(
                            json,
                            "age"
                    );


            String gender =
                    getJsonValue(
                            json,
                            "gender"
                    );


            String mobile =
                    getJsonValue(
                            json,
                            "mobile"
                    );


            String email =
                    getJsonValue(
                            json,
                            "email"
                    );


            String trainName =
                    getJsonValue(
                            json,
                            "train_name"
                    );


            String source =
                    getJsonValue(
                            json,
                            "source"
                    );


            String destination =
                    getJsonValue(
                            json,
                            "destination"
                    );


            String travelClass =
                    getJsonValue(
                            json,
                            "travel_class"
                    );


            String paymentMode =
                    getJsonValue(
                            json,
                            "payment_mode"
                    );


            String fareString =
                    getJsonValue(
                            json,
                            "fare"
                    );


            // ------------------------------------------
            // Validation
            // ------------------------------------------

            if (passengerName.isEmpty()) {

                sendJson(
                        exchange,
                        400,
                        "{\"success\":false,\"message\":\"Passenger name required\"}"
                );

                return;
            }


            if (ageString.isEmpty()) {

                sendJson(
                        exchange,
                        400,
                        "{\"success\":false,\"message\":\"Age required\"}"
                );

                return;
            }


            int age =
                    Integer.parseInt(ageString);


            double fare =
                    fareString.isEmpty()
                            ? 1500
                            : Double.parseDouble(fareString);


            // ------------------------------------------
            // DATABASE
            // ------------------------------------------

            DatabaseConnectionManager db =
                    DatabaseConnectionManager
                            .getInstance();


            Connection connection =
                    db.getConnection();


            String sql =
                    "INSERT INTO bookings " +
                            "(passenger_name, age, gender, mobile, email, " +
                            "train_name, source, destination, travel_class, " +
                            "payment_mode, fare, status) " +

                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


            PreparedStatement statement =
                    connection.prepareStatement(
                            sql,
                            java.sql.Statement.RETURN_GENERATED_KEYS
                    );


            statement.setString(
                    1,
                    passengerName
            );


            statement.setInt(
                    2,
                    age
            );


            statement.setString(
                    3,
                    gender
            );


            statement.setString(
                    4,
                    mobile
            );


            statement.setString(
                    5,
                    email
            );


            statement.setString(
                    6,
                    trainName
            );


            statement.setString(
                    7,
                    source
            );


            statement.setString(
                    8,
                    destination
            );


            statement.setString(
                    9,
                    travelClass
            );


            statement.setString(
                    10,
                    paymentMode
            );


            statement.setDouble(
                    11,
                    fare
            );


            statement.setString(
                    12,
                    "CONFIRMED"
            );


            statement.executeUpdate();


            // ------------------------------------------
            // GET PNR
            // ------------------------------------------

            int pnr = 0;


            ResultSet keys =
                    statement.getGeneratedKeys();


            if (keys.next()) {

                pnr =
                        keys.getInt(1);
            }


            statement.close();


            System.out.println(
                    "Booking successful!"
            );


            System.out.println(
                    "PNR: " + pnr
            );


            System.out.println(
                    "================================"
            );


            // ------------------------------------------
            // RESPONSE
            // ------------------------------------------

            String response =
                    "{"
                            + "\"success\":true,"
                            + "\"pnr\":" + pnr + ","
                            + "\"message\":\"Booking successful\""
                            + "}";


            sendJson(
                    exchange,
                    200,
                    response
            );


        } catch (Exception e) {

            e.printStackTrace();


            String response =
                    "{"
                            + "\"success\":false,"
                            + "\"message\":\""
                            + escapeJson(e.getMessage())
                            + "\""
                            + "}";


            sendJson(
                    exchange,
                    500,
                    response
            );
        }
    }


    // =====================================================
    // SIMPLE JSON VALUE READER
    // =====================================================

    private static String getJsonValue(
            String json,
            String key
    ) {

        String search =
                "\"" + key + "\"";


        int keyPosition =
                json.indexOf(search);


        if (keyPosition == -1) {

            return "";
        }


        int colon =
                json.indexOf(
                        ":",
                        keyPosition
                );


        if (colon == -1) {

            return "";
        }


        int start =
                colon + 1;


        while (
                start < json.length()
                        &&
                        Character.isWhitespace(
                                json.charAt(start)
                        )
        ) {

            start++;
        }


        // String value

        if (
                start < json.length()
                        &&
                        json.charAt(start) == '"'
        ) {

            start++;


            StringBuilder value =
                    new StringBuilder();


            boolean escaped = false;


            for (
                    int i = start;
                    i < json.length();
                    i++
            ) {

                char c =
                        json.charAt(i);


                if (escaped) {

                    value.append(c);

                    escaped = false;

                } else if (c == '\\') {

                    escaped = true;

                } else if (c == '"') {

                    break;

                } else {

                    value.append(c);
                }
            }


            return value.toString();
        }


        // Number / boolean

        int end =
                start;


        while (
                end < json.length()
                        &&
                        json.charAt(end) != ','
                        &&
                        json.charAt(end) != '}'
        ) {

            end++;
        }


        return json.substring(
                start,
                end
        ).trim();
    }


    // =====================================================
    // JSON RESPONSE
    // =====================================================

    private static void sendJson(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        byte[] data =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );


        exchange.sendResponseHeaders(
                status,
                data.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(data);

        output.close();
    }


    // =====================================================
    // 404
    // =====================================================

    private static void send404(
            HttpExchange exchange
    ) throws IOException {

        String response =
                "404 - File Not Found: "
                        +
                        exchange.getRequestURI()
                                .getPath();


        byte[] data =
                response.getBytes(
                        StandardCharsets.UTF_8
                );


        exchange.sendResponseHeaders(
                404,
                data.length
        );


        OutputStream output =
                exchange.getResponseBody();


        output.write(data);

        output.close();
    }


    // =====================================================
    // ESCAPE JSON
    // =====================================================

    private static String escapeJson(
            String value
    ) {

        if (value == null) {

            return "Unknown error";
        }


        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}