package dungeonforge.factory;

import dungeonforge.behavior.AggressiveStrategy;
import dungeonforge.behavior.CombatStrategy;
import dungeonforge.behavior.HealerStrategy;
import dungeonforge.behavior.RangedStrategy;
import dungeonforge.behavior.SkittishStrategy;
import dungeonforge.config.GameConfig;
import dungeonforge.config.Json;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MonsterFactory {

    private final Map<String, MonsterDef> blueprints = new LinkedHashMap<>();

    public MonsterFactory() {
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        String text = GameConfig.readResource("/data/monsters.json");
        if (text == null) {
            return; // AC4: missing data file -> factory stays empty, doesn't crash
        }
        Map<String, Object> raw = Json.parseObject(text);
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> fields = (Map<String, Object>) entry.getValue();
            String name = (String) fields.get("name");
            int hp = ((Number) fields.get("hp")).intValue();
            int attack = ((Number) fields.get("attack")).intValue();
            int xp = ((Number) fields.get("xp")).intValue();
            String theme = (String) fields.get("theme");
            boolean boss = Boolean.TRUE.equals(fields.get("boss"));
            String strategy = (String) fields.get("strategy");
            blueprints.put(id, new MonsterDef(id, name, hp, attack, xp, theme, boss, strategy));
        }
    }

    public Monster create(String id, int depth) {
        MonsterDef d = blueprints.get(id);
        if (d == null) {
            // AC4: unknown id must not crash -- safe fallback, still playable
            d = new MonsterDef(id, "Wanderer", 10, 3, 2, "unknown", false);
        }
        // WEEK 5: variance lives HERE now -- the only place that adds it, since Monster's
        // own constructor no longer does.
        int hp = d.getHp() + depth * 4 + RandomSource.getInstance().nextInt(5) - 2;
        int attack = d.getAttack() + depth + RandomSource.getInstance().nextInt(3) - 1;
        Monster m = new Monster(d.getName(), hp, attack, d.getXp());
        m.setStrategy(strategyFor(d.getStrategy()));
        return m;
    }

    /** WEEK 5 (US-3.1) -- turns a name from the data file into a CombatStrategy object. */
    public CombatStrategy strategyFor(String name) {
        if (name == null) {
            return new AggressiveStrategy();
        }
        return switch (name) {
            case "ranged" -> new RangedStrategy();
            case "skittish" -> new SkittishStrategy();
            case "healer" -> new HealerStrategy();
            default -> new AggressiveStrategy();
        };
    }

    public List<MonsterDef> getByTheme(String theme, boolean bossOnly) {
        List<MonsterDef> out = new ArrayList<>();
        for (MonsterDef d : blueprints.values()) {
            if (d.getTheme().equals(theme) && d.isBoss() == bossOnly) {
                out.add(d);
            }
        }
        return out;
    }

    public int blueprintCount() {
        return blueprints.size();
    }

    public boolean hasBlueprint(String id) {
        return blueprints.containsKey(id);
    }
}
