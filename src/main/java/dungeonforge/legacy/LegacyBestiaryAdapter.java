package dungeonforge.legacy;

import dungeonforge.factory.MonsterDef;
import dungeonforge.factory.MonsterSource;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 12 (US-9.1) -- ADAPTER (Head First Ch. 7).
 *
 *   Target  : MonsterSource       -- what DungeonForge wants: a List<MonsterDef>
 *   Adaptee : LegacyBestiary      -- what the previous team left: a cursor of Strings
 *   Client  : MonsterFactory      -- unchanged, and it never learns a CSV exists
 *
 * Note the SHAPE: it implements one type and holds a DIFFERENT one. WeaponDecorator (Week 9)
 * has the same mechanics -- implement, hold, delegate -- but it holds a Weapon and IS a Weapon,
 * because it adds behaviour. This holds a LegacyBestiary and is a MonsterSource, because it
 * changes the interface. Same mechanics, opposite intent.
 *
 * The honest half: an adapter cannot invent what the adaptee never had. The file has no xp
 * column, and one AI name has no counterpart here. Both are derived/defaulted AND recorded in
 * translationNotes(), so a derived number never looks like a read one.
 */
public class LegacyBestiaryAdapter implements MonsterSource {

    private final LegacyBestiary adaptee;
    private final String sourceName;
    private final List<String> notes = new ArrayList<>();

    public LegacyBestiaryAdapter(LegacyBestiary adaptee, String sourceName) {
        this.adaptee = adaptee;
        this.sourceName = sourceName;
    }

    @Override
    public String sourceName() { return sourceName; }

    @Override
    public List<MonsterDef> definitions() {
        notes.clear();                       // notes describe THIS translation, not the last one
        adaptee.rewind();                    // a cursor is single-use; hiding that is our job (AC9)
        List<MonsterDef> out = new ArrayList<>();
        while (adaptee.advance()) {
            String legacyId = adaptee.field("MOB_ID");
            int hp = hitPointsFrom(adaptee.field("HIT_DICE"));
            int attack = attackFrom(adaptee.field("DMG"));
            out.add(new MonsterDef(
                    legacyId.toLowerCase(),
                    adaptee.field("DISPLAY"),
                    hp,
                    attack,
                    experienceFor(legacyId, hp, attack),
                    themeFrom(adaptee.field("ZONE")),
                    "BOSS".equalsIgnoreCase(adaptee.field("TIER")),
                    strategyFrom(legacyId, adaptee.field("AI"))));
        }
        return out;
    }

    /** What this adapter had to decide rather than read. One note per decision. */
    public List<String> translationNotes() { return new ArrayList<>(notes); }

    // ------------------------------------------------------------ the translations

    /** HIT_DICE "4" -> 4 * 4 + 2 = 18. */
    private static int hitPointsFrom(String hitDice) {
        return Math.max(1, Integer.parseInt(hitDice.trim())) * 4 + 2;
    }

    /** DMG "2d6+3" -> count * floor((sides + 1) / 2) + bonus = 2 * 3 + 3 = 9. "1d4" has no bonus. */
    private static int attackFrom(String dice) {
        String expr = dice.trim().toLowerCase();
        int d = expr.indexOf('d');
        int plus = expr.indexOf('+');
        int count = Integer.parseInt(expr.substring(0, d));
        int sides = Integer.parseInt(plus < 0 ? expr.substring(d + 1) : expr.substring(d + 1, plus));
        int bonus = plus < 0 ? 0 : Integer.parseInt(expr.substring(plus + 1));
        return count * ((sides + 1) / 2) + bonus;
    }

    /** ZONE 1..4 -> the theme names this engine uses. */
    private static String themeFrom(String zone) {
        return switch (zone.trim()) {
            case "1" -> "crypt";
            case "2" -> "forge";
            case "3" -> "frost";
            case "4" -> "winterfell";
            default  -> throw new IllegalArgumentException("unknown legacy ZONE: " + zone);
        };
    }

    /**
     * The file has NO xp column -- the 0.4 engine's xp table was never exported. Of the three
     * honest options (refuse, default, derive) this DERIVES it from what the file does have,
     * and records that it did, for every record.
     */
    private int experienceFor(String legacyId, int hp, int attack) {
        int xp = Math.max(1, hp / 2 + attack);
        notes.add(legacyId + ": xp derived as hp/2 + attack = " + xp + " (no xp column in the export)");
        return xp;
    }

    /** AI -> strategy name. Anything without a counterpart falls back to aggressive, out loud. */
    private String strategyFrom(String legacyId, String ai) {
        return switch (ai.trim().toUpperCase()) {
            case "BRAWLER" -> "aggressive";
            case "ARCHER"  -> "ranged";
            case "COWARD"  -> "skittish";
            case "MEDIC"   -> "healer";
            default -> {
                notes.add(legacyId + ": AI " + ai + " has no counterpart in this engine; "
                        + "it fights as aggressive instead");
                yield "aggressive";
            }
        };
    }
}
