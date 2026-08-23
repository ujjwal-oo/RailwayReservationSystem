package abstractfactory;

public class ACClassFactory implements TravelClassFactory {
    @Override
    public SeatAmenity createSeatAmenity() {
        return () -> System.out.println("[AbstractFactory:AC] Air-conditioned berth with complimentary bedding.");
    }

    @Override
    public MealService createMealService() {
        return () -> System.out.println("[AbstractFactory:AC] Pre-booked multi-course meal included in fare.");
    }
}
