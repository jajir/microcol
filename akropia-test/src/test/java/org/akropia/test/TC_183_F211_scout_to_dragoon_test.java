package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.ColonyScreen;
import org.akropia.page.EquipmentPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * A mounted unarmed colonist is an ordinary scout; weapons turn the same person
 * into a dragoon (R-0211, UC-022). The colony holds 49 weapons and 100 horses.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_183_F211_scout_to_dragoon_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String MISSING = "Not enough goods in the warehouse. Cancel and obtain the required goods.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F17-colony-equipment.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_scout_is_refused_weapons_at_49_and_stays_a_scout_after_continue()
            throws Exception {
        // Horses alone make the colonist a scout with the wider sight.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Mount horses");
        equipment.confirm();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);

        // One weapon short of the 50 needed: refused, cancel changes nothing.
        equipment.open("Equip with weapons");
        equipment.verifyChoice("Equip with weapons", false, MISSING);
        equipment.verifyCostContains("50");
        equipment.cancel();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);

        // The refusal and the scout survive leaving the game and Continue.
        colony.close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);
    }

    @Test
    @Tag("local")
    void TC_02_scout_becomes_dragoon_at_50_weapons_and_keeps_identity_after_continue()
            throws Exception {
        // The same colonist is first a scout.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Mount horses");
        equipment.confirm();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);

        // The ship brings the missing weapons through the colony controls.
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.WEAPONS));
        equipment.verifyState(100, 50, 80, false, true, 0);

        // Fifty weapons are debited once; the horses stay on the colonist.
        equipment.open("Equip with weapons");
        equipment.verifyChoice("Equip with weapons", true, "");
        equipment.confirm();
        equipment.verifyState(50, 50, 80, true, true, 0);
        equipment.verifySightRadius(1);

        // The dragoon is the same person after leaving the game and Continue.
        colony.close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        equipment.verifyState(50, 50, 80, true, true, 0);
        equipment.verifySightRadius(1);
    }
}
