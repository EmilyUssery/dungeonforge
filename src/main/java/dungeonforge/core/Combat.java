package dungeonforge.core;

import java.util.ArrayList;

import dungeonforge.behavior.Action;
import dungeonforge.behavior.SkittishStrategy;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;

/**
 * WEEK 5 -- runs a fight in one room.
 *
 * Two rules this class exists to enforce:
 *   1. A CombatStrategy returns a DECISION (an Action). Combat is the only thing that
 *      actually carries that decision out -- strategies never touch hp directly.
 *   2. Combat decides nothing about behaviour beyond asking the strategy, and knows nothing
 *      about who is listening. It publishes events on the EventBus and never prints.
 */
public class Combat {

    private final EventBus bus;

    public Combat(EventBus bus) {
        this.bus = bus;
    }

    /** Fights out one room. Returns true if the player survived, false if the player died. */
    public boolean fight(Player player, Room room, int depth) {
        while (player.isAlive() && hasLivingMonster(room)) {
            for (Monster monster : new ArrayList<>(room.getMonsters())) {
                if (!monster.isAlive() || !room.getMonsters().contains(monster)) {
                    continue;
                }
                if (!player.isAlive()) {
                    break;
                }

                checkForTacticsChange(monster);

                Action action = monster.getStrategy().chooseAction(monster, player, room);
                resolveMonsterAction(monster, player, room, action);
            }

            if (!player.isAlive()) {
                break;
            }

            Monster target = firstAlive(room);
            if (target != null) {
                resolvePlayerAttack(player, target);
            }
        }

        if (!player.isAlive()) {
            return false;
        }

        bus.publish(GameEvent.of(EventType.ROOM_CLEARED, "room", room.getId()));
        return true;
    }

    /** WEEK 5 (US-3.2) -- a badly wounded monster swaps to a skittish strategy, once. */
    private void checkForTacticsChange(Monster monster) {
        if (monster.getStrategy() instanceof SkittishStrategy) {
            return;
        }
        if (monster.hpFraction() < SkittishStrategy.FLEE_THRESHOLD) {
            String from = monster.getStrategy() == null ? "unknown" : monster.getStrategy().name();
            monster.setStrategy(new SkittishStrategy());
            bus.publish(GameEvent.of(EventType.STRATEGY_CHANGED,
                    "name", monster.getSpecies(), "from", from, "to", "skittish"));
        }
    }

    private void resolveMonsterAction(Monster monster, Player player, Room room, Action action) {
        switch (action.getType()) {
            case ATTACK -> {
                int damage = Math.max(1, monster.getAttackPower() - player.getDefense());
                player.takeDamage(damage);
                bus.publish(GameEvent.message(monster.getSpecies() + " attacks for " + damage + "."));
            }
            case RANGED_ATTACK -> {
                int damage = Math.max(1, monster.getAttackPower() - player.getDefense());
                player.takeDamage(damage);
                bus.publish(GameEvent.message(monster.getSpecies() + " strikes from range for " + damage + "."));
            }
            case FLEE -> {
                room.getMonsters().remove(monster);
                bus.publish(GameEvent.message(monster.getSpecies() + " flees."));
            }
            case HEAL_ALLY -> {
                Monster ally = action.getTarget();
                int healAmount = Math.max(1, monster.getAttackPower());
                ally.heal(healAmount);
                bus.publish(GameEvent.message(monster.getSpecies() + " mends " + ally.getSpecies() + "."));
            }
            case WAIT -> bus.publish(GameEvent.message(monster.getSpecies() + " " + action.getFlavor() + "."));
        }
    }

    private void resolvePlayerAttack(Player player, Monster target) {
        int damage = Math.max(1, player.getAttackPower());
        target.takeDamage(damage);
        if (!target.isAlive()) {
            player.addXp(target.getXpReward());
            bus.publish(GameEvent.of(EventType.MONSTER_DIED,
                    "name", target.getSpecies(), "xp", target.getXpReward()));
        }
    }

    private boolean hasLivingMonster(Room room) {
        for (Monster m : room.getMonsters()) {
            if (m.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private Monster firstAlive(Room room) {
        for (Monster m : room.getMonsters()) {
            if (m.isAlive()) {
                return m;
            }
        }
        return null;
    }

    /** Heals the player a little between rooms. */
    public static void restAfterRoom(Player player) {
        player.heal(Math.max(1, player.getMaxHp() / 10));
    }
}
