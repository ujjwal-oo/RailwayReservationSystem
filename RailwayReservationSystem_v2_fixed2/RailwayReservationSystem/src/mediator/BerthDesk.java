package mediator;

import model.Booking;
import repository.BookingRepository;
import strategy.Berth;
import strategy.BerthAllocator;

import java.util.List;
import java.util.Optional;

/**
 * Colleague: allots a berth using the Strategy pattern
 * (senior citizens -> lower berth if available).
 */
public class BerthDesk extends Colleague {

    private final BookingRepository repository;
    private final BerthAllocator allocator = new BerthAllocator();

    public BerthDesk(BookingRepository repository) {
        this.repository = repository;
    }

    public void allocate(BookingContext ctx) {
        Booking b = ctx.getBooking();
        List<Berth> free = repository.freeBerths(b.getTrainName(), b.getJourneyDate(),
                b.getTravelClass(), b.getPnr());
        Optional<Berth> chosen = allocator.allocate(b, free);
        if (!chosen.isPresent()) {
            System.out.println("[BerthDesk] No berth free for PNR " + b.getPnr());
            mediator.notify(this, BookingEvent.BERTH_UNAVAILABLE, ctx);
            return;
        }
        Berth berth = chosen.get();
        b.setCoach(berth.getCoach());
        b.setBerthNo(berth.getNumber());
        b.setBerthType(berth.getType());
        repository.updateState(b);
        System.out.println("[BerthDesk] " + allocator.select(b).name() + " -> " + b.getSeatLabel());
        mediator.notify(this, BookingEvent.BERTH_ALLOCATED, ctx);
    }

    public String explain(Booking b) { return allocator.explain(b); }
}
