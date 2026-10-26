package abstractfactory;

/**
 * ABSTRACT FACTORY PATTERN
 * -------------------------
 * Participants:
 *  - AbstractFactory: TravelClassFactory
 *  - ConcreteFactories: SleeperClassFactory, ACClassFactory
 *  - AbstractProducts: SeatAmenity, MealService
 *  - ConcreteProducts: SleeperSeatAmenity/ACSeatAmenity, SleeperMeal/ACMeal
 *
 * Each concrete factory produces a FAMILY of related objects
 * (seat amenity + meal service) that are guaranteed to be consistent
 * with each other (you never get an AC meal with a Sleeper seat amenity).
 */
public interface TravelClassFactory {
    SeatAmenity createSeatAmenity();
    MealService createMealService();
}
