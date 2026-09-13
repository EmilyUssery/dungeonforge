package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.config.RandomSource;

/**
 * WEEK 1 -- one room of the dungeon.
 *
 * WEEK 3 (US-1.2): randomness now comes from the single shared RandomSource.
 */
public class Room {

    private static final String[] FLAVORS = {
        "Damp stone. Something drips in the dark, patiently.",
        "Burial niches line the walls. Most are empty. Most.",
        "The air tastes of old dust and older grief.",
        "Your footsteps come back a half-second late."
    };

    private final String id;
    private final String flavor;
    private final List<Monster> monsters = new ArrayList<>();

    public Room(String id) {
        this.id = id;
        this.flavor = FLAVORS[RandomSource.getInstance().nextInt(FLAVORS.length)];
    }

    public String getId()               { return id; }
    public String getFlavor()           { return flavor; }
    public List<Monster> getMonsters()  { return monsters; }
    public void addMonster(Monster m)   { monsters.add(m); }
}
