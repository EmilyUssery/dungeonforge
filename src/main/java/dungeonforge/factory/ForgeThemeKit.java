package dungeonforge.factory;

import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;
import dungeonforge.items.*;

import java.util.List;

public class ForgeThemeKit implements ThemeKit {

    private final MonsterFactory factory;

    private static final String[] FLAVORS = {
            "Heat rolls off the walls in slow waves.",
            "Cinders drift up from cracks in the floor.",
            "Somewhere below, a hammer strikes. No one is holding it.",
            "The stone here has been melted and reset, badly."
    };

    public ForgeThemeKit(MonsterFactory factory) {
        this.factory = factory;
    }

    @Override
    public String themeName() { return "forge"; }

    @Override
    public Monster createMonster(int depth) {
        List<MonsterDef> pool = factory.getByTheme("forge", false);
        MonsterDef chosen = pool.get(RandomSource.getInstance().nextInt(pool.size()));
        return factory.create(chosen.getId(), depth);
    }

    @Override
    public Monster createBoss(int depth) {
        return factory.create("forge_tyrant", depth);
    }

    @Override
    public Item createLoot(int depth) {
        switch (RandomSource.getInstance().nextInt(4)) {
            case 0: return new Weapon("Slag Cleaver", 3.5, 55 + depth * 10, 6 + depth);
            case 1: return new Armor("Scorched Plate", 4.0, 45 + depth * 10, 4 + depth);
            case 2: return new Potion("Quench Drops", 0.5, 30, 22 + depth * 3);
            default: return new Treasure("Ingot of Bright Iron", 1.5, 80 + depth * 18);
        }
    }

    @Override
    public String createRoomFlavor() {
        return FLAVORS[RandomSource.getInstance().nextInt(FLAVORS.length)];
    }
}