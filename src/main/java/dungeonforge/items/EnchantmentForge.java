package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- TEMPORARY BRIDGE. Deleted in US-7.2.
 *
 * The 108-line ladder of type tests is gone along with the combination classes it chose
 * between. This stub exists only so EnchantCommand still compiles and the build stays green
 * between US-7.1 and US-7.2; until the registry replaces it, `enchant` refuses everything.
 */
public final class EnchantmentForge {

    private EnchantmentForge() { }

    public static final String[] KNOWN = { "flaming", "frost", "vampiric" };

    public static boolean isKnown(String enchantment) {
        for (String k : KNOWN) if (k.equalsIgnoreCase(enchantment)) return true;
        return false;
    }

    public static Weapon enchant(Weapon w, String enchantment) {
        return null;   // US-7.2 replaces this whole class with EnchantmentRegistry
    }
}
