package dungeonforge.config;

import java.util.List;
import java.util.Random;

/**
 * Singleton wrapping exactly one seeded java.util.Random.
 *
 * This is the ONLY source of randomness allowed in the project. The seed comes from
 * GameConfig, so the same seed always reproduces the same sequence.
 *
 * WEEK 5: added between()/pick()/getSeed() helpers -- no new randomness sources, just
 * convenience methods on the one that already exists.
 */
public final class RandomSource {

    private static RandomSource instance;

    private Random random;
    private long seed;

    private RandomSource() {
        seed = GameConfig.getInstance().getLong("randomSeed");
        random = new Random(seed);
    }

    public static synchronized RandomSource getInstance() {
        if (instance == null) {
            instance = new RandomSource();
        }
        return instance;
    }

    public static synchronized void resetForTests() {
        instance = null;
    }

    public void reseed(long seed) {
        this.seed = seed;
        random = new Random(seed);
    }

    public long getSeed() { return seed; }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    /** A random integer in [lo, hi], inclusive on both ends. */
    public int between(int lo, int hi) {
        return lo + random.nextInt(hi - lo + 1);
    }

    public <T> T pick(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }

    public <T> T pick(T[] items) {
        return items[random.nextInt(items.length)];
    }
}
