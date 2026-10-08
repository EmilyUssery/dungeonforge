package dungeonforge.items;

/**
 * WEEK 7 (US-5.4) -- the Chronomancer's Hourglass: an item that rewinds time.
 *
 * Why it exists: in Week 6 "undo the last turn" was impossible -- there was nothing to undo.
 * In Week 7 every turn is a Command on a history stack with a snapshot behind it, so this
 * whole feature is one small class plus a few lines in UseItemCommand.
 */
public class Hourglass implements Item {

    private int charges;

    public Hourglass(int charges) {
        this.charges = Math.max(0, charges);
    }

    @Override public String getName()   { return "Chronomancer's Hourglass"; }
    @Override public double getWeight() { return 0.5; }
    @Override public int getValue()     { return 250; }

    public boolean hasCharges() { return charges > 0; }
    public int getCharges()     { return charges; }

    /** Use up one charge; never goes below zero. */
    public void spendCharge() {
        if (charges > 0) charges--;
    }

    @Override
    public String describe() {
        return getName() + " [" + charges + " charges, rewinds one turn]";
    }
}