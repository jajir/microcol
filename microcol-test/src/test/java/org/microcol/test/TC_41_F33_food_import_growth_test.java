package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Food imported by ship and wagon waits for the next colony update. */
@ExtendWith(ApplicationExtension.class)
public class TC_41_F33_food_import_growth_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F33-food-import-growth.microcol");
    private static final Location DELFT = Location.of(22, 12);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_41_ship_and_wagon_food_import_waits_for_growth() throws Exception {
        // Open the colony with food below the birth threshold.
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = gamePage.openColonyAt(DELFT, "Delft");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 190));
        colony.verifyCarrierGoods(3, 0, Goods.of(GoodsType.CORN, 20));
        colony.verifyCarrierGoods(5000, 0, Goods.of(GoodsType.CORN, 20));
        colony.verifyFoodAccounting(10, 16);

        // Cancel the ship import after reading its accepted quantity and
        // timing.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(20);
        dialog.verifyExplanation(
                "Up to 20 Food can be accepted. Choose the amount to unload or cancel. Population growth is checked at the next colony update, after food consumption.");
        dialog.cancel();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 190));
        colony.verifyCarrierGoods(3, 0, Goods.of(GoodsType.CORN, 20));

        // Confirm the same ship cargo; crossing 200 does not create a unit yet.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        dialog = DialogChooseGoodsAmount.of(getContext());
        dialog.verifyOfferedAmount(20);
        dialog.clickOnOk();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 210));
        colony.verifyCarrierGoods(3, 0, Goods.of(GoodsType.CORN, 0));
        colony.verifyNumberOfUnitsAtPier(2);

        // The wagon uses the same preview, cancellation and confirmation.
        colony.selectUnitFromPort(1);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        dialog = DialogChooseGoodsAmount.of(getContext());
        dialog.verifyOfferedAmount(20);
        dialog.cancel();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 210));
        colony.verifyCarrierGoods(5000, 0, Goods.of(GoodsType.CORN, 20));
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        dialog = DialogChooseGoodsAmount.of(getContext());
        dialog.verifyOfferedAmount(20);
        dialog.clickOnOk();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 230));
        colony.verifyCarrierGoods(5000, 0, Goods.of(GoodsType.CORN, 0));
        colony.verifyNumberOfUnitsAtPier(2);

        // Only the next national update consumes food and creates a colonist.
        gamePage = colony.close();
        gamePage.nextTurnAndCloseDialogs();
        colony = gamePage.openColonyAt(DELFT, "Delft");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 24));
        colony.verifyNumberOfUnitsAtPier(3);
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_41_stale_food_offer_requires_fresh_confirmation() throws Exception {
        // A source change while the food amount dialog is open invalidates it.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(DELFT, "Delft");
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        final DialogChooseGoodsAmount firstOffer = DialogChooseGoodsAmount
                .of(getContext());
        firstOffer.verifyOfferedAmount(20);
        colony.reduceCarrierCargoForStaleOffer(3, Goods.of(GoodsType.CORN, 1));
        firstOffer.clickOnOk();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 190));
        colony.verifyCarrierGoods(3, 0, Goods.of(GoodsType.CORN, 19));
        colony.closeWarehouseWarning();

        // The refreshed offer can transfer the exact remaining cargo.
        final DialogChooseGoodsAmount refreshed = DialogChooseGoodsAmount
                .of(getContext());
        refreshed.verifyOfferedAmount(19);
        refreshed.clickOnOk();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 209));
        colony.verifyCarrierGoods(3, 0, Goods.of(GoodsType.CORN, 0));
        colony.close();
    }
}
