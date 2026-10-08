package dungeonforge.events;

import dungeonforge.core.Player;

/**
 * WEEK 6 -- an Observer that tallies damage and warns once when the player
 * drops below a fraction of max HP.
 */
public class DamageMeter implements GameEventListener {

    private final EventBus bus;
    private final Player player;
    private final double warnFraction;
    private int dealt;
    private int taken;
    private boolean warned;

    public DamageMeter(EventBus bus, Player player, double warnFraction) {
        this.bus = bus;
        this.player = player;
        this.warnFraction = warnFraction;
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case DAMAGE_DEALT -> dealt += event.getInt("amount");
            case DAMAGE_TAKEN -> {
                taken += event.getInt("amount");
                if (!warned && player.getHp() <= player.getMaxHp() * warnFraction) {
                    warned = true;
                    bus.message("You are badly wounded -- consider a potion.");
                }
            }
            default -> { }
        }
    }

    public int getDealt() { return dealt; }
    public int getTaken() { return taken; }
}
