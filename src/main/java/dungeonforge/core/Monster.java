package dungeonforge.core;

import dungeonforge.config.RandomSource;

/**
 * WEEK 1 -- a monster.
 *
 * WEEK 3 (US-1.2): randomness now comes from the single shared RandomSource.
 */
public class Monster extends Entity {

    private final String species;
    private final int xpReward;

    public Monster(String species, int baseHp, int baseAttack, int xpReward) {
        // A little stat variance so no two monsters are identical.
        super(species,
              baseHp + RandomSource.getInstance().nextInt(5) - 2,
              baseAttack + RandomSource.getInstance().nextInt(3) - 1,
              0);
        this.species = species;
        this.xpReward = xpReward;
    }

    public String getSpecies() { return species; }
    public int getXpReward()   { return xpReward; }

    @Override
    public String describe() {
        return species + " (" + hp + "/" + maxHp + " HP, ATK " + attackPower + ")";
    }
}