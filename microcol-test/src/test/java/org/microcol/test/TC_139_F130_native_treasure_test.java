package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeConquestPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Treasure train of a native conquest (R-0175). A Dutch soldier conquers the
 * Aztec capital through the real chooser and confirmation. With Cortes in the
 * Dutch Congress the conquest always leaves one train worth double the capital
 * base, 2000 gold, and the result names it. Without Cortes a failed treasure
 * roll leaves no train, and a successful one leaves a train of 1000 gold. The
 * train is shown on its square with its value and survives Save and Continue
 * without a second train or a second roll. Gold is not credited at creation.
 * The probabilities, the exact values and the saved draws are checked in the
 * model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_139_F130_native_treasure_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, learned by the Dutch, defended by two braves. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Dutch soldier north of the capital. */
    private static final Location SOLDIER_AT = Location.of(23, 14);

    /** Two braves defend the square. */
    private static final int DEFENDERS = 2;

    private static final String CONQUERED_EN = "You overcame 2 defender(s) and conquered the settlement.";

    private static final String TRAIN_2000_EN = "A treasure train worth 2000 gold now stands on the conquered square.";

    private static final String TRAIN_1000_EN = "A treasure train worth 1000 gold now stands on the conquered square.";

    private static final String NO_TRAIN_EN = "No treasure train was left in the settlement.";

    private static final String VALUE_2000_EN = "Treasure value: 2000 gold";

    private static final String VALUE_1000_EN = "Treasure value: 1000 gold";

    private static final String CONQUERED_CS = "Přemohli jste 2 obránce(ů) a sídlo jste dobyli.";

    private static final String TRAIN_2000_CS = "Na dobytém místě nyní stojí pokladní povoz v hodnotě 2000 zlatých.";

    private static final String VALUE_2000_CS = "Hodnota pokladu: 2000 zlatých";

    /** Anger of a conquered capital: (20 + 20) twice, from zero. */
    private static final double CONQUEST_ANGER = 80;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_cortes_leaves_a_double_train_that_survives_continue()
            throws Exception {
        // Load the native position with Cortes in the Dutch Congress.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS, true);
        fog.centerFrontier();

        // Confirm the conquest; the result names the train and its value.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.startAttackChoice();
        conquest.confirmAttack();
        conquest.verifyResultMessage(CONQUERED_EN);
        conquest.verifyResultMessage(TRAIN_2000_EN);
        conquest.verifyResultFits(TRAIN_2000_EN, 900, 750);
        conquest.captureResult("native-treasure-outcome-en.png");
        conquest.closeAttackResult();
        visit.verifyChooserClosed();

        // The train stands on the conquered square and names its value.
        conquest.verifySettlementRemoved(CAPITAL);
        conquest.verifyTreasureTrain(CAPITAL, 2000);
        conquest.verifyTribalAnger(CONQUEST_ANGER);
        conquest.verifyConquestRecords(1);
        GamePage.of(getContext()).selectTile(CAPITAL);
        conquest.verifyTreasurePanel(CAPITAL, VALUE_2000_EN);
        fog.saveMapEvidence("native-treasure-map-en.png");

        // Save and Continue keep the one train and do not roll or create again.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        conquest.verifyTreasureTrain(CAPITAL, 2000);
        conquest.verifyTribalAnger(CONQUEST_ANGER);
        conquest.verifyConquestRecords(1);
        GamePage.of(getContext()).selectTile(CAPITAL);
        conquest.verifyTreasurePanel(CAPITAL, VALUE_2000_EN);
    }

    @Test
    @Tag("local")
    void TC_02_failed_treasure_roll_without_cortes_leaves_no_train()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS, false);
        fog.centerFrontier();

        // The conquest is real, and the result says that no train was left.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.startAttackChoice();
        conquest.confirmAttack();
        conquest.verifyResultMessage(CONQUERED_EN);
        conquest.verifyResultMessage(NO_TRAIN_EN);
        conquest.verifyResultFits(NO_TRAIN_EN, 900, 750);
        conquest.captureResult("native-treasure-none-en.png");
        conquest.closeAttackResult();
        visit.verifyChooserClosed();

        conquest.verifySettlementRemoved(CAPITAL);
        conquest.verifyNoTreasureTrain();
        conquest.verifyConquestRecords(1);

        // Save and Continue repeat neither the conquest nor its treasure roll.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        conquest.verifyNoTreasureTrain();
        conquest.verifyConquestRecords(1);
    }

    @Test
    @Tag("local")
    void TC_03_successful_treasure_roll_without_cortes_leaves_a_train_of_1000()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE,
                NativeConquestPage.treasureState(), DEFENDERS, false);
        fog.centerFrontier();

        // Without Cortes the capital's train is the ordinary 1000 gold.
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.startAttackChoice();
        conquest.confirmAttack();
        conquest.verifyResultMessage(CONQUERED_EN);
        conquest.verifyResultMessage(TRAIN_1000_EN);
        conquest.captureResult("native-treasure-reward-en.png");
        conquest.closeAttackResult();
        visit.verifyChooserClosed();

        conquest.verifyTreasureTrain(CAPITAL, 1000);
        GamePage.of(getContext()).selectTile(CAPITAL);
        conquest.verifyTreasurePanel(CAPITAL, VALUE_1000_EN);

        // Save and Continue keep the same single train.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        conquest.verifyTreasureTrain(CAPITAL, 1000);
        conquest.verifyConquestRecords(1);
    }

    @Test
    @Tag("local")
    void TC_04_czech_cortes_train_fits_the_result_and_the_panel()
            throws Exception {
        // Switch to Czech, load the native position with Cortes.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeConquestPage conquest = new NativeConquestPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        conquest.loadConquestFixture(FIXTURE, NativeConquestPage.victoryState(),
                DEFENDERS, true);
        fog.centerFrontier();

        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        conquest.startAttackChoice();
        conquest.confirmAttack();
        conquest.verifyResultMessage(CONQUERED_CS);
        conquest.verifyResultMessage(TRAIN_2000_CS);
        conquest.verifyResultFits(TRAIN_2000_CS, 900, 750);
        conquest.captureResult("native-treasure-outcome-cs.png");
        conquest.closeAttackResult();
        visit.verifyChooserClosed();

        // The long Czech hover text of the train's square wraps the status bar
        // onto more lines; the map keeps its place, so the square is selected.
        conquest.verifySettlementRemoved(CAPITAL);
        conquest.verifyTreasureTrain(CAPITAL, 2000);
        GamePage.of(getContext()).selectTile(CAPITAL);
        conquest.verifyTreasurePanel(CAPITAL, VALUE_2000_CS);
        conquest.captureTreasurePanel(CAPITAL, "native-treasure-panel-cs.png");
        fog.saveMapEvidence("native-treasure-map-cs.png");
    }

}
