package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.ai.PassiveRivalPlayer;
import org.microcol.ai.RivalPlayerFactory;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.AttackPage;
import org.microcol.page.ClearForestPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that a real forest clearing changes the defense of the same grassland
 * square: the attack review shows the forest bonus before clearing and only the
 * bare defense after the clearing finished and the game was saved and
 * continued. Declining the clearing and cancelling a preview change nothing.
 *
 * <p>
 * The French rival is passive, as in TC101 and TC181, so its units stay where
 * the scenario puts them while five owner turns pass. The only colony beside
 * the forest is French, so the Dutch pioneer has no recipient colony and the
 * lumber is lost with a notice. The defender is placed with the existing
 * {@code AttackPage.simulateMovedDefender} helper, only to leave the forest
 * square for the pioneer and to stand on the cleared square afterwards. The
 * clearing itself is the player's own confirmed action.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_182_F210_forest_clearing_defense_test
        extends AbstractMicroColTest {

    /** Grassland square under the forest and the defender of the scenario. */
    private final static Location FOREST = Location.of(17, 11);

    /** Dutch colonist with muskets that attacks the defender. */
    private final static Location SOLDIER = Location.of(18, 10);

    /** Unarmed Dutch colonist that clears the forest, then steps back out. */
    private final static Location PIONEER_START = Location.of(18, 11);

    /** Empty grassland beside the forest where the defender waits. */
    private final static Location SIDE = Location.of(16, 11);

    private final static int PIONEER_ID = 1;

    /**
     * The only colony beside the forest is French, so no colony of the player
     * is within two squares and the question names no colony.
     */
    private final static String QUESTION = "Clear the forest here? Work takes 4 owner updates and spends 20 tools when it finishes. Expected result: 20 lumber, but no colony of yours is within two squares now. The colony is chosen when the work finishes, and without one the lumber is lost. Spent movement is not restored if you cancel later.";

    private final static String LOSS_NOTICE = "Forest cleared at (17, 11): lumber lost: 20. No colony of yours is within two squares.";

    private final static String ATTACKER_STRENGTH = "Base: 2\nAttack: +50%\nFinal strength: 3";

    private final static String FOREST_DEFENDER = "Base: 2\nTerrain: +50%\nFinal strength: 3";

    private final static String FOREST_CHANCE = "Attacker win chance: 50%";

    private final static String BARE_DEFENDER = "Base: 2\nFinal strength: 2";

    private final static String BARE_CHANCE = "Attacker win chance: 60%";

    private final static File testFileName = new File(
            "src/test/scenarios/F27-attack-eligibility.microcol");

    /**
     * Starts the application with the usual test services and a passive French
     * computer rival. The Load button gives the file the scenario prepares.
     */
    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenReturn(testFileName);
            binder.bind(FileSelectingService.class).toInstance(selecting);
            binder.bind(RivalPlayerFactory.class)
                    .toInstance(PassiveRivalPlayer::new);
        });
    }

    @Test
    @Tag("local")
    void TC_01_forest_defense_becomes_bare_after_real_clearing_and_continue()
            throws Exception {
        // Plant a forest under the defender and give the unarmed colonists
        // tools. The French colony's lumber stock is zeroed only to fix the
        // start state.
        ClearForestFixture.prepare(getContext(), testFileName,
                "clear-f210-defense.microcol", 100, null, FOREST, "Quebec", 0);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        final FogPage map = new FogPage(getContext());
        final ClearForestPage clear = new ClearForestPage(getContext());

        // Previewing the attack and cancelling it changes nothing.
        attack.rememberWorld();
        attack.preview(SOLDIER, FOREST);
        attack.verifyAttackPreview(FOREST);
        attack.cancelPreview();
        attack.verifyGameplayUnchanged();

        // Before clearing, the forested grassland defender has terrain bonus.
        attack.chooseAttack(SOLDIER, FOREST);
        attack.verifyStrengths(ATTACKER_STRENGTH, FOREST_DEFENDER,
                FOREST_CHANCE);
        attack.escape();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // The defender steps aside so the pioneer can enter the forest square.
        attack.simulateMovedDefender(SIDE);
        map.move(PIONEER_START, FOREST, PIONEER_ID);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(FOREST);

        // Declining the clearing question spends nothing and starts nothing.
        clear.verifyControlVisible(true);
        clear.useControl();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.decline();
        clear.verifyDialogClosed();
        clear.verifyUnitWork(FOREST, 0, UnitActionType.noAction, 1, 100);
        clear.verifyForestAt(FOREST, true);
        clear.verifyCancelControlVisible(false);

        // Confirming the real clearing spends movement and starts four updates.
        clear.useControl();
        clear.verifyConfirmation("Clear forest", QUESTION);
        clear.confirm();
        clear.verifyUnitWork(FOREST, 0, UnitActionType.clearForest, 0, 100);
        clear.verifyRemainingUpdates(FOREST, 0, 4);
        clear.verifyForestAt(FOREST, true);

        // Two updates pass, then Continue keeps the countdown.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyRemainingUpdates(FOREST, 0, 2);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        clear.verifyRemainingUpdates(FOREST, 0, 2);
        clear.verifyForestAt(FOREST, true);

        // The fourth update removes the forest and reports the lost lumber.
        gamePage.nextTurnAndCloseDialogs();
        clear.verifyRemainingUpdates(FOREST, 0, 1);
        final Optional<ScreenTurnReport> report = gamePage.nextTurn();
        clear.verifyTurnReportShown(report);
        report.get().verifyThatAtLeastOneEventMessageContains(LOSS_NOTICE);
        report.get().close();
        clear.verifyForestAt(FOREST, false);
        clear.verifyClearedAt(FOREST, true);
        clear.verifyTerrainOutput(FOREST, 0, 0);
        clear.verifyUnitWork(FOREST, 0, UnitActionType.noAction, 0, 80);

        // Save and Continue keeps the cleared square.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        clear.verifyForestAt(FOREST, false);
        clear.verifyClearedAt(FOREST, true);
        clear.verifyTerrainOutput(FOREST, 0, 0);

        // A new turn restores the pioneer's movement; it steps back out.
        gamePage.nextTurnAndCloseDialogs();
        map.move(FOREST, PIONEER_START, PIONEER_ID);

        // Placing the defender on the cleared square only positions the
        // target; the clearing above was the player's own action.
        attack.simulateMovedDefender(FOREST);

        // After clearing and reload, the same grassland defender has no terrain
        // bonus, so the attacker's chance rises from 50% to 60%.
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, FOREST);
        attack.verifyStrengths(ATTACKER_STRENGTH, BARE_DEFENDER, BARE_CHANCE);
        attack.escape();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }

}
