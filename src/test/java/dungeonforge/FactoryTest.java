package dungeonforge;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;
import dungeonforge.factory.CryptThemeKit;
import dungeonforge.factory.ForgeThemeKit;
import dungeonforge.factory.MonsterDef;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.items.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dungeonforge.factory.ThemeRegistry;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.factory.RoomPopulator;
import dungeonforge.factory.StandardRoomPopulator;
import dungeonforge.factory.TreasureRoomPopulator;
import java.lang.reflect.Method;


import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FactoryTest {

    private MonsterFactory factory;

    @BeforeEach
    void setup() {
        GameConfig.resetForTests();
        RandomSource.resetForTests();
        factory = new MonsterFactory();
    }

    @Test
    void everyBlueprintInDataFileIsRegistered() {
        assertEquals(22, factory.blueprintCount());
        assertTrue(factory.hasBlueprint("skeleton"));
        assertTrue(factory.hasBlueprint("rime_tyrant"));
    }

    @Test
    void factoryBuildsMonsterFromBlueprint() {
        Monster m = factory.create("skeleton", 1);
        assertEquals("Skeleton", m.getSpecies());
        assertTrue(m.getMaxHp() > 0);
    }

    @Test
    void unknownIdProducesFallbackRatherThanException() {
        Monster m = assertDoesNotThrow(() -> factory.create("nonexistent_monster_xyz", 1));
        assertNotNull(m);
        assertTrue(m.getMaxHp() > 0);
    }

    @Test
    void monstersScaleUpWithDepth() {
        RandomSource.getInstance().reseed(42L);
        Monster shallow = factory.create("skeleton", 1);
        RandomSource.getInstance().reseed(42L);
        Monster deep = factory.create("skeleton", 5);
        assertTrue(deep.getMaxHp() > shallow.getMaxHp(),
                "a depth-5 skeleton should be tougher than a level-1 skeleton");
    }

    @Test
    void bossesAreExcludedFromOrdinarySpawnPool() {
        List<MonsterDef> cryptPool = factory.getByTheme("crypt", false);
        assertFalse(cryptPool.stream().anyMatch(d -> d.getId().equals("bone_tyrant")));

        List<MonsterDef> cryptBosses = factory.getByTheme("crypt", true);
        assertTrue(cryptBosses.stream().anyMatch(d -> d.getId().equals("bone_tyrant")));
    }

    @Test
    void themeKitOnlyProducesItsOwnFamily() {
        CryptThemeKit crypt = new CryptThemeKit(factory);
        Set<String> cryptNames = Set.of("Skeleton", "Crypt Rat", "Wight", "Bone Priest", "Ghoul", "Grave Moth");

        for (int i = 0; i < 50; i++) {
            Monster m = crypt.createMonster(1);
            assertTrue(cryptNames.contains(m.getSpecies()),
                    "CryptThemeKit must never hand back a non-crypt monster: " + m.getSpecies());
        }
    }

    @Test
    void differentKitsProduceDifferentFamilies() {
        CryptThemeKit crypt = new CryptThemeKit(factory);
        ForgeThemeKit forge = new ForgeThemeKit(factory);

        assertEquals("Bone Tyrant", crypt.createBoss(3).getSpecies());
        assertEquals("Forge Tyrant", forge.createBoss(3).getSpecies());
        assertNotEquals(crypt.themeName(), forge.themeName());
    }

    @Test
    void kitProducesLootAndFlavorAsWellAsMonsters() {
        ForgeThemeKit forge = new ForgeThemeKit(factory);
        Item loot = forge.createLoot(1);
        assertNotNull(loot);
        assertTrue(loot.getValue() > 0);
        assertFalse(forge.createRoomFlavor().isBlank());
    }

    @Test
    void everyRegisteredThemeIsReachableByDepth() {
        ThemeRegistry registry = new ThemeRegistry(factory);
        assertEquals(4, registry.size());
        assertEquals("crypt", registry.forDepth(1).themeName());
        assertEquals("forge", registry.forDepth(2).themeName());
        assertEquals("frost", registry.forDepth(3).themeName());
        assertEquals("swamp", registry.forDepth(4).themeName());
        assertEquals("crypt", registry.forDepth(5).themeName());
    }

    @Test
    void everyLevelIsInternallyConsistent() {
        GameWorld world = new GameWorld(new Player("Tester"));

        for (DungeonLevel level : world.getLevels()) {
            Set<String> allowed = switch (level.getThemeName()) {
                case "crypt" -> Set.of("Skeleton", "Crypt Rat", "Wight", "Bone Priest", "Ghoul", "Grave Moth", "Bone Tyrant");
                case "forge" -> Set.of("Imp", "Slag Hound", "Ember Sprite", "Forge Golem", "Forge Tyrant");
                case "frost" -> Set.of("Frost Wight", "Rime Stalker", "Ice Lurker", "Hoar Shade", "Rime Tyrant");
                case "swamp" -> Set.of("Bog Leech", "Mire Stalker", "Swamp Troll", "Will-o'-Wisp", "Bog Matriarch");
                default -> Set.of();
            };

            for (Room room : level.getRooms()) {
                for (Monster m : room.getMonsters()) {
                    assertTrue(allowed.contains(m.getSpecies()),
                            m.getSpecies() + " does not belong on a " + level.getThemeName() + " level");
                }
            }
        }
    }
    @Test
    void populateIsFinalSoSubclassesCannotBreakTheOrder() throws NoSuchMethodException {
        Method populate = RoomPopulator.class.getDeclaredMethod("populate", Room.class, int.class);
        assertTrue(java.lang.reflect.Modifier.isFinal(populate.getModifiers()),
                "populate() must be final -- the order is the invariant this class protects");
    }

    @Test
    void createEncounterIsAbstractSoEverySubclassMustAnswerIt() throws NoSuchMethodException {
        Method createEncounter = RoomPopulator.class.getDeclaredMethod("createEncounter", int.class);
        assertTrue(java.lang.reflect.Modifier.isAbstract(createEncounter.getModifiers()),
                "createEncounter() must be abstract -- this is the factory method subclasses implement");
    }

    @Test
    void standardRoomGetsNoChest() {
        RoomPopulator populator = new StandardRoomPopulator(new CryptThemeKit(factory));
        Room room = new Room("test-room");
        populator.populate(room, 1);

        assertFalse(room.getChest() != null && !room.getChest().isEmpty());
        assertFalse(room.getFlavor().isBlank(), "populate() must always set flavor text");
    }

    @Test
    void treasureRoomGetsChestWithThemeAppropriateLoot() {
        RoomPopulator populator = new TreasureRoomPopulator(new ForgeThemeKit(factory));
        Room room = new Room("test-room");
        populator.populate(room, 2);

        assertTrue(room.getChest() != null);
        assertTrue(room.getChest().getContents().size() >= 2);
        assertEquals(1, room.getMonsters().size(), "a treasure room has exactly one guard");
    }

    @Test
    void finalRoomOfTheDungeonHoldsTheBoss() {
        GameWorld world = new GameWorld(new Player("Tester"));
        List<DungeonLevel> levels = world.getLevels();
        DungeonLevel lastLevel = levels.get(levels.size() - 1);
        Room finalRoom = lastLevel.getRooms().get(lastLevel.getRooms().size() - 1);

        assertTrue(finalRoom.getMonsters().stream().anyMatch(m -> m.getSpecies().contains("Tyrant") || m.getSpecies().contains("Matriarch")),
                "the last room of the last level should contain a boss");
    }

    @Test
    void theWorldStillGeneratesDeterministicallyFromASeed() {
        RandomSource.getInstance().reseed(77L);
        int first = new GameWorld(new Player("A")).totalMonsters();
        RandomSource.getInstance().reseed(77L);
        int second = new GameWorld(new Player("B")).totalMonsters();
        assertEquals(first, second, "singletons must still guarantee a deterministic world");
    }


}