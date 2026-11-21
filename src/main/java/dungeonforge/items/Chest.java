package dungeonforge.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * WEEK 4 -- a container of loot, placed by a TreasureRoomPopulator or BossRoomPopulator.
 * WEEK 13 (US-10.2) -- the COMPOSITE. A chest is an Item that holds Items, so it has a weight,
 * a value, and a type the inventory accepts. add() lives here and NOT on Item: a leaf has no
 * honest add(), so the Component does not promise one (the "safety" side of the trade).
 */
public class Chest implements Item {

    /** The box itself. Every chest weighs this much empty. */
    private static final double TARE_WEIGHT_KG = 5.0;

    private final String name;
    private final List<Item> contents = new ArrayList<>();

    public Chest(String name) { this.name = name; }

    @Override public String getName() { return name; }
    public List<Item> getContents()   { return contents; }
    public boolean isEmpty()          { return contents.isEmpty(); }

    public void add(Item item) {
        if (item == this) throw new IllegalArgumentException("A chest cannot contain itself.");
        contents.add(item);
    }

    public boolean remove(Item item) { return contents.remove(item); }

    /**
     * The tare plus each DIRECT child's weight. No hand-written recursion: a nested chest's
     * getWeight() already includes its own contents, so asking each child once is the whole job.
     */
    @Override
    public double getWeight() {
        double sum = TARE_WEIGHT_KG;
        for (Item i : contents) sum += i.getWeight();
        return sum;
    }

    /** The children's value. The box itself is not treasure. */
    @Override
    public int getValue() {
        int sum = 0;
        for (Item i : contents) sum += i.getValue();
        return sum;
    }

    /** Direct contents only. Walking the whole tree is CompositeIterator's job (US-10.3). */
    @Override
    public Iterator<Item> children() { return contents.iterator(); }

    @Override
    public String describe() {
        return name + " (" + String.format("%.1f", getWeight()) + "kg, " + getValue() + "g, "
                + contents.size() + " item" + (contents.size() == 1 ? "" : "s") + ")";
    }

    /** @deprecated duplicates {@link #getValue()} now that a chest is an Item. */
    @Deprecated
    public int totalValue() { return getValue(); }
}
