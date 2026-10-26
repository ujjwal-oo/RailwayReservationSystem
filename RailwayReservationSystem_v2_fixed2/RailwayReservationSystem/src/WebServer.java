import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import builder.BookingBuilder;
import mediator.BookingContext;
import mediator.RailwayBookingMediator;
import memento.BookingMemento;
import model.Booking;
import model.Passenger;
import repository.BookingRepository;
import strategy.BerthAllocator;
import strategy.FareContext;
import strategy.FareStrategy;
import util.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;

public class WebServer {

    private static final int PORT = 8080;

    // FIX: Explicit Railway frontend path.
    private static final Path FRONTEND_DIRECTORY = Paths.get(
            "C:\\Downloads\\RailwayReservationSystem_live_trains_modified"
                    + "\\RailwayReservationSystem_v2_fixed2"
                    + "\\RailwayReservationSystem\\frontend"
    ).toAbsolutePath().normalize();

    private static Path resolveFrontendDirectory() {

        Path indexFile = FRONTEND_DIRECTORY.resolve("index.html");

        if (!Files.isDirectory(FRONTEND_DIRECTORY)) {
            throw new IllegalStateException(
                    "Railway frontend directory not found: "
                            + FRONTEND_DIRECTORY
            );
        }

        if (!Files.isRegularFile(indexFile)) {
            throw new IllegalStateException(
                    "Railway index.html not found: " + indexFile
            );
        }

        return FRONTEND_DIRECTORY;
    }

    private static boolean hasHomepage(Path directory) {
        return Files.isDirectory(directory)
                && Files.isRegularFile(directory.resolve("index.html"));
    }

    private static final RailwayBookingMediator mediator =
            new RailwayBookingMediator();

    private static final BookingRepository repository =
            mediator.getRepository();

    private static final FareContext fareContext = new FareContext();

    private static final BerthAllocator berthAllocator =
            new BerthAllocator();

    public static void main(String[] args) throws Exception {

        Path frontendDirectory = resolveFrontendDirectory();

        System.out.println("Frontend directory: " + frontendDirectory);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT), 0
        );

        server.createContext("/", ex ->
                serveFrontend(ex, frontendDirectory));

        server.createContext("/api/quote",
                ex -> api(ex, "GET", WebServer::quote));

        server.createContext("/api/trains",
                ex -> api(ex, "GET", WebServer::liveTrains));

        server.createContext("/api/availability",
                ex -> api(ex, "GET", WebServer::availability));

        server.createContext("/api/book",
                ex -> api(ex, "POST", WebServer::book));

        server.createContext("/api/ticket",
                ex -> api(ex, "GET", WebServer::ticket));

        server.createContext("/api/cancel",
                ex -> api(ex, "POST", WebServer::cancel));

        server.createContext("/api/undo",
                ex -> api(ex, "POST", WebServer::undo));

        server.createContext("/api/rebook",
                ex -> api(ex, "POST", WebServer::rebook));

        server.setExecutor(null);

        System.out.println();
        System.out.println("=================================");
        System.out.println(" Railway Reservation Server");
        System.out.println("=================================");
        System.out.println("Open: http://127.0.0.1:" + PORT);
        System.out.println("=================================");

        server.start();
    }

    // =====================================================
    // API plumbing
    // =====================================================

    private interface Handler {
        void handle(HttpExchange ex) throws Exception;
    }

    private static void api(
            HttpExchange ex,
            String method,
            Handler handler
    ) throws IOException {

        if (!ex.getRequestMethod().equalsIgnoreCase(method)) {
            sendJson(ex, 405, error(method + " required"));
            return;
        }

        try {
            handler.handle(ex);

        } catch (IllegalStateException | NumberFormatException e) {

            sendJson(
                    ex,
                    400,
                    error(e.getMessage() == null
                            ? "Invalid input"
                            : e.getMessage())
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(
                    ex,
                    500,
                    error(e.getMessage() == null
                            ? "Unknown server error"
                            : e.getMessage())
            );
        }
    }

    private static String error(String message) {
        return Json.obj()
                .put("success", false)
                .put("message", message)
                .toString();
    }

    private static String body(HttpExchange ex) throws IOException {
        return new String(
                ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private static Map<String, String> query(HttpExchange ex) {

        Map<String, String> map = new HashMap<>();

        String q = ex.getRequestURI().getRawQuery();

        if (q == null) {
            return map;
        }

        for (String pair : q.split("&")) {

            int i = pair.indexOf('=');

            if (i < 0) {
                continue;
            }

            map.put(
                    URLDecoder.decode(
                            pair.substring(0, i),
                            StandardCharsets.UTF_8
                    ),
                    URLDecoder.decode(
                            pair.substring(i + 1),
                            StandardCharsets.UTF_8
                    )
            );
        }

        return map;
    }

    private static int intOf(String s, String label) {

        try {
            return Integer.parseInt(s.trim());

        } catch (Exception e) {
            throw new IllegalStateException(
                    label + " must be a number"
            );
        }
    }

    private static double doubleOr(String s, double fallback) {

        try {
            return s == null || s.trim().isEmpty()
                    ? fallback
                    : Double.parseDouble(s.trim());

        } catch (Exception e) {
            return fallback;
        }
    }

    // =====================================================
    // Live train API
    // =====================================================

    private static final HttpClient RAIL_API_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

    private static final String RAIL_API_BASE =
            "https://api.railradar.in/v1";

    private static void liveTrains(HttpExchange ex) throws Exception {

        Map<String, String> q = query(ex);

        String from = q.getOrDefault("from", "").trim();
        String to = q.getOrDefault("to", "").trim();
        String date = q.getOrDefault("date", "").trim();

        if (from.isEmpty() || to.isEmpty() || date.isEmpty()) {

            sendJson(
                    ex,
                    400,
                    error("Enter source, destination and journey date.")
            );

            return;
        }

        String apiKey = System.getenv("RAILRADAR_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {

            sendJson(
                    ex,
                    503,
                    Json.obj()
                            .put("success", false)
                            .put(
                                    "message",
                                    "Live train search is not configured yet. "
                                            + "Set the RAILRADAR_API_KEY environment variable."
                            )
                            .toString()
            );

            return;
        }

        try {

            String fromCode = stationCode(from, apiKey);
            String toCode = stationCode(to, apiKey);

            if (fromCode == null || toCode == null) {

                sendJson(
                        ex,
                        200,
                        Json.obj()
                                .put("success", true)
                                .raw("trains", "[]")
                                .put(
                                        "message",
                                        "Station not found. Try a station name or station code."
                                )
                                .toString()
                );

                return;
            }

            String url = RAIL_API_BASE
                    + "/trains/between/"
                    + encPath(fromCode)
                    + "/"
                    + encPath(toCode)
                    + "?date="
                    + enc(date)
                    + "&live=true";

            HttpResponse<String> response = railGet(url, apiKey);

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                sendJson(
                        ex,
                        502,
                        Json.obj()
                                .put(
                                        "success",
                                        false
                                )
                                .put(
                                        "message",
                                        friendlyApiMessage(
                                                response.statusCode()
                                        )
                                )
                                .toString()
                );

                return;
            }

            String raw = response.body();

            String trainArray = extractJsonArray(raw, "trains");

            if (trainArray == null) {
                trainArray = "[]";
            }

            sendJson(
                    ex,
                    200,
                    Json.obj()
                            .put("success", true)
                            .put("fromCode", fromCode)
                            .put("toCode", toCode)
                            .raw("trains", trainArray)
                            .put("source", "RailRadar")
                            .toString()
            );

        } catch (java.net.http.HttpTimeoutException e) {

            sendJson(
                    ex,
                    504,
                    error(
                            "Live train service took too long. Please try again."
                    )
            );

        } catch (Exception e) {

            System.err.println(
                    "Live train lookup failed: " + e.getMessage()
            );

            sendJson(
                    ex,
                    502,
                    error(
                            "Live train service is temporarily unavailable. "
                                    + "Please try again later."
                    )
            );
        }
    }

    private static String stationCode(
            String name,
            String apiKey
    ) throws Exception {

        String url = RAIL_API_BASE
                + "/lookup/search/stations?q="
                + enc(name)
                + "&limit=10";

        HttpResponse<String> response = railGet(url, apiKey);

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            return null;
        }

        String raw = response.body();

        Matcher matcher = Pattern.compile(
                "\\{[^{}]*\"code\"\\s*:\\s*\"([^\"]+)\""
                        + "[^{}]*\"name\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}"
        ).matcher(raw);

        String firstCode = null;

        while (matcher.find()) {

            String code = matcher.group(1);
            String stationName = matcher.group(2);

            if (stationName.equalsIgnoreCase(name)
                    || code.equalsIgnoreCase(name)) {
                return code;
            }

            if (firstCode == null) {
                firstCode = code;
            }
        }

        return firstCode;
    }

    private static HttpResponse<String> railGet(
            String url,
            String apiKey
    ) throws Exception {

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(url)
                )
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        return RAIL_API_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static String extractJsonArray(
            String json,
            String key
    ) {

        Matcher keyMatcher = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\\["
        ).matcher(json);

        if (!keyMatcher.find()) {
            return null;
        }

        int start = json.indexOf('[', keyMatcher.start());

        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {

            char c = json.charAt(i);

            if (quoted) {

                if (escaped) {
                    escaped = false;

                } else if (c == '\\') {
                    escaped = true;

                } else if (c == '"') {
                    quoted = false;
                }

                continue;
            }

            if (c == '"') {
                quoted = true;

            } else if (c == '[') {
                depth++;

            } else if (c == ']' && --depth == 0) {
                return json.substring(start, i + 1);
            }
        }

        return null;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String encPath(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "");
    }

    private static String friendlyApiMessage(int status) {

        if (status == 401) {
            return "Live train API key is invalid. Check RAILRADAR_API_KEY.";
        }

        if (status == 429) {
            return "Live train search limit reached. Please try later.";
        }

        if (status == 404) {
            return "No train route was found for these stations and date.";
        }

        if (status == 503) {
            return "Live train provider is temporarily unavailable. Please try later.";
        }

        return "Unable to load live trains right now. Please try again.";
    }

    // =====================================================
    // Fare quote
    // =====================================================

    private static void quote(HttpExchange ex) throws Exception {

        Map<String, String> q = query(ex);

        int age = intOf(q.getOrDefault("age", ""), "Age");

        String cls = q.getOrDefault("class", "");

        double base = doubleOr(q.get("fare"), 1500);

        String pref = q.getOrDefault("preference", "NONE");

        FareStrategy fs = fareContext.select(age, cls);

        String policy;
        String text;

        if (age >= FareContext.SENIOR_AGE) {

            policy = "LOWER_PRIORITY";

            text = "Senior citizen: a lower berth will be allotted if available.";

        } else if (!pref.isEmpty() && !pref.equals("NONE")) {

            policy = "PREFERENCE";

            text = "We will try to give you a "
                    + pref.replace('_', ' ').toLowerCase()
                    + " berth.";

        } else {

            policy = "STANDARD";

            text = "Berth will be allotted automatically.";
        }

        sendJson(
                ex,
                200,
                Json.obj()
                        .put("success", true)
                        .put("fare_category", fs.category())
                        .put("discount_percent", fs.discountPercent())
                        .put("base_fare", base)
                        .put("final_fare", fs.calculate(base))
                        .put("berth_policy", policy)
                        .put("berth_message", text)
                        .toString()
        );
    }

    // =====================================================
    // Availability
    // =====================================================

    private static void availability(HttpExchange ex) throws Exception {

        Map<String, String> q = query(ex);

        int[] a = repository.availability(
                q.getOrDefault("train", ""),
                q.getOrDefault("date", ""),
                q.getOrDefault("class", "")
        );

        sendJson(
                ex,
                200,
                Json.obj()
                        .put("success", true)
                        .put("capacity", a[0])
                        .put("available", a[1])
                        .put("lower_available", a[2])
                        .put("waitlist", a[0] > 0 && a[1] == 0)
                        .toString()
        );
    }

    // =====================================================
    // Booking
    // =====================================================

    private static void book(HttpExchange ex) throws Exception {

        String json = body(ex);

        System.out.println(
                "\n========== NEW WEB BOOKING ==========\n"
                        + json
        );

        Booking booking = new BookingBuilder()
                .passenger(
                        Json.get(json, "passenger_name"),
                        intOf(Json.get(json, "age"), "Age"),
                        Json.get(json, "gender")
                )
                .contact(
                        Json.get(json, "mobile"),
                        Json.get(json, "email")
                )
                .train(Json.get(json, "train_name"))
                .route(
                        Json.get(json, "source"),
                        Json.get(json, "destination")
                )
                .travelClass(Json.get(json, "travel_class"))
                .paymentMode(Json.get(json, "payment_mode"))
                .journeyDate(Json.get(json, "journey_date"))
                .baseFare(
                        doubleOr(Json.get(json, "fare"), 1500.0)
                )
                .berthPreference(
                        Json.get(json, "berth_preference")
                )
                .build();

        respond(ex, mediator.book(booking), "Booking successful");
    }

    // =====================================================
    // Ticket
    // =====================================================

    private static void ticket(HttpExchange ex) throws Exception {

        int pnr = intOf(
                query(ex).getOrDefault("pnr", ""),
                "PNR"
        );

        Booking b = repository.findByPnr(pnr);

        if (b == null) {
            sendJson(ex, 404, error("PNR not found"));
            return;
        }

        sendJson(
                ex,
                200,
                ticketJson(b, true)
                        .put("success", true)
                        .toString()
        );
    }

    // =====================================================
    // Cancel booking
    // =====================================================

    private static void cancel(HttpExchange ex) throws Exception {

        int pnr = intOf(
                Json.get(body(ex), "pnr"),
                "PNR"
        );

        respond(
                ex,
                mediator.cancel(pnr),
                "Ticket cancelled"
        );
    }

    // =====================================================
    // Undo cancellation
    // =====================================================

    private static void undo(HttpExchange ex) throws Exception {

        int pnr = intOf(
                Json.get(body(ex), "pnr"),
                "PNR"
        );

        respond(
                ex,
                mediator.undoCancel(pnr),
                "Cancellation undone"
        );
    }

    // =====================================================
    // Rebooking
    // =====================================================

    private static void rebook(HttpExchange ex) throws Exception {

        String json = body(ex);

        int pnr = intOf(
                Json.get(json, "pnr"),
                "PNR"
        );

        String mode = Json.get(json, "mode").toUpperCase();

        String date = Json.get(json, "journey_date");

        if (date.isEmpty()) {
            throw new IllegalStateException(
                    "Journey date is required"
            );
        }

        Booking clone;

        if (mode.equals("RETURN")) {

            clone = mediator.getPrototypes()
                    .returnJourney(pnr, date);

        } else if (mode.equals("COMPANION")) {

            clone = mediator.getPrototypes().getClone(pnr);

            if (clone != null) {

                String name = Json.get(json, "passenger_name");

                if (name.isEmpty()) {
                    throw new IllegalStateException(
                            "Passenger name is required"
                    );
                }

                Passenger old = clone.getPassenger();

                clone.setPassenger(
                        new Passenger(
                                name.trim(),
                                intOf(Json.get(json, "age"), "Age"),
                                Json.get(json, "gender"),
                                old.getMobileNumber(),
                                old.getEmail()
                        )
                );

                clone.setBerthPreference("NONE");

                clone.setJourneyDate(date);
            }

        } else {

            throw new IllegalStateException(
                    "Unknown rebook mode"
            );
        }

        if (clone == null) {
            sendJson(ex, 404, error("PNR not found"));
            return;
        }

        respond(
                ex,
                mediator.book(clone),
                "New booking created from PNR " + pnr
        );
    }

    // =====================================================
    // Common response
    // =====================================================

    private static void respond(
            HttpExchange ex,
            BookingContext ctx,
            String okMessage
    ) throws IOException {

        if (!ctx.isSuccess()) {

            System.out.println(
                    "[WebServer] Request failed: "
                            + ctx.getMessage()
            );

            sendJson(ex, 400, error(ctx.getMessage()));

            return;
        }

        Booking b = ctx.getBooking();

        sendJson(
                ex,
                200,
                Json.obj()
                        .put("success", true)
                        .put("message", okMessage)
                        .put("pnr", b.getPnr())
                        .put("status", b.getStatus())
                        .toString()
        );
    }

    // =====================================================
    // Ticket JSON
    // =====================================================

    private static Json.Obj ticketJson(
            Booking b,
            boolean withHistory
    ) {

        Passenger p = b.getPassenger();

        FareStrategy fs = fareContext.select(
                p.getAge(),
                b.getTravelClass()
        );

        Json.Obj o = Json.obj()
                .put("pnr", b.getPnr())
                .put("passenger_name", p.getName())
                .put("age", p.getAge())
                .put("gender", p.getGender())
                .put("mobile", p.getMobileNumber())
                .put("email", p.getEmail())
                .put("train_name", b.getTrainName())
                .put("source", b.getSource())
                .put("destination", b.getDestination())
                .put("travel_class", b.getTravelClass())
                .put("payment_mode", b.getPaymentMode())
                .put("journey_date", b.getJourneyDate())
                .put("base_fare", b.getBaseFare())
                .put("fare", b.getFare())
                .put("fare_category", b.getFareCategory())
                .put("discount_percent", fs.discountPercent())
                .put("status", b.getStatus())
                .put("coach", b.getCoach())
                .put("berth_no", b.getBerthNo())
                .put("berth_type", b.getBerthType())
                .put("berth_preference", b.getBerthPreference())
                .put("berth_note", mediator.explainBerth(b));

        if (withHistory) {

            o.put(
                    "can_undo",
                    mediator.getHistory().peek(b.getPnr()) != null
            );

            StringBuilder arr = new StringBuilder("[");

            List<BookingMemento> list =
                    mediator.getHistory().list(b.getPnr());

            for (int i = 0; i < list.size(); i++) {

                BookingMemento m = list.get(i);

                if (i > 0) {
                    arr.append(',');
                }

                arr.append(
                        Json.obj()
                                .put("label", m.getLabel())
                                .put("status", m.getStatus())
                                .put("coach", m.getCoach())
                                .put("berth_no", m.getBerthNo())
                                .put("berth_type", m.getBerthType())
                                .put("saved_at", m.getSavedAt())
                );
            }

            o.raw("history", arr.append(']').toString());
        }

        return o;
    }

    // =====================================================
    // Static frontend serving
    // =====================================================

    private static void serveFrontend(
            HttpExchange ex,
            Path frontendDirectory
    ) throws IOException {

        String requestPath = ex.getRequestURI().getPath();

        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        String fileName = requestPath.substring(1);

        if (fileName.contains("..")) {
            send404(ex);
            return;
        }

        Path filePath = frontendDirectory
                .resolve(fileName)
                .normalize();

        if (!filePath.startsWith(frontendDirectory)
                || !Files.isRegularFile(filePath)) {

            send404(ex);
            return;
        }

        byte[] content = Files.readAllBytes(filePath);

        ex.getResponseHeaders().set(
                "Content-Type",
                getContentType(fileName)
        );

        ex.getResponseHeaders().set(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        ex.sendResponseHeaders(200, content.length);

        try (OutputStream out = ex.getResponseBody()) {
            out.write(content);
        }
    }

    private static String getContentType(String name) {

        String l = name.toLowerCase();

        if (l.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (l.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (l.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        if (l.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }

        if (l.endsWith(".png")) {
            return "image/png";
        }

        if (l.endsWith(".jpg") || l.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (l.endsWith(".svg")) {
            return "image/svg+xml";
        }

        if (l.endsWith(".ico")) {
            return "image/x-icon";
        }

        return "application/octet-stream";
    }

    private static void sendJson(
            HttpExchange ex,
            int status,
            String response
    ) throws IOException {

        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        ex.sendResponseHeaders(status, data.length);

        try (OutputStream out = ex.getResponseBody()) {
            out.write(data);
        }
    }

    private static void send404(HttpExchange ex) throws IOException {

        byte[] data = (
                "404 - File Not Found: "
                        + ex.getRequestURI().getPath()
        ).getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        ex.sendResponseHeaders(404, data.length);

        try (OutputStream out = ex.getResponseBody()) {
            out.write(data);
        }
    }
}