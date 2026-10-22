package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RivalLandPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Wartime land military on the free-play map, through real French and Dutch
 * owner turns. France holds Quebec at (17,10) and the armed colonist 6 starts
 * at (17,11). Montreal is the Dutch colony, and its worker moves with it, so
 * both nations keep a colony throughout.
 *
 * Three scenarios use different saved positions. A Dutch colonist next to
 * Quebec threatens it, so the armed colonist walks in and stays within three
 * rounds, and Save and Continue keeps it there. An unmounted French colonist
 * and an unmounted Dutch colonist stand side by side on bare grassland, so the
 * shared attacker win probability is exactly 60 percent (the attacker's +50%
 * gives 3 against 2): the French colonist attacks within ten rounds and the
 * battle draws from the saved combat state. With no visible target, the French
 * colonist stays put for every bounded round.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_145_F136_rival_land_military_test extends AbstractMicroColTest {

    private static final int QUEBEC_ID = 0;
    private static final int MONTREAL_ID = 1;
    private static final int QUEBEC_WORKER_ID = 4;
    private static final int MONTREAL_WORKER_ID = 5;
    private static final int RIVAL_ID = 6;
    private static final int HUMAN_ID = 0;
    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location RIVAL_START = Location.of(17, 11);
    private static final Location HUMAN_START = Location.of(18, 11);
    private static final Location FAR_HUMAN = Location.of(18, 14);
    private static final Location EQUAL_RIVAL = Location.of(18, 12);
    private static final Location EQUAL_HUMAN = Location.of(18, 13);
    /** Garrison must be in place within this many French turns. */
    private static final int DEFENCE_ROUNDS = 3;
    /** The attack must come within this many French turns. */
    private static final int OFFENCE_ROUNDS = 10;
    /**
     * Rounds with no visible target; the rival must not move in any of them.
     */
    private static final int QUIET_ROUNDS = 5;

    /** The saved game the Load command opens; this scenario swaps it. */
    private volatile File loadTarget;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenAnswer(invocation -> loadTarget);
            binder.bind(FileSelectingService.class).toInstance(selecting);
        });
    }

    /**
     * Saves a free-play game at the start of the human turn, with the two
     * nations at war. The squares listed as grassland are painted so their
     * combat bonuses are equal.
     */
    private static File saveFixture(final String name,
            final boolean quebecDutch, final Location rivalAt,
            final Location humanAt, final boolean mountedRival,
            final Location... grassland) throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        if (quebecDutch) {
            handOver(colonyPo(data, QUEBEC_ID), unitPo(data, QUEBEC_WORKER_ID));
        }
        handOver(colonyPo(data, MONTREAL_ID), unitPo(data, MONTREAL_WORKER_ID));
        if (grassland.length > 0) {
            final Map<Location, TerrainType> terrain = new HashMap<>(
                    data.getModel().getMap().getTerrainMap());
            for (final Location at : grassland) {
                terrain.put(at, TerrainType.GRASSLAND);
            }
            data.getModel().getMap().setTerrainType(terrain);
            // A tree would add 50% to the defender and change the odds.
            data.getModel().getMap().setTrees(Set.of());
        }
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            model.startGame();
            for (int guard = 0; guard < 4 && model.getCurrentPlayer() != model
                    .getPlayerByName("Dutch"); guard++) {
                model.getCurrentPlayer().endTurn();
            }
            if (model.getCurrentPlayer() != model.getPlayerByName("Dutch")) {
                throw new IllegalStateException("The human turn never came");
            }
            final UnitFreeColonist rival = (UnitFreeColonist) model
                    .getUnitById(RIVAL_ID);
            rival.placeToLocation(rivalAt);
            rival.setHoldingGuns(true);
            rival.setMounted(mountedRival);
            final UnitFreeColonist human = (UnitFreeColonist) model
                    .getUnitById(HUMAN_ID);
            human.placeToLocation(humanAt);
            human.setHoldingGuns(true);
            model.declareHostilities(model.getPlayerByName("Dutch"),
                    model.getPlayerByName("France"));
            data.setModel(model.save());
            // The first battle is won by an attacker at 60 percent and promotes
            // nobody, so the outcome is known before the turn is played.
            data.getModel().setCombatRandomState(winningCombatState());
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve(name);
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    /**
     * First saved combat state whose first draw is below 0.6 and whose second
     * draw is at least 0.25, read from the model's own generator.
     */
    private static long winningCombatState() {
        final long gamma = 0x9E3779B97F4A7C15L;
        for (long state = 0;; state++) {
            if (Model.combatRandomAfter(state) < 0.6
                    && Model.combatRandomAfter(state + gamma) >= 0.25) {
                return state;
            }
        }
    }

    private static ColonyPo colonyPo(final GameModelPo data, final int id) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    /** A colony handed to the Dutch takes its resident worker with it. */
    private static void handOver(final ColonyPo colony, final UnitPo worker) {
        colony.setOwnerName("Dutch");
        colony.setLastColonialAffiliation("Dutch");
        worker.setOwnerId("Dutch");
    }

    @Test
    @Tag("local")
    void TC_01_threatened_colony_gets_a_garrison_and_survives_continue()
            throws Exception {
        loadTarget = saveFixture("F136-defence.microcol", false, RIVAL_START,
                HUMAN_START, false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalLandPage rival = new RivalLandPage(getContext());
        rival.verifyThreatReady(QUEBEC_ID, QUEBEC, RIVAL_ID, RIVAL_START,
                HUMAN_ID, HUMAN_START);
        for (int turn = 0; turn < DEFENCE_ROUNDS
                && !rival.isGarrisoned(RIVAL_ID, QUEBEC); turn++) {
            final int next = rival.readRound() + 1;
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyThreatTurn("Round " + next, RIVAL_ID, HUMAN_ID);
        }
        rival.verifyGarrisonWithinRounds(RIVAL_ID, QUEBEC, DEFENCE_ROUNDS);
        final String before = rival.describeRival(RIVAL_ID);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.waitForHumanRound(rival.readRound());
        rival.verifyGarrisonUnchanged(before, RIVAL_ID);
        for (int turn = 0; turn < QUIET_ROUNDS; turn++) {
            final int next = rival.readRound() + 1;
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyThreatTurn("Round " + next, RIVAL_ID, HUMAN_ID);
            rival.verifyOwnedColonyDefended(QUEBEC_ID, QUEBEC, RIVAL_ID);
        }
    }

    @Test
    @Tag("local")
    void TC_02_rival_attacks_an_exactly_sixty_percent_colonist_within_ten_rounds()
            throws Exception {
        loadTarget = saveFixture("F136-offence.microcol", false, EQUAL_RIVAL,
                EQUAL_HUMAN, false, EQUAL_RIVAL, EQUAL_HUMAN);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalLandPage rival = new RivalLandPage(getContext());
        rival.verifyOffenceReady(RIVAL_ID, HUMAN_ID);
        boolean fought = false;
        for (int turn = 0; turn < OFFENCE_ROUNDS && !fought; turn++) {
            final int next = rival.readRound() + 1;
            final String combatBefore = rival.readCombatState();
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyOffenceTurn("Round " + next, RIVAL_ID, HUMAN_ID);
            fought = rival.isBattleFought(combatBefore);
        }
        // The pinned draw wins for France: the Dutch colonist keeps its square
        // and owner but loses its muskets, the French colonist keeps its own.
        rival.verifyOffenceBattle(RIVAL_ID, EQUAL_RIVAL, HUMAN_ID, EQUAL_HUMAN,
                fought, OFFENCE_ROUNDS, false, true);
    }

    @Test
    @Tag("local")
    void TC_03_rival_without_visible_target_stays_bounded() throws Exception {
        loadTarget = saveFixture("F136-quiet.microcol", false, RIVAL_START,
                FAR_HUMAN, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalLandPage rival = new RivalLandPage(getContext());
        rival.verifyQuietStart(RIVAL_ID, RIVAL_START, FAR_HUMAN, HUMAN_ID);
        for (int turn = 0; turn < QUIET_ROUNDS; turn++) {
            final int next = rival.readRound() + 1;
            final String combatBefore = rival.readCombatState();
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyQuietTurn("Round " + next, RIVAL_ID, HUMAN_ID,
                    RIVAL_START, combatBefore);
        }
    }
}
