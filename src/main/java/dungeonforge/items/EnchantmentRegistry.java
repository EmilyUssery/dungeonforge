package dungeonforge.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * WEEK 9 (US-7.2) -- replaces the 108-line forge ladder of type tests.
 *
 * Each enchantment is ONE line: a name and a constructor reference. Every decorator takes a
 * Weapon and is a Weapon, so each constructor is a UnaryOperator<Weapon> -- weapon in,
 * weapon out -- and they chain to any depth, in any order, repeats included.
 *
 * There is no branch that gives back nothing: a known name always wraps, an unknown one throws.
 */
public final class EnchantmentRegistry {

    private static final Map<String, UnaryOperator<Weapon>> SPELLS = new LinkedHashMap<>();

    static {
        SPELLS.put("flaming",  FlamingWeapon::new);
        SPELLS.put("frost",    FrostWeapon::new);
        SPELLS.put("vampiric", VampiricWeapon::new);
    }

    private EnchantmentRegistry() { }

    /** The enchantment names, in registration order. A copy: callers cannot edit the registry. */
    public static List<String> known() { return new ArrayList<>(SPELLS.keySet()); }

    public static boolean isKnown(String enchantment) {
        return enchantment != null && SPELLS.containsKey(key(enchantment));
    }

    /** Wraps {@code weapon} in the named enchantment. Throws for an unknown name. */
    public static Weapon apply(String enchantment, Weapon weapon) {
        UnaryOperator<Weapon> spell = enchantment == null ? null : SPELLS.get(key(enchantment));
        if (spell == null) {
            throw new IllegalArgumentException("No such enchantment: " + enchantment
                    + " (known: " + String.join(", ", known()) + ")");
        }
        return spell.apply(weapon);
    }

    private static String key(String enchantment) {
        return enchantment.trim().toLowerCase(Locale.ROOT);
    }
}
