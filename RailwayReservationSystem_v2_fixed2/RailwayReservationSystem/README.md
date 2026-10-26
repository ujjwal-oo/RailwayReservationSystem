# Railway Reservation System — Design Patterns Mini Project  

A Java application (console demo + web UI) implementing **12 design patterns** together
in one coherent domain (railway ticket booking), backed by a real **SQLite**
database (`railway.db`, created automatically on first run, and migrated
automatically if it was created by an older version).

## How to run

```bash
./run-web.sh      # web app  -> http://127.0.0.1:8081   (keeps railway.db)
./run.sh          # console demo of all patterns (recreates railway.db)
```

Requires JDK 11+ (tested on OpenJDK 21). Manual console run:

```bash
mkdir -p bin
javac -cp lib/sqlite-jdbc-3.40.1.0.jar -d bin $(find src -name "*.java")
java -cp "bin:lib/sqlite-jdbc-3.40.1.0.jar" main.Main
```

This compiles everything, runs `main.Main`, creates `railway.db` in the
project folder, and prints the contents of the `bookings` and
`notification_log` tables at the end so you can see the persistence working.

---

## 1. Singleton Pattern

**Package:** `singleton`
**Class:** `DatabaseConnectionManager`

Ensures only **one** SQLite connection exists for the whole application.
Private constructor + static `getInstance()` + a static instance field,
with double-checked locking for thread safety. Every other pattern
(Proxy, Observer) reads/writes through this same instance so there's never a
duplicate or conflicting connection to `railway.db`.

```java
DatabaseConnectionManager db1 = DatabaseConnectionManager.getInstance();
DatabaseConnectionManager db2 = DatabaseConnectionManager.getInstance();
// db1 == db2  -> true
```

---

## 2. Chain of Responsibility Pattern

**Package:** `chain`
**Classes:**
- `ValidationHandler` (abstract Handler)
- `PassengerDetailsHandler` → `SeatAvailabilityHandler` → `PaymentValidationHandler` (ConcreteHandlers)

Each handler checks one thing about a `Booking` and either passes it to
`nextHandler` or rejects it outright (returns `false`, stopping the chain).
The chain is built once, inside `ReservationServiceProxy`:

```java
passengerCheck.setNext(seatCheck).setNext(paymentCheck);
```

---

## 3. Factory Method Pattern

**Package:** `factorymethod`

| Role | Class(es) |
|---|---|
| Product | `Ticket` (interface) |
| ConcreteProduct | `GeneralTicket`, `SleeperTicket`, `ACTicket`, `TatkalTicket` |
| Creator | `TicketCreator` (abstract, declares `createTicket()`) |
| ConcreteCreator | `GeneralTicketCreator`, `SleeperTicketCreator`, `ACTicketCreator`, `TatkalTicketCreator` |

Each `ConcreteCreator` overrides `createTicket()` to decide which concrete
`Ticket` gets built. `TicketCreator.issueTicket()` is the shared logic
(print ticket + compute surcharge) that stays identical across all
creators — only *which product* varies.

`TicketCreatorFactory` is a small convenience **Simple Factory** (not the
pattern itself) used only so `Main` doesn't need `if/else` to pick a creator.

---

## 4. Proxy Pattern

**Package:** `proxy`

| Role | Class |
|---|---|
| Subject (interface) | `ReservationService` |
| RealSubject | `RealReservationService` — inserts the booking into SQLite |
| Proxy | `ReservationServiceProxy` — adds validation (via the Chain of Responsibility), logging, and post-success Observer notification, before delegating to the RealSubject |
| Client | `Main` |

The client only ever talks to `ReservationServiceProxy`, never directly to
`RealReservationService`. The proxy decides whether the real object is
even invoked — e.g., invalid bookings are rejected before the database is
touched at all.

---

## 5. Abstract Factory Pattern

**Package:** `abstractfactory`

| Role | Class(es) |
|---|---|
| AbstractFactory | `TravelClassFactory` |
| ConcreteFactory | `SleeperClassFactory`, `ACClassFactory`, `GeneralClassFactory` |
| AbstractProduct A | `SeatAmenity` |
| AbstractProduct B | `MealService` |

Each concrete factory produces a **matched family** of objects — you can
never accidentally get an AC meal paired with a Sleeper seat amenity,
because both come from the same factory call.

---

## 6. Bridge Pattern

**Package:** `bridge`

| Role | Class(es) |
|---|---|
| Abstraction | `Payment` |
| RefinedAbstraction | `FastTrackPayment` (adds a Tatkal-style priority fee) |
| Implementor | `PaymentGateway` |
| ConcreteImplementor | `UPIGateway`, `CreditCardGateway`, `NetBankingGateway` |

`Payment` holds a `PaymentGateway` reference (the "bridge") instead of
inheriting from it, so payment types and gateway types can each grow
independently — e.g. adding a `WalletGateway` never requires touching
`Payment` or `FastTrackPayment`.

---

## 7. Observer Pattern (with database connectivity)

**Package:** `observer`

| Role | Class(es) |
|---|---|
| Subject | `ReservationSubject` |
| Observer (interface) | `ReservationObserver` — `update(Booking booking)` |
| ConcreteObserver | `PassengerAppObserver`, `SMSNotificationObserver`, `EmailNotificationObserver` |

`update()` takes the **`Booking` object itself**, not a String — this
mirrors the correction your instructor gave on the Faculty Leave
Management System practical. `SMSNotificationObserver` and
`EmailNotificationObserver` also **write their own row** into a
`notification_log` table via the same Singleton connection, so the
Observer pattern here is genuinely tied to database connectivity, not
just console prints.

---

---

# New in this version

Pattern map: **Mediator** coordinates the whole workflow; **Builder** creates the
booking; **Strategy** prices it and picks the berth; **Prototype** clones it;
**Memento** makes cancellation undoable. The original 7 patterns are all still
used inside that flow.

```
POST /api/book
   WebServer --Builder--> Booking --> RailwayBookingMediator (Mediator)
        FareDesk         (Strategy: fare)
     -> ReservationDesk  (Proxy -> Chain of Responsibility -> Singleton DB)
     -> BerthDesk        (Strategy: berth allocation)
     -> PaymentDesk      (Bridge)
     -> NotificationDesk (Observer: app + SMS + email -> notification_log)
```

## Senior citizens get a lower berth

Passengers aged **60+** are allotted a lower berth if one is free:
`LOWER` first, then `SIDE_LOWER`; if none is left they get the best remaining
berth and the ticket says so. The constant is `FareContext.SENIOR_AGE`.
Passengers with no preference are given middle/upper berths first, so lower
berths stay available for seniors. An explicit preference (e.g. Lower) is
honoured when possible. Berth availability is real: it is counted per
**train + journey date + class** from the database.

Layout per class: AC = coach B1 (24 berths), Sleeper = S1 + S2 (48), Tatkal = T1 (8),
General = 100 unreserved seats. A full class waitlists the booking.

## 8. Builder Pattern
**Package:** `builder` - `BookingBuilder` builds a `Booking` step by step
(`passenger()`, `contact()`, `train()`, `route()`, `travelClass()`, ...),
applies defaults, normalises values and validates in `build()`.
`WebServer` uses it instead of a long constructor call.

## 9. Strategy Pattern
**Package:** `strategy` - two strategy families, each with a Context:

| Family | Strategy interface | Concrete strategies | Context |
|---|---|---|---|
| Fare | `FareStrategy` | `RegularFareStrategy`, `SeniorCitizenFareStrategy` (-40%), `ChildFareStrategy` (5-11, -50%), `TatkalFareStrategy` (+30%) | `FareContext` |
| Berth | `BerthAllocationStrategy` | `LowerBerthPriorityStrategy` (age 60+), `PreferredBerthStrategy`, `StandardBerthStrategy` | `BerthAllocator` |

The strategy is chosen at runtime from the passenger's age, class and preference.
`GET /api/quote` exposes the fare strategy so the booking form shows the
discounted fare live as the age is typed.

## 10. Mediator Pattern
**Package:** `mediator`

| Role | Class(es) |
|---|---|
| Mediator | `BookingMediator` (interface), `RailwayBookingMediator` |
| Colleagues | `FareDesk`, `ReservationDesk`, `BerthDesk`, `PaymentDesk`, `NotificationDesk` |

Colleagues never call each other. Each reports an event
(`FARE_CALCULATED`, `RESERVED`, `BERTH_ALLOCATED`, `PAYMENT_DONE`, ...) to the
mediator, whose `notify()` decides the next step. Per-request state travels in a
`BookingContext`, so the mediator itself is stateless. The same mediator runs
the cancel and undo flows.

## 11. Memento Pattern
**Package:** `memento`

| Role | Class |
|---|---|
| Originator | `Booking` (`createMemento()` / `restoreFromMemento()`) |
| Memento | `BookingMemento` (status, coach, berth, fare - immutable) |
| Caretaker | `BookingHistory` (stack of mementos per PNR) |

Cancelling a ticket saves a memento first; **Undo Cancellation** restores it. If
the original berth was given to someone else in the meantime, the mediator
allots a fresh one (lower-berth rule still applies). History is kept in memory,
so it is cleared when the server restarts.

## 12. Prototype Pattern
**Package:** `prototype` - `Prototype<T>` (`copy()`), implemented by `Booking`
and `Passenger` (deep copy), plus `BookingPrototypeRegistry`.
The ticket page can **clone** a booking for a *return journey* (route swapped,
new date) or for *another passenger on the same journey*; the clone is then
booked through the mediator like any other booking.

## Web API

| Endpoint | Purpose |
|---|---|
| `GET /api/quote?age=&class=&fare=&preference=` | fare + berth policy preview (Strategy) |
| `GET /api/availability?train=&date=&class=` | free / lower berths |
| `POST /api/book` | Builder -> Mediator |
| `GET /api/ticket?pnr=` | ticket, berth, saved states |
| `POST /api/cancel` `{pnr}` / `POST /api/undo` `{pnr}` | Memento |
| `POST /api/rebook` `{pnr, mode: RETURN or COMPANION, journey_date, ...}` | Prototype |

## Database schema (auto-created)

```sql
CREATE TABLE bookings (
    pnr INTEGER PRIMARY KEY AUTOINCREMENT,
    passenger_name TEXT, age INTEGER, gender TEXT,
    mobile TEXT, email TEXT,
    train_name TEXT, source TEXT, destination TEXT,
    travel_class TEXT, payment_mode TEXT, fare REAL, status TEXT,
    journey_date TEXT, berth_preference TEXT, coach TEXT,
    berth_no INTEGER DEFAULT 0, berth_type TEXT,
    base_fare REAL, fare_category TEXT
);

CREATE TABLE notification_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pnr INTEGER, channel TEXT, message TEXT,
    sent_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

## Sample flow exercised in `Main.java`

1. Get the Singleton DB manager twice, prove it's the same instance.
2. Attach 3 observers to a `ReservationSubject`.
3. Wrap a `RealReservationService` in a `ReservationServiceProxy`.
4. Submit a valid AC booking → passes the chain → persisted → all 3 observers fire.
5. Submit a deliberately invalid booking (blank name, negative age) → rejected by `PassengerDetailsHandler`, chain stops, nothing is written to the DB.
6. Submit a valid Sleeper booking → same success path.
7. Use `TicketCreatorFactory` + `TicketCreator` (Factory Method) to issue tickets and compute class-based surcharges.
8. Use `TravelClassFactory` (Abstract Factory) to get matched seat/meal amenities for AC and Sleeper.
9. Use `Payment`/`FastTrackPayment` (Bridge) with `UPIGateway`/`CreditCardGateway` to process the final fares.
10. Dump `bookings` and `notification_log` tables to prove everything actually persisted.


## Live train search

The web search now calls the RailRadar API from the Java backend. The API key is
kept on the server and is not exposed in browser JavaScript. Live data is requested
with `live=true`; if the provider is down, the page shows a friendly message rather
than a JavaScript error.

1. Create a RailRadar API key from the provider's official website.
2. Set an environment variable named `RAILRADAR_API_KEY` before starting `WebServer`.
3. Restart the server after setting the variable.

PowerShell example:

```powershell
$env:RAILRADAR_API_KEY = "YOUR_API_KEY"
```

For IntelliJ, add `RAILRADAR_API_KEY` under **Run > Edit Configurations > Environment variables**.
Do not commit the real key to source control. The provider enforces its own plan limits,
so live results require a valid key and available quota.

The endpoint used by the frontend is `GET /api/trains?from=Mumbai&to=Delhi&date=YYYY-MM-DD`.
