package dungeonforge.items;

import java.util.Collections;
import java.util.Iterator;

/**
 * WEEK 4 -- introduced so that a ThemeKit has loot to hand back.
 *
 * Deliberately minimal. Week 9 will wrap these in DECORATORS to add enchantments, and
 * Week 13 will turn this into a COMPOSITE so bags can hold bags. Do not over-build it now.
 */
public interface Item {
    String getName();
    double getWeight();
    int getValue();

    /**
     * WEEK 13 (US-10.2) -- COMPOSITE. Traversal belongs on the Component; mutation does not.
     * A leaf has no children, and these defaults mean no leaf class had to change.
     */
    default Iterator<Item> children() { return Collections.emptyIterator(); }

    /** True when this item holds something right now. An empty chest says false, honestly. */
    default boolean isContainer() { return children().hasNext(); }

    default String describe() {
        return getName() + " (" + String.format("%.1f", getWeight()) + "kg, " + getValue() + "g)";
    }
}
