package strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical layout of each travel class. A sleeper-style coach repeats an
 * 8-berth bay: LB, MB, UB, LB, MB, UB, SL, SU.
 */
public final class BerthLayout {

    private static final String[] BAY = {
            Berth.LOWER, Berth.MIDDLE, Berth.UPPER,
            Berth.LOWER, Berth.MIDDLE, Berth.UPPER,
            Berth.SIDE_LOWER, Berth.SIDE_UPPER
    };

    private BerthLayout() {}

    public static List<Berth> forClass(String travelClass) {
        List<Berth> list = new ArrayList<>();
        String c = travelClass == null ? "" : travelClass.toUpperCase();
        switch (c) {
            case "AC":      addCoach(list, "B1", 24); break;
            case "SLEEPER": addCoach(list, "S1", 24); addCoach(list, "S2", 24); break;
            case "TATKAL":  addCoach(list, "T1", 8);  break;
            case "GENERAL":
                for (int i = 1; i <= 100; i++) list.add(new Berth("D1", i, Berth.SEAT));
                break;
            default: break;
        }
        return list;
    }

    public static int capacity(String travelClass) { return forClass(travelClass).size(); }

    private static void addCoach(List<Berth> list, String coach, int count) {
        for (int i = 0; i < count; i++) list.add(new Berth(coach, i + 1, BAY[i % 8]));
    }
}
