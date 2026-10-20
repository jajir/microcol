package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Player disbanding (UC-016, MOVE-009): the Disband control on a selected own
 * unit, cancel and confirm, the refusal of a loaded carrier, the equipment kept
 * in New Amsterdam or lost outside it, the acknowledged excess above a full
 * warehouse, the cleared selection and Save/Continue without resurrection. The
 * fixture places colonist 104 and sets the warehouse before the game loads.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_104_F95_disband_test extends AbstractMicroColTest {
    private static final File SOURCE = new File(
            "src/test/scenarios/F52-fortification.microcol");

    private static final int COLONIST = DisbandFixture.COLONIST_ID;

    private static final int GALLEON = 3;

    private static final int PASSENGER = 4;

    private static final String QUESTION = "Disband Free Colonist? It is removed for good from the map and your saved game. Cancel changes nothing.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_changes_nothing_and_confirm_removes_the_unit_once_for_good()
            throws Exception {
        DisbandFixture.prepare(getContext(), SOURCE,
                "F95-disband-confirm.microcol", DisbandFixture.WEST, 30, true,
                0);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        DisbandPage disband = new DisbandPage(getContext());
        game.panToTile(DisbandFixture.WEST);
        // The own colonist outside any colony shows the enabled control.
        game.selectTile(DisbandFixture.WEST);
        disband.verifySelectedUnit(Optional.of(COLONIST));
        disband.verifyControlVisible(true);
        disband.verifyControlEnabled(true);
        // The confirmation names the unit and the exact equipment outcome.
        disband.useControl();
        disband.verifyConfirmation("Disband unit", QUESTION,
                "No colony of yours is on this square, so all equipment is lost.");
        disband.verifyEquipmentLines("Tools: 0 kept, 30 lost.",
                "Horses: 0 kept, 50 lost.", "Muskets: 0 kept, 0 lost.");
        disband.verifyOverflowAcknowledgement(false);
        disband.verifyConfirmEnabled(true);
        // Cancel changes nothing: the colonist, its equipment and selection
        // stay.
        disband.cancel();
        disband.verifyDialogClosed();
        disband.verifyUnitPresent(COLONIST, true);
        disband.verifyEquipment(COLONIST, 30, true, false);
        disband.verifySelectedUnit(Optional.of(COLONIST));
        // Confirm removes the unit once; the selection and control go with it.
        disband.useControl();
        disband.confirm();
        disband.verifyDialogClosed();
        disband.verifyUnitPresent(COLONIST, false);
        disband.verifySelectedUnit(Optional.empty());
        disband.verifyControlVisible(false);
        // Save and Continue do not bring the unit back or select it again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        disband.verifyUnitPresent(COLONIST, false);
        game.panToTile(DisbandFixture.WEST);
        game.selectTile(Location.of(22, 15));
        disband.verifySelectedUnit(Optional.empty());
        disband.verifyControlVisible(false);
    }

    @Test
    @Tag("local")
    void TC_02_loaded_galleon_is_refused_with_a_reason_and_keeps_its_passenger()
            throws Exception {
        DisbandFixture.prepare(getContext(), SOURCE,
                "F95-disband-loaded.microcol", DisbandFixture.WEST, 0, false,
                0);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        DisbandPage disband = new DisbandPage(getContext());
        game.panToTile(DisbandFixture.GALLEON);
        // The galleon is an own unit, so the control is offered.
        game.selectTile(DisbandFixture.GALLEON);
        disband.verifySelectedUnit(Optional.of(GALLEON));
        disband.verifyControlVisible(true);
        disband.verifyControlEnabled(true);
        // The refusal gives the reason and opens no confirmation.
        disband.useControl();
        disband.verifyDialogClosed();
        game.verifyThatStatusBarContains(
                "Unload all passengers and goods before disbanding this ship or wagon.");
        // The galleon, its passenger and the passenger's tools stay unchanged.
        disband.verifyUnitPresent(GALLEON, true);
        disband.verifyPassengerAboard(PASSENGER, GALLEON);
        disband.verifyEquipment(PASSENGER, 80, false, false);
        disband.verifySelectedUnit(Optional.of(GALLEON));
    }

    @Test
    @Tag("local")
    void TC_03_colonist_on_the_colony_square_returns_its_equipment_to_the_warehouse()
            throws Exception {
        DisbandFixture.prepare(getContext(), SOURCE,
                "F95-disband-colony.microcol", DisbandFixture.COLONY, 35, true,
                40);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        DisbandPage disband = new DisbandPage(getContext());
        game.panToTile(DisbandFixture.COLONY);
        // A click on New Amsterdam selects the colonist standing there and
        // opens the colony; closing it returns to the map with the colonist
        // still selected and the Disband control shown.
        ColonyScreen colony = game.openColonyAt(DisbandFixture.COLONY,
                "New Amsterdam");
        game = colony.close();
        disband.verifySelectedUnit(Optional.of(COLONIST));
        disband.verifyControlVisible(true);
        disband.verifyControlEnabled(true);
        // Standing on New Amsterdam's square, the colonist's equipment is kept.
        disband.useControl();
        disband.verifyConfirmation("Disband unit", QUESTION,
                "Kept equipment goes to New Amsterdam. The unit's orders end without any work being done.");
        disband.verifyEquipmentLines("Tools: 35 kept, 0 lost.",
                "Horses: 50 kept, 0 lost.", "Muskets: 0 kept, 0 lost.");
        disband.verifyOverflowAcknowledgement(false);
        // Confirming moves the kept horses and tools into the warehouse.
        disband.confirm();
        disband.verifyDialogClosed();
        disband.verifyUnitPresent(COLONIST, false);
        disband.verifyColonyStock(DisbandFixture.COLONY_ID, GoodsType.HORSE,
                90);
        disband.verifyColonyStock(DisbandFixture.COLONY_ID, GoodsType.TOOLS,
                35);
        disband.verifySelectedUnit(Optional.empty());
    }

    @Test
    @Tag("local")
    void TC_04_excess_above_a_full_warehouse_needs_acknowledgement_and_cancel_keeps_it()
            throws Exception {
        DisbandFixture.prepare(getContext(), SOURCE,
                "F95-disband-overflow.microcol", DisbandFixture.COLONY, 0, true,
                80);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        DisbandPage disband = new DisbandPage(getContext());
        game.panToTile(DisbandFixture.COLONY);
        // Select the colonist on New Amsterdam's square, then leave the colony
        // that the same click opened.
        ColonyScreen colony = game.openColonyAt(DisbandFixture.COLONY,
                "New Amsterdam");
        game = colony.close();
        disband.verifySelectedUnit(Optional.of(COLONIST));
        disband.verifyControlVisible(true);
        // Twenty horses fit the warehouse; thirty would be lost.
        disband.useControl();
        disband.verifyEquipmentLines("Tools: 0 kept, 0 lost.",
                "Horses: 20 kept, 30 lost.", "Muskets: 0 kept, 0 lost.");
        disband.verifyOverflowAcknowledgement(true);
        disband.verifyConfirmEnabled(false);
        // Cancel keeps the unit and the warehouse exactly as they were.
        disband.cancel();
        disband.verifyDialogClosed();
        disband.verifyUnitPresent(COLONIST, true);
        disband.verifyColonyStock(DisbandFixture.COLONY_ID, GoodsType.HORSE,
                80);
        disband.verifySelectedUnit(Optional.of(COLONIST));
        // Confirming needs the explicit acknowledgement of the lost excess.
        disband.useControl();
        disband.acknowledgeExcess();
        disband.verifyConfirmEnabled(true);
        disband.confirm();
        disband.verifyUnitPresent(COLONIST, false);
        disband.verifyColonyStock(DisbandFixture.COLONY_ID, GoodsType.HORSE,
                100);
        disband.verifySelectedUnit(Optional.empty());
    }
}
