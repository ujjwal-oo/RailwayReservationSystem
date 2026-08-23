package abstractfactory;

public class GeneralClassFactory implements TravelClassFactory {
    @Override
    public SeatAmenity createSeatAmenity() {
        return () -> System.out.println("[AbstractFactory:General] Unreserved seating, first-come-first-serve.");
    }

    @Override
    public MealService createMealService() {
        return () -> System.out.println("[AbstractFactory:General] No meal service; platform vendors only.");
    }
}
