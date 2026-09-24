package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.items.Chest;
import dungeonforge.items.Item;

/**
 * WEEK 1 -- one room of the dungeon.
 * WEEK 4 (US-2.2): flavor text comes from a ThemeKit, not hardcoded here.
 * WEEK 4 (US-2.3): a room may hold a Chest, placed by a RoomPopulator.
 * WEEK 5: a room may also hold loose floor items, and can answer hasChest() directly.
 */
public class Room {

    private final String id;
    private String flavor = "";
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Item> floorItems = new ArrayList<>();
    private Chest chest;

    public Room(String id) {
        this.id = id;
    }

    public String getId()                { return id; }
    public String getFlavor()            { return flavor; }
    public void setFlavor(String flavor) { this.flavor = flavor; }
    public List<Monster> getMonsters()   { return monsters; }
    public void addMonster(Monster m)    { monsters.add(m); }
    public Chest getChest()              { return chest; }
    public void setChest(Chest chest)    { this.chest = chest; }
    public boolean hasChest()            { return chest != null && !chest.isEmpty(); }
    public List<Item> getFloorItems()    { return floorItems; }
    public void addItem(Item item)       { floorItems.add(item); }
}
