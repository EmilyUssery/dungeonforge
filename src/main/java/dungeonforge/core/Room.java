package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.items.Chest;

/**
 * WEEK 1 -- one room of the dungeon.
 *
 * WEEK 3 (US-1.2): randomness now comes from the single shared RandomSource.
 *
 * WEEK 4 (US-2.2): a room no longer knows what it sounds like. Its flavor
 * text now comes from that level's ThemeKit via setFlavor(...).
 *
 * WEEK 4 (US-2.3): a room may hold a Chest, placed by a RoomPopulator.
 */
public class Room {

    private final String id;
    private String flavor = "";
    private final List<Monster> monsters = new ArrayList<>();
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
}