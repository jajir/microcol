package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeAlarmPage;
import org.microcol.page.NativeMissionPage;
import org.microcol.page.NativeSpecialistJourneyPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A Jesuit missionary bought in Europe carries its expertise all the way to a
 * native mission (R-0074). The player hires the specialist from the real offer,
 * boards the galleon, saves during the voyage, sails home, lands in the colony
 * and walks the Jesuit from the colony square into the Aztec capital next to
 * it. The mission confirmation reads the Jesuit as an expert, Back changes
 * nothing, Confirm stations the same person at the settlement, and the progress
 * counts once per owner update across Save and Continue. The starting game is
 * the expert-purchase scenario with the authored Aztec capital added before it
 * loads; the bought unit is never placed or replaced by the test. The exact
 * alarm effects of an expert mission are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_165_F198_purchased_jesuit_mission_test
        extends AbstractMicroColTest {

    /** Europe, galleon and colony of the expert-purchase scenario. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** Scenario that owns the authored Aztec capital beside the colony. */
    private static final File NATIVES = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    private static final Location COLONY = Location.of(24, 16);

    /** The Aztec capital, one square north-west of the colony. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** The first purchase of the scenario receives this unit ID. */
    private static final int JESUIT = 103;

    private static final int GALLEON = 102;

    /** Treasury of the scenario minus the 3000 gold of one Jesuit. */
    private static final int GOLD_AFTER_PURCHASE = 1400;

    private static final String EXPERT_EN = "Missionary: expert.";

    private static final String PROGRESS_ONE_EN = "Progress: 1 of 5 updates.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_bought_jesuit_sails_home_and_establishes_an_expert_mission()
            throws Exception {
        // Start from the purchase game that has the Aztec capital next to the
        // colony.
        final NativeSpecialistJourneyPage journey = new NativeSpecialistJourneyPage(
                getContext());
        journey.prepareLoadableFixture(FIXTURE, NATIVES);
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        final ExpertPurchasePage purchase = new ExpertPurchasePage(
                getContext());
        final EuropeEquipmentPage equipment = new EuropeEquipmentPage(
                getContext());

        // Hire the Jesuit from the real offer: one charge, the specialist waits
        // on the pier.
        europe.openBuyUnitScreen();
        purchase.buySpecialist("JESUIT_MISSIONARY");
        purchase.verifyPurchasedSpecialist(JESUIT, "JESUIT_MISSIONARY",
                GOLD_AFTER_PURCHASE);
        journey.verifyBoughtJesuit(JESUIT, GOLD_AFTER_PURCHASE);

        // Board the galleon and sail; the voyage is sampled by the game.
        europe.selectUnitFromPort(0);
        equipment.embark(JESUIT);
        equipment.verifyPassenger(JESUIT, GALLEON);
        equipment.depart(GALLEON);
        final int voyageDuration = equipment.verifyVoyageSample(GALLEON);
        final int voyageWeather = equipment.getVoyageWeather(GALLEON);
        GamePage game = europe.close();

        // Continue keeps the same Jesuit aboard and the sampled voyage.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        equipment.verifyVoyageMetadata(GALLEON, voyageWeather, voyageDuration,
                voyageDuration);
        equipment.verifyPassenger(JESUIT, GALLEON);
        journey.verifyBoughtJesuit(JESUIT, GOLD_AFTER_PURCHASE);
        for (int remaining = voyageDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                equipment.verifyVoyageMetadata(GALLEON, voyageWeather,
                        voyageDuration, remaining);
            }
        }
        equipment.verifyArrived(GALLEON);

        // Sail from the eastern arrival through four ordinary sea turns.
        equipment.verifyCarrierAt(GALLEON, Location.of(49, 15));
        equipment.showSeaRoute(Location.of(49, 15), Location.of(43, 16));
        game.moveMouseAtLocation(Location.of(49, 15));
        game.dragMouseAtLocation(Location.of(43, 16));
        equipment.verifyCarrierAt(GALLEON, Location.of(43, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(43, 16), Location.of(37, 16));
        game.moveMouseAtLocation(Location.of(43, 16));
        game.dragMouseAtLocation(Location.of(37, 16));
        equipment.verifyCarrierAt(GALLEON, Location.of(37, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(37, 16), Location.of(31, 16));
        game.moveMouseAtLocation(Location.of(37, 16));
        game.dragMouseAtLocation(Location.of(31, 16));
        equipment.verifyCarrierAt(GALLEON, Location.of(31, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(31, 16), Location.of(26, 16));
        game.moveMouseAtLocation(Location.of(31, 16));
        game.dragMouseAtLocation(Location.of(26, 16));
        equipment.verifyCarrierAt(GALLEON, Location.of(26, 16));
        game.nextTurnAndCloseDialogs();

        // Enter the owned colony; the Jesuit steps ashore on its square and the
        // colonists now see the Aztec capital next to it.
        equipment.showSeaRoute(Location.of(26, 16), COLONY);
        game.moveMouseAtLocation(Location.of(26, 16));
        game.dragMouseAtLocation(COLONY);
        game.waitForNumberOfUnitsAt(COLONY, 2);
        journey.verifyJesuitLanded(JESUIT, GALLEON, COLONY);
        journey.verifySettlementKnown(CAPITAL);

        // A new turn gives the Jesuit its movement; Continue keeps the person.
        game.nextTurnAndCloseDialogs();
        journey.verifyJesuitCanMove(JESUIT);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        journey.verifyBoughtJesuit(JESUIT, GOLD_AFTER_PURCHASE);
        journey.verifyJesuitLanded(JESUIT, GALLEON, COLONY);

        // Show the colony and the capital beside it.
        game.panToTile(CAPITAL);
        game.verifyThatTileIsVisible(COLONY);
        game.verifyThatTileIsVisible(CAPITAL);

        // Choose the Jesuit, not the galleon, among the units on the colony
        // square and walk it into the capital; the confirmation reads it as an
        // expert.
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        journey.chooseUnitOnColonySquare(COLONY, CAPITAL, JESUIT);
        journey.verifySelectedUnit(JESUIT);
        visit.enterSettlement(COLONY, CAPITAL);
        mission.verifyMissionButtonShown();
        mission.startMissionChoice();
        mission.verifyMissionConfirmation(EXPERT_EN);
        mission.verifyMissionConfirmation("Progress: 0 of 5 updates.");
        // Relations evolve during the voyage; show the current attitude.
        mission.verifyMissionAttitude(JESUIT, CAPITAL);

        // Back leaves the Jesuit on the map and the settlement without a
        // mission.
        mission.backFromMission();
        mission.verifyMissionNotEstablished(JESUIT, CAPITAL);
        visit.escape();
        visit.verifyChooserClosed();

        // Choose the Jesuit again; Confirm stations the same Jesuit at the
        // settlement.
        journey.chooseUnitOnColonySquare(COLONY, CAPITAL, JESUIT);
        journey.verifySelectedUnit(JESUIT);
        visit.enterSettlement(COLONY, CAPITAL);
        mission.startMissionChoice();
        mission.confirmMission();
        mission.verifyMissionEstablished(JESUIT, CAPITAL, 0);
        visit.verifyChooserClosed();

        // The next owner turn counts one update and the settlement keeps it.
        game.nextTurnAndCloseDialogs();
        mission.verifyMissionEstablished(JESUIT, CAPITAL, 1);
        NativeAlarmPage.of(getContext()).openWithNKey();
        mission.verifySettlementMission(PROGRESS_ONE_EN);
        NativeAlarmPage.of(getContext()).closeWithEscape();

        // Save and Continue keep the mission, its progress and the Jesuit.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        mission.verifyMissionEstablished(JESUIT, CAPITAL, 1);
        journey.verifyBoughtJesuit(JESUIT, GOLD_AFTER_PURCHASE);
    }

}
