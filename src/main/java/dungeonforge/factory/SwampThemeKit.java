package dungeonforge.factory;

import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;
import dungeonforge.items.*;

import java.util.List;

public class SwampThemeKit implements ThemeKit {

    private final MonsterFactory factory;

    private static final String[] FLAVORS = {
            "The mud pulls at your boots, unwilling to let go.",
            "Something croaks once, then falls silent.",
            "A green haze clings low to the water.",
            "The reeds move against the wind."
    };

    public SwampThemeKit(MonsterFactory factory) {
        this.factory = factory;
    }

    @Override
    public String themeName() { return "swamp"; }

    @Override
    public Monster createMonster(int depth) {
        List<MonsterDef> pool = factory.getByTheme("swamp", false);
        MonsterDef chosen = pool.get(RandomSource.getInstance().nextInt(pool.size()));
        return factory.create(chosen.getId(), depth);
    }

    @Override
    public Monster createBoss(int depth) {
        return factory.create("bog_matriarch", depth);
    }

    @Override
    public Item createLoot(int depth) {
        switch (RandomSource.getInstance().nextInt(4)) {
            case 0: return new Weapon("Waterlogged Spear", 3.0, 45 + depth * 9, 5 + depth);
            case 1: return new Armor("Reed-Woven Hide", 2.5, 40 + depth * 8, 3 + depth);
            case 2: return new Potion("Murky Tonic", 0.5, 26, 19 + depth * 3);
            default: return new Treasure("Sunken Coin Pouch", 1.0, 70 + depth * 16);
        }
    }

    @Override
    public String createRoomFlavor() {
        return FLAVORS[RandomSource.getInstance().nextInt(FLAVORS.length)];
    }
}