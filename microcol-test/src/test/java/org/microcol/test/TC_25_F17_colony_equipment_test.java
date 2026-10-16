package org.microcol.test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EquipmentPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import javafx.stage.Stage;

/** Confirm, cancel and revalidate equipment on the same colonist. */
@ExtendWith(ApplicationExtension.class)
public class TC_25_F17_colony_equipment_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String MISSING = "Not enough goods in the warehouse. Cancel and obtain the required goods.";
    private static final String FULL = "Returned equipment will not fit in the warehouse. Cancel and free storage first.";
    private static final String CHANGED = "The available goods changed. Review the updated choice and confirm again or cancel.";
    private static final String UNAVAILABLE = "This colonist is no longer available at your colony on your turn. Nothing was changed. Cancel and select the colonist again.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F17-colony-equipment.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_obtain_missing_goods_cancel_and_equip_same_colonist()
            throws Exception {
        // Missing goods remain visible with their cost and a reason.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.verifySightRadius(1);
        equipment.open("Equip with muskets");
        equipment.verifyChoice("Equip with muskets", false, MISSING);
        equipment.verifyCostContains("50");
        equipment.captureDialog("equipmentDialog", "equipment-missing.png");
        equipment.cancel();
        equipment.verifyState(49, 100, 80, false, false, 0);

        // Obtain the missing muskets through the existing cargo journey.
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.MUSKET));
        equipment.verifyState(100, 100, 80, false, false, 0);
        equipment.open("Equip with muskets");
        equipment.verifyChoice("Equip with muskets", true, "");
        equipment.escape();
        equipment.verifyState(100, 100, 80, false, false, 0);

        // Confirmation consumes exactly the goods; horses retain the muskets.
        equipment.open("Equip with muskets");
        equipment.confirmWithEnter();
        equipment.verifyState(50, 100, 80, true, false, 0);
        equipment.open("Mount horses");
        equipment.confirm();
        equipment.verifyState(50, 50, 80, true, true, 0);
        equipment.verifySightRadius(1);
        equipment.open("Equip with tools");
        equipment.verifyCostContains("80");
        equipment.captureDialog("equipmentDialog", "equipment-returns.png");
        equipment.confirm();
        equipment.verifyState(100, 100, 0, false, false, 80);
        equipment.verifySightRadius(1);
    }

    @Test
    @Tag("local")
    void TC_02_changed_storage_rejects_entire_equipment_switch()
            throws Exception {
        // Tools must be returned before mounting; no part may be applied on
        // overflow.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Equip with tools");
        equipment.confirm();
        equipment.open("Mount horses");
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getColonyAt(COLONY).orElseThrow()
                        .getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, 21)))
                .get(5, TimeUnit.SECONDS);
        equipment.confirm();
        equipment.verifyChoice("Mount horses", false, FULL);
        equipment.cancel();
        equipment.verifyState(49, 100, 21, false, false, 80);
    }

    @Test
    @Tag("local")
    void TC_03_changed_amount_requires_a_second_explicit_confirmation()
            throws Exception {
        // A competing action reduces the tools while the original choice is
        // open.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.open("Equip with tools");
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getColonyAt(COLONY).orElseThrow()
                        .getWarehouse()
                        .removeGoods(Goods.of(GoodsType.TOOLS, 60)))
                .get(5, TimeUnit.SECONDS);
        equipment.confirm();
        equipment.verifyChoice("Equip with tools", true, CHANGED);
        equipment.verifyCostContains("20");
        equipment.verifyState(49, 100, 20, false, false, 0);

        // Only the second confirmation equips the newly offered amount.
        equipment.confirm();
        equipment.verifyState(49, 100, 0, false, false, 20);
    }

    @Test
    @Tag("local")
    void TC_04_moved_subject_is_rejected_and_keyboard_cancel_preserves_stock()
            throws Exception {
        // Shift+F10 opens the same equipment choices as the mouse.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.openWithKeyboard(1);
        equipment.verifyChoice("Mount horses", true, "");
        equipment.escape();
        equipment.verifyState(49, 100, 80, false, false, 0);

        // The subject moves before confirmation; no stock or equipment changes.
        equipment.open("Mount horses");
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getUnitById(4)
                        .placeToLocation(Location.of(25, 16)))
                .get(5, TimeUnit.SECONDS);
        equipment.confirm();
        equipment.verifyChoice("Mount horses", false, UNAVAILABLE);
        equipment.cancel();
        equipment.verifyState(49, 100, 80, false, false, 0);
    }

    @Test
    @Tag("local")
    void TC_05_czech_equipment_and_keyboard_confirmation() throws Exception {
        // Use the player's language preference before opening the colony.
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame().openColonyAt(COLONY, "New Amsterdam");
        final EquipmentPage equipment = new EquipmentPage(getContext());
        equipment.verifySightRadius(1);
        equipment.open("Vybavit mušketami");
        equipment.verifyChoice("Vybavit mušketami", false,
                "Ve skladu není dostatek zboží. Zrušte volbu a obstarejte potřebné zboží.");
        equipment.escape();

        // Keyboard confirmation has the same cost and result as a mouse click.
        equipment.openWithKeyboard(1);
        equipment.verifyChoice("Osedlat koně", true, "");
        equipment.captureDialog("equipmentDialog", "equipment-czech.png");
        equipment.confirmWithEnter();
        equipment.verifyState(49, 50, 80, false, true, 0);
        equipment.verifySightRadius(2);
    }

}
