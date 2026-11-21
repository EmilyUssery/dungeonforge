package dungeonforge.events;

import dungeonforge.core.Player;

/**
 * WEEK 12 -- warns once when the player is nearly dead.
 * WEEK 13 (US-9.4) -- the level is a FRACTION of the player's live maximum, computed on every
 * event. It used to be a constant derived from the starting hit points, which stopped meaning
 * anything the moment a maximum changed.
 */
public class DamageMeter implements GameEventListener {
    private final EventBus bus;
    private final Player player;
    private final double fraction;
    private boolean haveWarned;

    public DamageMeter(EventBus bus, Player player, double fraction) {
        this.bus = bus;
        this.player = player;
        this.fraction = fraction;
        this.haveWarned = false;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (haveWarned) return;
        if (event.getType().equals(EventType.DAMAGE_TAKEN)) {
            int warnAt = (int) Math.round(fraction * player.getMaxHp());
            if (player.getHp() <= warnAt) {
                bus.message(String.format(
                        "~~~ DANGER %s ROBINSON: You're dangerously close to death!!!\n      HP: %d, WARN: %d ~~~",
                        player.getName(), player.getHp(), warnAt));
                haveWarned = true;
            }
        }
    }
}
