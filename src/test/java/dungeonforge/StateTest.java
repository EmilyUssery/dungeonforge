package dungeonforge;

import dungeonforge.behavior.AggressiveStrategy;
import dungeonforge.behavior.CombatStrategy;
import dungeonforge.behavior.HealerStrategy;
import dungeonforge.behavior.SkittishStrategy;
import dungeonforge.commands.Command;
import dungeonforge.commands.CommandHistory;
import dungeonforge.commands.CommandParser;
import dungeonforge.commands.GameContext;
import dungeonforge.commands.NoCommand;
import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Combat;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.EventBus;
import dungeonforge.events.QuestTracker;
import dungeonforge.states.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** WEEK 8 -- Sprint 6. The State pattern, at two levels. */
class StateTest {

    private GameContext ctx;
    private CommandParser parser;
    private CommandHistory history;
    private Player player;

    @BeforeEach
    void setUp() {
        GameConfig.resetForTests();
        RandomSource.resetForTests();

        player = new Player("Tester");
        GameWorld world = new GameWorld(player);
        EventBus bus = new EventBus();
        QuestTracker quests = new QuestTracker(bus);
        AchievementSystem achievements = new AchievementSystem(bus);
        history = new CommandHistory();
        ctx = new GameContext(world, player, bus, new Combat(bus), history);
        parser = new CommandParser(quests, achievements);
    }

    private void run(String input) {
        Command c = parser.parse(input, ctx);
        c.execute();
        history.push(c);
    }

    private Monster monster(String name, int hp, int atk) {
        Monster m = new Monster(name, hp, atk, 5);
        m.setStrategy(new AggressiveStrategy());
        return m;
    }

    // ---------- US-6.1: the game has modes ----------

    @Test
    void aQuietRoomMeansExploring() {
        assertInstanceOf(ExploringState.class, ctx.getState());
    }

    @Test
    void aLivingMonsterPutsTheGameIntoCombat() {
        ctx.getCurrentRoom().getMonsters().add(monster("Skeleton", 20, 3));
        ctx.syncState();
        assertInstanceOf(CombatState.class, ctx.getState());
    }

    @Test
    void clearingTheRoomReturnsToExploring() {
        Monster m = monster("Skeleton", 5, 1);
        ctx.getCurrentRoom().getMonsters().add(m);
        ctx.syncState();
        assertInstanceOf(CombatState.class, ctx.getState());

        run("attack");
        assertFalse(m.isAlive());
        ctx.syncState();
        assertInstanceOf(ExploringState.class, ctx.getState());
    }

    /** ILLEGAL, not merely rejected: combat does not list movement at all. */
    @Test
    void movementIsNotAvailableDuringCombat() {
        ctx.getCurrentRoom().getMonsters().add(monster("Skeleton", 30, 2));
        ctx.syncState();

        assertFalse(ctx.allowedVerbs().contains("north"));
        assertInstanceOf(NoCommand.class, parser.parse("north", ctx));
    }

    @Test
    void attackIsNotAvailableWhileExploring() {
        assertFalse(ctx.allowedVerbs().contains("attack"));
        assertInstanceOf(NoCommand.class, parser.parse("attack", ctx));
    }

    /** The rule lives in ONE place now, not in a guard inside each command. */
    @Test
    void commandsNoLongerCarryTheirOwnModeGuards() throws Exception {
        for (String file : new String[]{"MoveCommand", "TakeCommand", "DropCommand"}) {
            String source = java.nio.file.Files.readString(
                    java.nio.file.Path.of("src/main/java/dungeonforge/commands/" + file + ".java"));
            assertFalse(source.contains("hasLivingMonsters"),
                    file + " should not check the game mode itself");
        }
    }

    @Test
    void helpShowsOnlyWhatIsLegalRightNow() {
        int exploringVerbs = ctx.allowedVerbs().size();
        ctx.getCurrentRoom().getMonsters().add(monster("Skeleton", 30, 2));
        ctx.syncState();

        assertNotEquals(exploringVerbs, ctx.allowedVerbs().size(),
                "the legal verb list must change with the mode");
    }

    // ---------- US-6.2: dedicated screens ----------

    @Test
    void thePackIsItsOwnModeYouEnterAndLeaveOnPurpose() {
        run("pack");
        assertInstanceOf(InventoryState.class, ctx.getState());
        run("back");
        assertInstanceOf(ExploringState.class, ctx.getState());
    }

    /** A mode the player chose must not be yanked away by the world changing. */
    @Test
    void openingThePackSurvivesAMonsterArriving() {
        run("pack");
        ctx.getCurrentRoom().getMonsters().add(monster("Skeleton", 20, 3));
        ctx.syncState();

        assertInstanceOf(InventoryState.class, ctx.getState(),
                "syncState must leave a deliberately-entered sub-state alone");
    }

    @Test
    void deathIsAStateNotABoolean() {
        player.setHp(0);
        ctx.syncState();

        assertInstanceOf(GameOverState.class, ctx.getState());
        assertEquals(4, ctx.allowedVerbs().size());
        assertFalse(ctx.allowedVerbs().contains("north"));
    }

    // ---------- US-6.3: monsters have a lifecycle ----------

    @Test
    void aMonsterStartsIdleAndCannotAct() {
        Monster m = monster("Skeleton", 20, 3);
        assertInstanceOf(IdleState.class, m.getState());
        assertFalse(m.getState().canAct());
    }

    @Test
    void idleBecomesHunting() {
        Monster m = monster("Skeleton", 20, 3);
        assertInstanceOf(HuntingState.class, m.getState().next(m, player));
    }

    @Test
    void aWoundedHunterEnrages() {
        Monster m = monster("Skeleton", 20, 3);
        m.setState(new HuntingState());
        m.takeDamage(12);                 // 40% left, under the 45% enrage threshold

        assertInstanceOf(EnragedState.class, m.getState().next(m, player));
    }

    @Test
    void aBadlyWoundedMonsterFlees() {
        Monster m = monster("Skeleton", 20, 3);
        m.setState(new HuntingState());
        m.takeDamage(16);                 // 20% left, under the 30% flee threshold

        assertInstanceOf(FleeingState.class, m.getState().next(m, player));
    }

    @Test
    void deadIsTerminalAndCannotAct() {
        Monster m = monster("Skeleton", 20, 3);
        m.setState(new HuntingState());
        m.takeDamage(20);

        MonsterState next = m.getState().next(m, player);
        assertInstanceOf(DeadState.class, next);
        assertFalse(next.canAct());
        assertInstanceOf(DeadState.class, next.next(m, player), "the dead stay dead");
    }

    @Test
    void enrageAndFleeingAreTheOnlyStatesThatChangeDamage() {
        assertEquals(1.0, new HuntingState().damageMultiplier(), 0.001);
        assertEquals(0.5, new FleeingState().damageMultiplier(), 0.001);
        assertEquals(EnragedState.DAMAGE_BONUS, new EnragedState().damageMultiplier(), 0.001);
    }

    /** Combat must not enumerate state names any more. */
    @Test
    void combatNoLongerBranchesOnStateNames() throws Exception {
        String source = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/dungeonforge/core/Combat.java"));

        assertFalse(source.contains("getStateName"));
        assertFalse(source.contains("\"enraged\""));
        assertFalse(source.contains("\"fleeing\""));
        assertFalse(source.contains("\"idle\""));
    }

    // ---------- US-6.4: State and Strategy are different things ----------

    @Test
    void aMonsterHasBothAStateAndAStrategy() {
        Monster m = monster("Bone Priest", 20, 3);
        m.setStrategy(new HealerStrategy());

        assertNotNull(m.getState());
        assertNotNull(m.getStrategy());
        assertEquals("healer", m.getStrategy().name());
        assertEquals("idle", m.getState().label());
    }

    /** THE demonstration: a STATE transition can change the STRATEGY. */
    @Test
    void enteringRageMakesEvenAHealerAggressive() {
        Monster priest = monster("Bone Priest", 20, 3);
        priest.setStrategy(new HealerStrategy());
        assertEquals("healer", priest.getStrategy().name());

        priest.setState(new EnragedState());

        assertEquals("aggressive", priest.getStrategy().name(),
                "EnragedState.onEnter should swap the strategy -- state decides WHEN, strategy is WHAT");
    }

    @Test
    void fleeingSwapsTheStrategyToSkittish() {
        Monster m = monster("Slag Hound", 20, 3);
        m.setState(new FleeingState());
        assertInstanceOf(SkittishStrategy.class, m.getStrategy());
    }

    /** ...but the arrow only points one way. A strategy never sets a state. */
    @Test
    void changingStrategyDoesNotChangeState() {
        Monster m = monster("Skeleton", 20, 3);
        m.setState(new HuntingState());

        CombatStrategy before = m.getStrategy();
        m.setStrategy(new SkittishStrategy());

        assertInstanceOf(HuntingState.class, m.getState(),
                "swapping tactics must not move the monster through its lifecycle");
        assertNotSame(before, m.getStrategy());
    }

    // ---------- regression ----------

    @Test
    void earlierWeeksStillHold() {
        assertEquals(19, ctx.getWorld().getMonsterFactory().blueprintCount(), "Week 4");

        Room start = ctx.getCurrentRoom();
        run("north");
        assertNotSame(start, ctx.getCurrentRoom());
        assertTrue(history.undoLast(), "Week 7 undo");
        assertSame(start, ctx.getCurrentRoom());

        RandomSource.getInstance().reseed(7L);
        int a = new GameWorld(new Player("A")).totalMonsters();
        RandomSource.getInstance().reseed(7L);
        int b = new GameWorld(new Player("B")).totalMonsters();
        assertEquals(a, b, "Week 3 determinism");
    }

    // ---------- US-6.7 (carried over from Sprint 6): one answer to "is the game over?" ----------

    /** Strip comments first, so a comment that MENTIONS the flag cannot trip the search. */
    private static String codeOnly(String source) {
        return source.replaceAll("(?s)/\\*.*?\\*/", "")
                     .replaceAll("(?m)//.*$", "");
    }

    /** The body of one method, found by brace counting from its signature. */
    private static String methodBody(String source, String signature) {
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "could not find " + signature);
        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == '{') depth++;
            if (ch == '}' && --depth == 0) return source.substring(open, i + 1);
        }
        throw new AssertionError("unbalanced braces after " + signature);
    }

    @Test
    void onlyGameOverIsTerminal() {
        assertTrue(new GameOverState().isTerminal());
        assertFalse(new ExploringState().isTerminal());
        assertFalse(new CombatState().isTerminal());
        assertFalse(new InventoryState().isTerminal());
    }

    @Test
    void deathIsTerminalButTheSessionKeepsRunning() {
        Monster m = monster("Ogre", 200, 50);
        ctx.getCurrentRoom().getMonsters().add(m);
        ctx.syncState();
        m.setState(new HuntingState());
        player.setHp(1);

        run("attack");
        ctx.syncState();

        assertFalse(player.isAlive(), "a 50-attack ogre should finish a 1 HP player");
        assertInstanceOf(GameOverState.class, ctx.getState(), "death must land in Game Over");
        assertTrue(ctx.getState().isTerminal());
        assertTrue(ctx.isRunning(), "the adventure is over; the session is not");
    }

    @Test
    void aTerminalStateIsSomewhereYouStayEvenWhenHealed() {
        run("quit");
        assertInstanceOf(GameOverState.class, ctx.getState());

        player.setHp(player.getMaxHp());
        ctx.syncState();
        assertInstanceOf(GameOverState.class, ctx.getState(),
                "a healed quitter must not be dragged back into Exploring");

        player.setHp(0);
        ctx.syncState();
        player.setHp(10);
        ctx.syncState();
        assertInstanceOf(GameOverState.class, ctx.getState());
    }

    @Test
    void gameOverOffersItsFourVerbsAndNoMovement() {
        player.setHp(0);
        ctx.syncState();
        for (String verb : new String[] { "quests", "history", "status", "quit" }) {
            assertTrue(ctx.allowedVerbs().contains(verb), verb + " must be reachable after death");
            assertFalse(parser.parse(verb, ctx) instanceof NoCommand, verb + " must parse in Game Over");
        }
        assertFalse(ctx.allowedVerbs().contains("north"));
        assertInstanceOf(NoCommand.class, parser.parse("north", ctx));
    }

    @Test
    void quitEndsTheAdventureAndTheSession() {
        run("quit");
        assertTrue(ctx.getState().isTerminal(), "quit must end the adventure");
        assertFalse(ctx.isRunning(), "quit must end the session");
    }

    @Test
    void syncStateNoLongerReadsTheSessionFlag() throws Exception {
        String source = codeOnly(java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/dungeonforge/commands/GameContext.java")));
        String body = methodBody(source, "void syncState()");
        assertFalse(body.contains("running"), "syncState decides the MODE; it must not read the session flag");
        assertFalse(body.contains("isRunning"));
    }

    @Test
    void mainAsksOneQuestionAboutWhetherToKeepGoing() throws Exception {
        String main = codeOnly(java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/dungeonforge/Main.java")));
        assertTrue(main.contains("while (ctx.isRunning())"), "the loop asks exactly one question");
        assertTrue(main.contains("if (!ctx.isRunning()) break;"), "so does the script runner");
        assertFalse(main.contains("isRunning() &&"), "no second clause on the loop");
        assertFalse(main.contains("isRunning() ||"), "no second clause in the script runner");
    }
}
