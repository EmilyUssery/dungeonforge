package dungeonforge;

import dungeonforge.commands.CommandHistory;
import dungeonforge.commands.CommandParser;
import dungeonforge.commands.GameContext;
import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Combat;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.EventBus;
import dungeonforge.events.QuestTracker;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.items.Chest;
import dungeonforge.items.Item;
import dungeonforge.items.Potion;
import dungeonforge.items.Treasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** WEEK 13 -- Iterator (US-10.1). */
class IteratorCompositeTest {

    private Player player;
    private GameWorld world;

    @BeforeEach
    void setUp() {
        GameConfig.resetForTests();
        RandomSource.resetForTests();
        player = new Player("Tester");
        world = new GameWorld(player, new MonsterFactory());
    }

    private static Potion potion(String n)  { return new Potion(n, 0.4, 25, 21); }
    private static Treasure coin(String n)  { return new Treasure(n, 0.1, 5); }

    private static List<Item> drain(Iterator<Item> it) {
        List<Item> out = new ArrayList<>();
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    private static String codeOnly(String src) {
        return src.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
    }

    private static String source(String relPath) throws Exception {
        return codeOnly(java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/dungeonforge/" + relPath)));
    }

    private static int countOf(String haystack, String needle) {
        int n = 0, at = 0;
        while ((at = haystack.indexOf(needle, at)) >= 0) { n++; at += needle.length(); }
        return n;
    }

    /**
     * Puts a container in a room.
     *
     * WEEK 13 NOTE -- this helper exists because it is the ONE thing in these tests that is
     * coupled to how a room stores items. US-10.3 changes a room to keep everything in one
     * collection, and when it does, THIS METHOD is what changes. Every assertion below stays
     * exactly as it is.
     */
    private static void putContainerIn(Room room, Chest chest) {
        room.setChest(chest);
    }

    // ===================== US-10.1 : the ITERATOR =====================

    @Test
    void oneWalkReachesItemsWhereverTheRoomKeepsThem() {
        Room r = new Room("Test");
        Chest c = new Chest("Strongbox");
        c.add(potion("Inside A"));
        c.add(potion("Inside B"));
        r.getFloorItems().add(coin("Loose"));
        putContainerIn(r, c);

        List<String> names = new ArrayList<>();
        for (Item i : drain(r.contents())) names.add(i.getName());

        assertTrue(names.contains("Loose"),     "a loose floor item must be reachable");
        assertTrue(names.contains("Inside A"),  "and so must the things inside the container");
        assertTrue(names.contains("Inside B"),  "all of them, from the SAME walk");
        assertEquals(3, names.size(),           "three items, one walk");
    }

    @Test
    void theWalkIsHonestWhenOneCollectionIsEmpty() {
        Room floorOnly = new Room("A");
        floorOnly.getFloorItems().add(coin("Loose"));
        assertEquals(1, drain(floorOnly.contents()).size(), "floor only");

        Room chestOnly = new Room("B");
        Chest c = new Chest("Strongbox");
        c.add(potion("Inside"));
        putContainerIn(chestOnly, c);
        assertEquals(1, drain(chestOnly.contents()).size(),
                "an EMPTY floor and a full container is the case people get wrong");

        Room empty = new Room("C");
        assertFalse(empty.contents().hasNext(), "and nothing at all must say so");
    }

    @Test
    void removeTakesTheItemOutOfWhicheverCollectionItWasIn() {
        Room r = new Room("Test");
        Chest c = new Chest("Strongbox");
        c.add(potion("Inside"));
        r.getFloorItems().add(coin("Loose"));
        putContainerIn(r, c);

        for (Iterator<Item> it = r.contents(); it.hasNext(); ) {
            if (it.next().getName().equals("Inside")) { it.remove(); break; }
        }
        assertEquals(0, c.getContents().size(), "removed from the container");
        assertEquals(1, r.getFloorItems().size(), "and the floor is untouched");

        for (Iterator<Item> it = r.contents(); it.hasNext(); ) {
            if (it.next().getName().equals("Loose")) { it.remove(); break; }
        }
        assertEquals(0, r.getFloorItems().size(), "and now from the floor");
    }

    @Test
    void removeBeforeNextIsAnErrorRatherThanAGuess() {
        Room r = new Room("Test");
        r.getFloorItems().add(coin("Loose"));
        Iterator<Item> it = r.contents();
        assertThrows(IllegalStateException.class, it::remove);
    }

    @Test
    void takeCommandHasExactlyOneSearchPath() throws Exception {
        String code = source("commands/TakeCommand.java");
        assertFalse(code.contains("takeFromFloor"), "the two-method version is gone");
        assertFalse(code.contains("takeFromChest"), "both halves of it");
        assertEquals(1, countOf(code, "room().contents()"), "one walk, used once");
        assertEquals(1, countOf(code, "it.remove()"),
                "and it removes through the iterator rather than working around it");
    }

    @Test
    void twoOfTheSixFilesNoLongerKnowARoomHasTwoContainers() throws Exception {
        for (String f : List.of("commands/TakeCommand.java", "core/GameWorld.java")) {
            String code = source(f);
            assertFalse(code.contains("getChest()") || code.contains("hasChest()"),
                    f + " still knows a room keeps items in exactly two places");
        }
        // And the three that still do, on purpose. Each one needs to know WHERE an item is,
        // which is the one thing this iterator forgets. US-10.2 and US-10.3 fix two of them by
        // removing the second collection; describeRoom() keeps direct access for good.
        for (String f : List.of("commands/TurnSnapshot.java", "factory/RoomPopulator.java",
                                "commands/CommandParser.java")) {
            String code = source(f);
            assertTrue(code.contains("getChest()") || code.contains("hasChest()")
                    || code.contains("setChest("),
                    f + " was expected to still need direct access after US-10.1. If it does "
                      + "not, check that nothing was weakened to win a grep.");
        }
    }

    @Test
    void countingLootNoLongerForgetsTheFloor() {
        int atGeneration = world.totalLoot();
        Room start = world.startingRoom();
        start.getFloorItems().add(potion("Dropped A"));
        start.getFloorItems().add(coin("Dropped B"));

        assertEquals(atGeneration + 2, world.totalLoot(),
                "totalLoot() used to count container contents only. Generation puts nothing "
              + "loose on a floor, so the missing branch was invisible until a player dropped "
              + "something. Writing the walk ONCE is what fixed it -- not the pattern.");
    }

    // ===================== US-10.2 : the COMPOSITE =====================

    @Test
    void aChestIsAnItem() {
        assertInstanceOf(Item.class, new Chest("Strongbox"));
    }

    @Test
    void weightAndValueRollUpThroughOneLevel() {
        Chest c = new Chest("Strongbox");
        c.add(potion("P"));                 // 0.4kg, 25g
        c.add(coin("C"));                   // 0.1kg, 5g
        assertEquals(5.5, c.getWeight(), 0.001, "5.0 tare + 0.4 + 0.1");
        assertEquals(30, c.getValue(),           "25 + 5; the box itself is not treasure");
    }

    @Test
    void weightAndValueRollUpThroughNesting() {
        Chest inner = new Chest("Inner");
        inner.add(potion("P"));             // 0.4 / 25
        Chest outer = new Chest("Outer");
        outer.add(inner);
        outer.add(coin("C"));               // 0.1 / 5

        assertEquals(5.0 + (5.0 + 0.4) + 0.1, outer.getWeight(), 0.001,
                "the outer tare, the inner chest's own total, and the loose coin");
        assertEquals(30, outer.getValue(), "value passes straight through the nesting");
    }

    @Test
    void anEmptyChestStillWeighsSomething() {
        assertEquals(5.0, new Chest("Empty").getWeight(), 0.001,
                "a chest is a thing, not just a label for its contents");
        assertEquals(0, new Chest("Empty").getValue());
    }

    @Test
    void aChestCanBeCarriedLikeAnythingElse() {
        Chest c = new Chest("Strongbox");
        c.add(potion("P"));
        player.addItem(c);
        assertTrue(player.getInventory().contains(c));
    }

    @Test
    void aChestCannotContainItself() {
        Chest c = new Chest("Strongbox");
        assertThrows(IllegalArgumentException.class, () -> c.add(c));
    }

    @Test
    void leavesHaveNoChildrenAndTheCompositeDoes() {
        assertFalse(potion("P").children().hasNext(), "a potion contains nothing");
        assertFalse(potion("P").isContainer());
        Chest c = new Chest("Strongbox");
        assertFalse(c.isContainer(), "an EMPTY chest holds nothing, and says so");
        c.add(potion("P"));
        assertTrue(c.isContainer());
    }

    @Test
    void theComponentDeclaresNoMutationItCannotHonour() throws Exception {
        String code = source("items/Item.java");
        assertFalse(code.contains("void add("),
                "Head First puts add() on the Component and lets leaves throw (p. 364). We "
              + "did not. The cost is that you cannot call add() against an Item, and D2 of "
              + "the clinic asks you to defend that trade.");
        assertTrue(code.contains("children()"), "traversal belongs on the Component");
    }
}
