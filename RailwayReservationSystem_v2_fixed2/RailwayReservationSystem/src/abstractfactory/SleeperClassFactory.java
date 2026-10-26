package abstractfactory;

public class SleeperClassFactory implements TravelClassFactory {
    @Override
    public SeatAmenity createSeatAmenity() {
        return () -> System.out.println("[AbstractFactory:Sleeper] Non-AC berth, cotton bedsheet on request.");
    }

    @Override
    public MealService createMealService() {
        return () -> System.out.println("[AbstractFactory:Sleeper] Pantry vendor meal, pay on board.");
    }
}
