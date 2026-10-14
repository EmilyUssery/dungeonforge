package dungeonforge.commands;

import dungeonforge.behavior.CombatStrategy;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.items.Item;
import dungeonforge.states.MonsterState;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * WEEK 7 (US-5.2) -- a MEMENTO of everything one turn can change.
 *
 * Why it exists: the naive undo remembered one number and failed, because a turn is not just
 * "my attack" -- the monsters reply, heal each other, swap strategies and run away. This
 * class captures ALL of it before the turn, and restore() puts ALL of it back.
 *
 * Every list is COPIED, never aliased: if we kept a reference to room.getMonsters(), the
 * "snapshot" would change as the game changed and remember nothing.
 */
public final class TurnSnapshot {

    private final Player player;
    private final Room room;

    private final int hp, xp, gold;
    private final List<Item> inventory;
    private final List<Monster> monsters;
    private final List<Item> floorItems;
    private final List<Item> chestContents;                       // null if the room has no chest
    private final Map<Monster, Integer> monsterHp = new IdentityHashMap<>();
    private final Map<Monster, CombatStrategy> monsterStrategies = new IdentityHashMap<>();
    private final Map<Monster, MonsterState> monsterStates = new IdentityHashMap<>();   // Week 8

    private TurnSnapshot(Player player, Room room) {
        this.player = player;
        this.room = room;
        this.hp = player.getHp();
        this.xp = player.getXp();
        this.gold = player.getGold();
        this.inventory = new ArrayList<>(player.getInventory());
        this.monsters = new ArrayList<>(room.getMonsters());       // includes ones that may flee
        this.floorItems = new ArrayList<>(room.getFloorItems());
        this.chestContents = room.getChest() == null ? null : new ArrayList<>(room.getChest().getContents());
        for (Monster m : this.monsters) {
            monsterHp.put(m, m.getHp());
            monsterStrategies.put(m, m.getStrategy());
            monsterStates.put(m, m.getState());
        }
    }

    /** Take a picture of the player and the room they are standing in. */
    public static TurnSnapshot capture(Player player, Room room) {
        return new TurnSnapshot(player, room);
    }

    /** Put every captured value back exactly as it was. */
    public void restore() {
        player.setHp(hp);
        player.setXp(xp);
        player.setGold(gold);
        player.getInventory().clear();
        player.getInventory().addAll(inventory);

        room.getMonsters().clear();                                  // brings back monsters that fled
        room.getMonsters().addAll(monsters);
        room.getFloorItems().clear();
        room.getFloorItems().addAll(floorItems);
        if (chestContents != null && room.getChest() != null) {
            room.getChest().getContents().clear();
            room.getChest().getContents().addAll(chestContents);
        }
        for (Map.Entry<Monster, Integer> e : monsterHp.entrySet()) {
            e.getKey().setHp(e.getValue());                          // the dead get back up
        }
        for (Map.Entry<Monster, MonsterState> e : monsterStates.entrySet()) {
            e.getKey().setState(e.getValue());                       // Week 8: back to its old lifecycle state
        }
        for (Map.Entry<Monster, CombatStrategy> e : monsterStrategies.entrySet()) {
            e.getKey().setStrategy(e.getValue());                    // undoes a Week 5 tactics swap
        }
    }

    public Room getRoom()     { return room; }
    public Player getPlayer() { return player; }
}