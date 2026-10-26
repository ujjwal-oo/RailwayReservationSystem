package memento;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MEMENTO PATTERN - Caretaker
 * Holds a stack of mementos per PNR without looking inside them.
 * (In-memory: history is lost when the server restarts.)
 */
public class BookingHistory {

    private final Map<Integer, Deque<BookingMemento>> history = new HashMap<>();

    public synchronized void save(int pnr, BookingMemento memento) {
        history.computeIfAbsent(pnr, k -> new ArrayDeque<>()).push(memento);
    }

    /** Removes and returns the most recent memento, or null if none. */
    public synchronized BookingMemento pop(int pnr) {
        Deque<BookingMemento> d = history.get(pnr);
        return (d == null || d.isEmpty()) ? null : d.pop();
    }

    public synchronized BookingMemento peek(int pnr) {
        Deque<BookingMemento> d = history.get(pnr);
        return (d == null || d.isEmpty()) ? null : d.peek();
    }

    /** Newest first. */
    public synchronized List<BookingMemento> list(int pnr) {
        Deque<BookingMemento> d = history.get(pnr);
        return d == null ? new ArrayList<>() : new ArrayList<>(d);
    }
}
