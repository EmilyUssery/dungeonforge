package dungeonforge.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton holding every tunable value for the game.
 *
 * Loads src/main/resources/data/config.json from the classpath. If the file is
 * missing or unreadable, documented defaults are used instead so the game can
 * always run (AC4).
 */
public final class GameConfig {

    private static GameConfig instance;

    private final Map<String, Object> defaults = new HashMap<>();
    private final Map<String, Object> settings = new HashMap<>();

    private GameConfig() {
        loadDefaults();
        loadFromFile();
    }

    public static synchronized GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    /** Resets the singleton so tests don't leak state into each other. */
    public static synchronized void resetForTests() {
        instance = null;
    }

    private void loadDefaults() {
        defaults.put("playerStartingHp", 60.0);
        defaults.put("playerStartingAttack", 8.0);
        defaults.put("playerStartingDefense", 2.0);
        defaults.put("playerCarryCapacity", 60.0);
        defaults.put("dungeonDepth", 3.0);
        defaults.put("roomsPerLevel", 8.0);
        defaults.put("maxMonstersPerRoom", 2.0);
        defaults.put("randomSeed", 42.0);
    }

       private void loadFromFile() {
        String text = readResource("/data/config.json");
        if (text == null) {
            return; // no file -> defaults stand (AC4)
        }
        try {
            settings.putAll(Json.parseObject(text));
        } catch (Exception e) {
            // Any parse failure -> defaults stand (AC4)
        }
    }

    /** Reads a classpath resource as a UTF-8 string, or null if it can't be read. */
    public static String readResource(String path) {
        try (InputStream in = GameConfig.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private Object get(String key) {
        if (settings.containsKey(key)) {
            return settings.get(key);
        }
        return defaults.get(key);
    }

    public int getInt(String key) {
        return ((Number) get(key)).intValue();
    }

    public double getDouble(String key) {
        return ((Number) get(key)).doubleValue();
    }

    public long getLong(String key) {
        return ((Number) get(key)).longValue();
    }
}