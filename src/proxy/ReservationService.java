package proxy;

import model.Booking;

/**
 * PROXY PATTERN
 * -------------
 * Participants:
 *  - Subject (interface): ReservationService
 *  - RealSubject: RealReservationService (does the actual DB insert)
 *  - Proxy: ReservationServiceProxy (adds authentication, logging, and
 *           runs the Chain-of-Responsibility validation chain BEFORE
 *           delegating to the RealSubject)
 *  - Client: Main
 */
public interface ReservationService {
    boolean reserve(Booking booking);
}
