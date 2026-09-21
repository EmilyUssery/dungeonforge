package dungeonforge.config;

import java.util.Random;

/**
 * Singleton wrapping exactly one seeded java.util.Random.
 *
 * This is the ONLY source of randomness allowed in the project. The seed
 * comes from GameConfig, so the same seed always reproduces the same
 * sequence -- which means the same dungeon, which means a reproducible bug.
 */
public final class RandomSource {

    private static RandomSource instance;

    private Random random;

    private RandomSource() {
        long seed = GameConfig.getInstance().getLong("randomSeed");
        random = new Random(seed);
    }

    public static synchronized RandomSource getInstance() {
        if (instance == null) {
            instance = new RandomSource();
        }
        return instance;
    }

    /** Resets the singleton so tests don't leak state into each other. */
    public static synchronized void resetForTests() {
        instance = null;
    }

    /** Re-seeds the underlying Random, e.g. from a --seed= command-line flag. */
    public void reseed(long seed) {
        random = new Random(seed);
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public double nextDouble() {
        return random.nextDouble();
    }
}