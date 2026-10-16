package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Saved damage advances only through real owner turns, including Continue. */
@ExtendWith(ApplicationExtension.class)
public class TC_63_F54_ship_repair_test extends AbstractMicroColTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F54-ship-repair.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_europe_transfer_repair_continue_and_cargo() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyNumberOfShipsInPort(0);
        repairs.open();
        repairs.verifyProgress("TRANSFER", 3,
                "Galleon #3 — Europe\nTransfer; your turns until operational: 3");
        repairs.captureDialog("shipRepairRows", "ship-repairs-english.png");
        repairs.escape();
        // Opening and cancelling a report never advances the saved transfer.
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        repairs.open();
        repairs.verifyProgress("TRANSFER", 3,
                "Galleon #3 — Europe\nTransfer; your turns until operational: 3");
        repairs.close();
        game = europe.close();
        ScreenTurnReport arrival = game.nextTurn().orElseThrow();
        arrival.verifyThatAtLeastOneEventMessageContains(
                "arrived for repair at Europe");
        arrival.close();
        // Arrival leaves two whole repair updates; the cargo remains
        // inaccessible.
        europe = game.openEuroperPort();
        europe.verifyNumberOfShipsInPort(0);
        repairs.open();
        repairs.verifyProgress("REPAIR", 2,
                "Galleon #3 — Europe\nRepair; your turns until operational: 2");
        repairs.close();
        game = europe.close();
        game.nextTurnAndCloseDialogs();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        repairs.open();
        repairs.verifyProgress("REPAIR", 1,
                "Galleon #3 — Europe\nRepair; your turns until operational: 1");
        repairs.close();
        game = europe.close();
        ScreenTurnReport completed = game.nextTurn().orElseThrow();
        completed.verifyThatAtLeastOneEventMessageContains(
                "at Europe are complete");
        completed.close();
        europe = game.openEuroperPort();
        europe.verifyNumberOfShipsInPort(1);
        repairs.open();
        repairs.verifyComplete(true, "No ships are being repaired.");
        repairs.close();
        // Repaired cargo is accessible again through the ordinary port
        // controls.
        europe.selectUnitFromPort(0);
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.disembarkInPort(4, 1, true);
        passengers.verifyOnPier(4, 1, 80);
    }

    @Test
    @Tag("local")
    void TC_02_czech_drydock_full_three_updates() throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        repairs.loadDrydockFixture();
        GamePage game = GamePage.of(getContext());
        EuropePortScreen europe = game.openEuroperPort("Evropa");
        repairs.open();
        repairs.verifyProgress("TRANSFER", 4,
                "galeona #3 — New Amsterdam\nPřeprava; do zprovoznění zbývá tahů vlastníka: 4");
        repairs.captureDialog("shipRepairRows", "ship-repairs-czech.png");
        repairs.close();
        game = europe.close();
        // One transfer update precedes all three drydock updates.
        game.nextTurnAndCloseDialogs();
        for (int remaining = 3; remaining > 0; remaining--) {
            europe = game.openEuroperPort("Evropa");
            repairs.open();
            repairs.verifyProgress("REPAIR", remaining,
                    "galeona #3 — New Amsterdam\nOprava; do zprovoznění zbývá tahů vlastníka: "
                            + remaining);
            repairs.close();
            game = europe.close();
            game.nextTurnAndCloseDialogs();
        }
        game.openEuroperPort("Evropa");
        repairs.open();
        repairs.verifyComplete(false, "Žádné lodě se neopravují.");
        repairs.close();
    }

    @Test
    @Tag("local")
    void TC_03_lost_destination_reroutes_without_free_repair()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        repairs.loadLostDestinationFixture(true);
        GamePage game = GamePage.of(getContext());
        // A saved lost destination remains valid data until the next owner
        // update.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        ScreenTurnReport rerouted = game.nextTurn().orElseThrow();
        rerouted.verifyThatAtLeastOneEventMessageContains(
                "was rerouted for repair to Europe");
        rerouted.close();
        game.openEuroperPort();
        repairs.open();
        repairs.verifyProgress("TRANSFER", 3,
                "Galleon #3 — Europe\nTransfer; your turns until operational: 3");
        repairs.close();
    }

    @Test
    @Tag("local")
    void TC_04_no_destination_reports_ship_and_payload_loss() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        repairs.loadLostDestinationFixture(false);
        // With Europe closed and no eligible yard, the real turn reports
        // losses.
        ScreenTurnReport loss = GamePage.of(getContext()).nextTurn()
                .orElseThrow();
        loss.verifyThatAtLeastOneEventMessageContains(
                "Galleon #3 sank: no repair destination.");
        loss.verifyThatAtLeastOneEventMessageContains(
                "Lost passengers: 1. Lost goods: 7");
        loss.close();
        repairs.verifySunk();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        repairs.verifySunk();
    }
}
