package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EquipmentPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * A mounted unarmed colonist is an ordinary scout; muskets turn the same person
 * into a dragoon (R-0211, UC-022). The colony holds 49 muskets and 100 horses.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_183_F211_scout_to_dragoon_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String MISSING = "Not enough goods in the warehouse. Cancel and obtain the required goods.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F17-colony-equipment.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_scout_is_refused_muskets_at_49_and_stays_a_scout_after_continue()
            throws Exception {
        // Horses alone make the colonist a scout with the wider sight.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Mount horses");
        equipment.confirm();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);

        // One musket short of the 50 needed: refused, cancel changes nothing.
        equipment.open("Equip with muskets");
        equipment.verifyChoice("Equip with muskets", false, MISSING);
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
    void TC_02_scout_becomes_dragoon_at_50_muskets_and_keeps_identity_after_continue()
            throws Exception {
        // The same colonist is first a scout.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Mount horses");
        equipment.confirm();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);

        // The ship brings the missing muskets through the colony controls.
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.MUSKET));
        equipment.verifyState(100, 50, 80, false, true, 0);

        // Fifty muskets are debited once; the horses stay on the colonist.
        equipment.open("Equip with muskets");
        equipment.verifyChoice("Equip with muskets", true, "");
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
