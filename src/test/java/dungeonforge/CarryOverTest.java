package dungeonforge;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Player;
import dungeonforge.events.DamageMeter;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;
import dungeonforge.items.BasicWeapon;
import dungeonforge.items.EnchantmentRegistry;
import dungeonforge.items.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** WEEK 13 -- stories carried over from earlier sprints (US-8.5, US-9.4). */
class CarryOverTest {

    @BeforeEach
    void setUp() {
        GameConfig.resetForTests();
        RandomSource.resetForTests();
    }

    // ===================== US-8.5 : carried from Sprint 8 =====================

    @Test
    void enchantmentNamesAreCaseAndWhitespaceInsensitive() {
        assertTrue(EnchantmentRegistry.isKnown("FLAMING"), "a player typing in caps is not an error");
        assertTrue(EnchantmentRegistry.isKnown("  frost  "));
        // AC2 of US-8.5 named three inputs; the test only ever checked two of them.
        assertTrue(EnchantmentRegistry.isKnown("Vampiric"), "mixed case is the third input");
        Weapon w = EnchantmentRegistry.apply("  FLAMING ", new BasicWeapon("Iron Sword", 3.0, 40, 8));
        assertEquals("Flaming Iron Sword", w.getName());
        assertFalse(EnchantmentRegistry.isKnown("sparkly"), "unknown is still unknown");
    }

    @Test
    void theMonsterStateEventIsSpelledCorrectly() {
        assertNotNull(EventType.valueOf("MONSTER_STATE_CHANGED"));
        for (EventType t : EventType.values()) {
            assertFalse(t.name().contains("SATE"), "left-over typo: " + t.name());
        }
    }

    // ===================== US-9.4 : carried from Sprint 9 =====================

    @Test
    void theDamageWarningTracksThePlayersOwnMaximumNotTheStartingConstant() {
        Player p = new Player("Tester");
        EventBus bus = new EventBus();
        List<String> heard = new ArrayList<>();
        bus.subscribe(e -> { if (e.getType() == EventType.MESSAGE) heard.add(e.getString("text")); });
        // Trap 1: the constructor takes the bus but does not subscribe itself.
        bus.subscribe(new DamageMeter(bus, p, 0.25));

        int originalMax = p.getMaxHp();             // 80 from config
        p.setMaxHp(originalMax * 2);                // 160
        // A quarter of the old max is 20, a quarter of the new is 40. 30 sits between them:
        // before US-9.4 the level came from playerStartingHp, so 30 looked safe.
        int between = (int) Math.round(0.25 * originalMax) + 10;

        // Trap 2: the meter warns once and then goes quiet, so the "must NOT fire" case comes first.
        p.setHp((int) Math.round(0.25 * p.getMaxHp()) + 10);   // 50 of 160: comfortable
        bus.publish(GameEvent.of(EventType.DAMAGE_TAKEN, "amount", 1));
        assertTrue(heard.stream().noneMatch(s -> s.contains("DANGER")),
                "50 of 160 is nowhere near death");

        p.setHp(between);                           // 30 of 160
        bus.publish(GameEvent.of(EventType.DAMAGE_TAKEN, "amount", 1));
        assertTrue(heard.stream().anyMatch(s -> s.contains("DANGER")),
                "30 of 160 is below a quarter, so the warning must fire");
    }

    @Test
    void setMaxHpClampsToOneAndPullsCurrentHpDown() {
        Player p = new Player("Tester");
        p.setMaxHp(0);
        assertEquals(1, p.getMaxHp(), "a maximum below 1 is clamped to 1");
        assertEquals(1, p.getHp(), "current hp follows the maximum down");
    }
}
