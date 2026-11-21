package dungeonforge.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dungeonforge.items.Chest;
import dungeonforge.items.Item;

/**
 * WEEK 1 -- one room of the dungeon.
 * WEEK 3 (US-1.2) -- flavour text is picked from the one seeded source.
 */
public class Room {

    private final String id;
    private String flavor;
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Item> floorItems = new ArrayList<>();
    /** WEEK 7 (provided): where you can walk from here. Week 10's generators make this a real graph. */
    private final Map<String, Room> exits = new LinkedHashMap<>();

    public Room(String id) {
        this.id = id;
        this.flavor = "";
    }

    public String getId()               { return id; }
    public String getFlavor()           { return flavor; }
    public void setFlavor(String f)     { this.flavor = f; }
    public List<Monster> getMonsters()  { return monsters; }
    public void addMonster(Monster m)   { monsters.add(m); }
    public List<Item> getFloorItems()   { return floorItems; }
    public void addItem(Item i)         { floorItems.add(i); }

    /**
     * WEEK 13 (US-10.3): the containers lying in this room. A chest is an Item now, so it
     * lives on the floor with everything else; this answers "which of those are boxes?"
     */
    public List<Chest> getChests() {
        List<Chest> out = new ArrayList<>();
        for (Item i : floorItems) if (i instanceof Chest c) out.add(c);
        return out;
    }

    /** WEEK 13: one walk over everything takeable, at any depth, containers first. */
    public java.util.Iterator<Item> contents() {
        return new CompositeIterator(floorItems.iterator());
    }

    /** WEEK 13 (US-10.1): the same walk, for for-each loops. */
    public Iterable<Item> asContents() { return this::contents; }

    public Map<String, Room> getExits()      { return exits; }
    public Room getExit(String direction)    { return exits.get(direction.toLowerCase()); }
    public void link(String direction, Room to) { exits.put(direction.toLowerCase(), to); }
    /** The first monster still standing, or null. */
    public Monster firstLivingIn() {
        for (Monster m : monsters) if (m.isAlive()) return m;
        return null;
    }

    public boolean hasLivingMonsters() {
        for (Monster m : monsters) if (m.isAlive()) return true;
        return false;
    }
}
