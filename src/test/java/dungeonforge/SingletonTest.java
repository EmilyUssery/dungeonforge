package dungeonforge;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;

/**
 * WEEK 3 (US-1.3) -- the Singleton rule is enforced, not hoped for.
 */
class SingletonTest {

    @BeforeEach
    void resetSingletons() {
        // Singletons are global state -- without this, one test's
        // GameConfig/RandomSource leaks into the next test.
        GameConfig.resetForTests();
        RandomSource.resetForTests();
    }

    @Test
    void getInstanceAlwaysReturnsTheSameObject_gameConfig() {
        GameConfig first = GameConfig.getInstance();
        GameConfig second = GameConfig.getInstance();
        assertSame(first, second);
    }

    @Test
    void getInstanceAlwaysReturnsTheSameObject_randomSource() {
        RandomSource first = RandomSource.getInstance();
        RandomSource second = RandomSource.getInstance();
        assertSame(first, second);
    }

    @Test
    void gameConfigConstructorIsPrivate() throws Exception {
        Constructor<?> constructor = GameConfig.class.getDeclaredConstructors()[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    void randomSourceConstructorIsPrivate() throws Exception {
        Constructor<?> constructor = RandomSource.class.getDeclaredConstructors()[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    void sameSeedProducesIdenticalSequence() {
        RandomSource.getInstance().reseed(12345L);
        int[] first = new int[10];
        for (int i = 0; i < 10; i++) first[i] = RandomSource.getInstance().nextInt(100);

        RandomSource.getInstance().reseed(12345L);
        int[] second = new int[10];
        for (int i = 0; i < 10; i++) second[i] = RandomSource.getInstance().nextInt(100);

        assertArrayEquals(first, second);
    }

    @Test
    void differentSeedProducesDifferentSequence() {
        RandomSource.getInstance().reseed(12345L);
        int[] first = new int[10];
        for (int i = 0; i < 10; i++) first[i] = RandomSource.getInstance().nextInt(100);

        RandomSource.getInstance().reseed(99999L);
        int[] second = new int[10];
        for (int i = 0; i < 10; i++) second[i] = RandomSource.getInstance().nextInt(100);

        assertNotEquals(java.util.Arrays.toString(first), java.util.Arrays.toString(second));
    }

    @Test
    void sameSeedProducesSameDungeon() {
        RandomSource.getInstance().reseed(12345L);
        GameWorld worldOne = new GameWorld(new Player("Tester"));
        int monstersOne = worldOne.totalMonsters();

        RandomSource.getInstance().reseed(12345L);
        GameWorld worldTwo = new GameWorld(new Player("Tester"));
        int monstersTwo = worldTwo.totalMonsters();

        assertEquals(monstersOne, monstersTwo);
    }
}