package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import org.microcol.page.*;
import org.microcol.page.TreasureCargoPage.Holds;
import org.microcol.page.TreasureCargoPage.Layout;

/**
 * UC-024/065/086: explicit treasure proceeds, cancellation and once-only
 * delivery.
 */
@ExtendWith(ApplicationExtension.class)
class TC_149_F140_treasure_redemption_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F21-passenger-transfer.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(
                Path.of(System.getProperty("user.home"), "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_own_galleon_cancels_then_redeems_once_and_continue_keeps_payment()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        TreasureCargoPage cargo = new TreasureCargoPage(getContext());
        cargo.loadScenario(FIXTURE, Layout.PORT, Holds.TRAIN_ABOARD, true);
        TreasureRedemptionPage treasure = new TreasureRedemptionPage(
                getContext());
        int gold = treasure.gold();
        int net = treasure.expectedNet(false);
        // Europe shows exact proceeds; closing leaves the train and treasury
        // intact.
        GamePage.of(getContext()).openEuroperPort();
        treasure.open();
        treasure.verifyTerms(false, net);
        treasure.close();
        treasure.verifyGold(gold);
        treasure.verifyTrainPresent(true);
        // Explicit confirmation removes the train and releases all six holds.
        treasure.open();
        treasure.confirm();
        treasure.verifyGold(gold + net);
        treasure.verifyTrainPresent(false);
        treasure.verifySixHoldsFree();
        treasure.close();
        EuropePortScreen.of(getContext()).close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        treasure.verifyGold(gold + net);
        treasure.verifyTrainPresent(false);
    }

    @Test
    @Tag("local")
    void TC_02_royal_pickup_waits_through_continue_until_the_next_owner_turn()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        TreasureCargoPage cargo = new TreasureCargoPage(getContext());
        cargo.loadScenario(FIXTURE, Layout.PORT, Holds.ALL_FREE);
        TreasureRedemptionPage treasure = new TreasureRedemptionPage(
                getContext());
        int gold = treasure.gold();
        int net = treasure.expectedNet(true);
        ColonyScreen colony = cargo.openDock();
        // Cancel has no effects; confirmation reserves the displayed proceeds.
        treasure.open();
        treasure.verifyTerms(true, net);
        treasure.close();
        treasure.verifyGold(gold);
        treasure.open();
        treasure.confirm();
        treasure.verifyPending(net);
        treasure.close();
        treasure.verifyTrainPresent(true);
        treasure.verifyGold(gold);
        colony.close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        treasure.verifyPending(net);
        treasure.verifyGold(gold);
        // The actual next owner turn collects exactly once.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        treasure.verifyTrainPresent(false);
        treasure.verifyGold(gold + net);
    }

    @Test
    @Tag("local")
    void TC_03_czech_royal_offer_shows_the_exact_deductions_and_cancel_keeps_the_train()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        TreasureCargoPage cargo = new TreasureCargoPage(getContext());
        cargo.loadScenario(FIXTURE, Layout.PORT, Holds.ALL_FREE);
        TreasureRedemptionPage treasure = new TreasureRedemptionPage(
                getContext());
        int gold = treasure.gold();
        int net = treasure.expectedNet(true);
        cargo.openDock();
        // Read the localized proceeds before deciding; cancel preserves the
        // train.
        treasure.open();
        treasure.verifyCzechTerms(net);
        treasure.capture("treasure-redemption-cs.png");
        treasure.close();
        treasure.verifyGold(gold);
        treasure.verifyTrainPresent(true);
    }
}
