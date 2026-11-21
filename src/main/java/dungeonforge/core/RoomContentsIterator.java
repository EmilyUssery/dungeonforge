package dungeonforge.core;

import dungeonforge.items.Item;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * WEEK 13 (US-10.1) -- ITERATOR. One walk over everything takeable in a room: the floor,
 * then the chest. Callers stop needing to know that a room keeps items in two places.
 *
 * The case people get wrong: an EMPTY floor and a FULL chest. hasNext() must look past the
 * floor before it answers.
 */
public class RoomContentsIterator implements Iterator<Item> {

    private final List<Item> floor;
    private final List<Item> chest;     // may be null: not every room has one
    private List<Item> current = null;  // the list we are walking
    private int index = -1;             // the index we last handed out in `current`

    public RoomContentsIterator(List<Item> floor, List<Item> chest) {
        this.floor = floor;
        this.chest = chest;
    }

    private boolean chestHasItems() { return chest != null && !chest.isEmpty(); }

    @Override
    public boolean hasNext() {
        if (current == null) return !floor.isEmpty() || chestHasItems();
        if (index + 1 < current.size()) return true;
        return current == floor && chestHasItems();
    }

    @Override
    public Item next() {
        if (!hasNext()) throw new NoSuchElementException();
        if (current == null) {
            current = floor.isEmpty() ? chest : floor;
            index = -1;
        } else if (index + 1 >= current.size()) {   // end of the floor: cross into the chest
            current = chest;
            index = -1;
        }
        return current.get(++index);
    }

    /** Deletes from whichever list the last item came out of. This is why the refactor works. */
    @Override
    public void remove() {
        if (current == null || index < 0) {
            throw new IllegalStateException("remove() called before next()");
        }
        current.remove(index--);   // step back so the next next() does not skip an item
    }
}
