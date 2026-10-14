package dungeonforge.core;

import dungeonforge.behavior.Action;
import dungeonforge.config.GameConfig;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;
import dungeonforge.states.DeadState;
import dungeonforge.states.MonsterState;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 5 -- strategies decide HOW a monster fights.
 * WEEK 7 -- one round at a time, because a human is typing.
 * WEEK 8 (US-6.3) -- monsters have a LIFECYCLE, and this class no longer knows its shape.
 *
 * The Week 7 version kept the lifecycle as a String and branched on it in three places
 * (the transitions, "may it act?", "is it enraged?"). Each of those is now ONE question asked
 * of the monster's current MonsterState object:
 *
 *   m.setState(m.getState().next(m, player));   // what comes next? the state decides
 *   if (!m.getState().canAct()) return;          // may it act?      the state answers
 *   base * m.getState().damageMultiplier()       // how hard?        the state answers
 *
 * Adding a new state now means adding a class; this file does not change.
 */
public class Combat {

    private final EventBus bus;

    public Combat(EventBus bus) { this.bus = bus; }

    /** One round of MONSTER actions. */
    public void monsterTurns(Player player, Room room) {
        for (Monster m : livingMonsters(room)) {
            monsterActs(m, player, room);          // act on the CURRENT state first...
            transition(m, player);                 // ...then let the state pick what comes next
            if (!player.isAlive()) break;
        }
        if (!player.isAlive()) {
            bus.publish(GameEvent.of(EventType.PLAYER_DIED));
        } else if (!hasLiving(room)) {
            bus.publish(GameEvent.of(EventType.ROOM_CLEARED, "room", room.getId()));
        }
    }

    /**
     * US-6.3: the whole Week 7 updateMonsterState() -- forty lines of nested ifs -- is now one
     * line. The CURRENT state knows its own outbound arrows and returns its successor.
     */
    private void transition(Monster m, Player player) {
        MonsterState before = m.getState();
        MonsterState after = before.next(m, player);
        if (after == before) return;
        m.setState(after);                         // runs after.onEnter(m), which may swap the strategy
        bus.publish(GameEvent.of(EventType.STRATEGY_CHANGED,
                "name", m.getName(), "from", before.label(), "to", after.label()));
    }

    /** US-6.3: "may this monster act?" is asked of the state, not answered with a conditional. */
    private void monsterActs(Monster m, Player player, Room room) {
        if (!m.getState().canAct()) return;
        if (m.getStrategy() == null) return;

        Action action = m.getStrategy().chooseAction(m, player, room);
        if (action == null) return;

        switch (action.getType()) {
            case ATTACK -> {
                int dmg = damageFrom(m);
                player.takeDamage(dmg);
                bus.publish(GameEvent.of(EventType.DAMAGE_TAKEN,
                        "source", m.getName(), "amount", dmg, "flavor", action.getFlavor()));
            }
            case RANGED_ATTACK -> {
                int dmg = Math.max(1, (int) Math.round(damageFrom(m) * 0.8));
                player.takeDamage(dmg);
                bus.publish(GameEvent.of(EventType.DAMAGE_TAKEN,
                        "source", m.getName(), "amount", dmg, "flavor", action.getFlavor()));
            }
            case FLEE -> {
                room.getMonsters().remove(m);
                bus.publish(GameEvent.of(EventType.MONSTER_FLED, "name", m.getName()));
            }
            case HEAL_ALLY -> {
                if (action.getTarget() != null) {
                    bus.message(action.getFlavor());
                    action.getTarget().heal(5);
                    bus.publish(GameEvent.of(EventType.MONSTER_HEALED,
                            "healer", m.getName(), "target", action.getTarget().getName()));
                }
            }
            case WAIT -> bus.publish(GameEvent.message(action.getFlavor()));
        }
    }

    /** US-6.3: "how hard does it hit?" -- enraged and fleeing states answer differently. */
    private int damageFrom(Monster m) {
        return Math.max(1, (int) Math.round(m.getAttackPower() * m.getState().damageMultiplier()));
    }

    public void playerStrikes(Player player, Monster target) {
        int damage = player.getAttackPower();
        target.takeDamage(damage);
        bus.publish(GameEvent.of(EventType.DAMAGE_DEALT,
                "target", target.getName(), "amount", damage));

        if (!target.isAlive()) {
            target.setState(new DeadState());
            player.addXp(target.getXpReward());
            player.addGold(target.getXpReward() * 2);
            bus.publish(GameEvent.of(EventType.MONSTER_DIED,
                    "name", target.getName(), "xp", target.getXpReward()));
            bus.publish(GameEvent.of(EventType.XP_GAINED, "amount", target.getXpReward()));
            bus.publish(GameEvent.of(EventType.GOLD_GAINED, "amount", target.getXpReward() * 2));
        }
    }

    public Monster firstLivingIn(Room room) { return firstLiving(room); }

    private boolean hasLiving(Room room) { return firstLiving(room) != null; }

    private Monster firstLiving(Room room) {
        for (Monster m : room.getMonsters()) if (m.isAlive()) return m;
        return null;
    }

    private List<Monster> livingMonsters(Room room) {
        List<Monster> out = new ArrayList<>();
        for (Monster m : room.getMonsters()) if (m.isAlive()) out.add(m);
        return out;
    }

    /** Between rooms the player catches their breath. The dead do not catch their breath. */
    public static void restAfterRoom(Player player) {
        if (!player.isAlive()) return;
        player.heal(GameConfig.getInstance().getInt("restHealPerRoom"));
    }
}
