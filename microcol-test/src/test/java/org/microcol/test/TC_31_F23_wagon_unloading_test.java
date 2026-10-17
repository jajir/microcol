package org.microcol.test;

import java.io.File;
import java.util.List;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WagonUnloadPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Verify the actual wagon journey and conservation at the warehouse limit. */
@ExtendWith(ApplicationExtension.class)
public class TC_31_F23_wagon_unloading_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F23-wagon-unloading.microcol");
    private static final Location ORIGIN = Location.of(23, 16);
    private static final Location COLONY = Location.of(24, 16);
    private static final int ORE_INDEX = 6;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_arrive_cancel_unload_and_continue_without_losing_cargo()
            throws Exception {
        // Move the loaded wagon into the owned colony through the map.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyWagon(103, "Dutch", ORIGIN, 30);
        game.moveMouseAtLocation(ORIGIN);
        game.verifyThatStatusBarContains("Wagon");
        game.dragMouseAtLocation(COLONY);
        wagon.verifyWagon(103, "Dutch", COLONY, 30);

        // Select the wagon beside the existing ship and inspect its holds.
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        wagon.verifyCarriers(List.of(3, 103), 103);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);
        wagon.captureDialog("id_blacksmith", "wagon-colony.png");
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(10);
        dialog.verifyExplanation(
                "Available warehouse space for this cargo: 10. Choose an amount or cancel. The rest stays aboard.");

        // Cancel leaves the complete load aboard and the warehouse unchanged.
        dialog.cancel();
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // Confirm the new proposal; ten fit and twenty stay in the wagon.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        dialog = DialogChooseGoodsAmount.of(getContext());
        dialog.verifyOfferedAmount(10);
        dialog.clickOnOk();
        wagon.verifyWagon(103, "Dutch", COLONY, 20);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 100);
        wagon.verifyCarriers(List.of(3, 103), 103);

        // A full warehouse explains why the remaining cargo cannot be unloaded.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        colony.verifyWarehouseWarning(
                "The warehouse is full for this cargo. Nothing was unloaded. Free storage and try again.");
        colony.closeWarehouseWarning();
        wagon.verifyWagon(103, "Dutch", COLONY, 20);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 100);

        // Continue restores the same wagon location and both exact inventories.
        colony.close().exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        wagon.verifyWagon(103, "Dutch", COLONY, 20);
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        wagon.verifyCarriers(List.of(3, 103), 103);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 100);
    }

    @Test
    @Tag("local")
    void TC_02_upgraded_warehouse_accepts_the_whole_load_after_escape()
            throws Exception {
        // Prepare the same wagon at a colony with an upgraded warehouse.
        prepareAtColony(true);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyCarriers(List.of(3, 103), 103);

        // The quantity control now offers all thirty; Escape preserves both
        // sides.
        final DialogChooseGoodsAmount dialog = colony
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(0,
                        ORE_INDEX);
        dialog.verifyOfferedAmount(30);
        dialog.pressEscape();
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // Ordinary unloading uses the current capacity, exceeding the basic
        // limit safely.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        wagon.verifyWagon(103, "Dutch", COLONY, 0);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 120);
    }

    @Test
    @Tag("local")
    void TC_03_changed_capacity_requires_a_new_five_unit_confirmation()
            throws Exception {
        // Open the original ten-unit proposal at the basic warehouse.
        prepareAtColony(false);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(10);

        // Simulate competing production while the player's proposal is open.
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getColonyByName("New Amsterdam").get()
                        .getWarehouse().addGoods(Goods.of(GoodsType.ORE, 5)))
                .get(5, TimeUnit.SECONDS);
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(
                "The cargo or colony changed. Nothing was unloaded. Check the cargo and available storage, then try again.");
        colony.closeWarehouseWarning();
        dialog.verifyOfferedAmount(5);
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 95);

        // Cancel the refreshed choice, then explicitly confirm a newly opened
        // offer.
        dialog.cancel();
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        dialog.verifyOfferedAmount(5);
        dialog.clickOnOk();
        wagon.verifyWagon(103, "Dutch", COLONY, 25);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 100);
    }

    @Test
    @Tag("local")
    void TC_04_changed_cargo_is_reconfirmed_without_creating_goods()
            throws Exception {
        // Open a ten-unit proposal while thirty ore are in the wagon.
        prepareAtColony(false);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(10);

        // Simulate another action consuming twenty-three ore from that hold.
        WaitForAsyncUtils
                .asyncFx(() -> ((UnitWithCargo) getModel().getUnitById(103))
                        .getCargo().getSlotByIndex(0)
                        .removeCargo(Goods.of(GoodsType.ORE, 23)))
                .get(5, TimeUnit.SECONDS);
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(
                "The cargo or colony changed. Nothing was unloaded. Check the cargo and available storage, then try again.");
        colony.closeWarehouseWarning();
        dialog.verifyOfferedAmount(7);
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyWagon(103, "Dutch", COLONY, 7);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // Only a second explicit confirmation transfers the seven remaining
        // ore.
        dialog.clickOnOk();
        wagon.verifyWagon(103, "Dutch", COLONY, 0);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 97);
    }

    @Test
    @Tag("local")
    void TC_05_wagon_leaving_during_confirmation_cannot_unload_remotely()
            throws Exception {
        // Open a proposal for the wagon currently at the colony.
        prepareAtColony(false);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(10);

        // Simulate the carrier leaving before the user confirms the old offer.
        WaitForAsyncUtils.asyncFx(
                () -> getModel().getUnitById(103).placeToLocation(ORIGIN))
                .get(5, TimeUnit.SECONDS);
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(
                "The cargo or colony changed. Nothing was unloaded. Check the cargo and available storage, then try again.");
        colony.closeWarehouseWarning();
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyWagon(103, "Dutch", ORIGIN, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // The remaining local ship is still selectable; the remote wagon is
        // absent.
        colony.selectUnitFromPort(0);
        wagon.verifyCarriers(List.of(3), 3);
    }

    @Test
    @Tag("local")
    void TC_06_captured_wagon_cannot_supply_the_previous_owner()
            throws Exception {
        // Open a proposal while both wagon and colony belong to the player.
        prepareAtColony(false);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_INDEX);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(10);

        // Simulate loss of the carrier while its old proposal is still visible.
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getUnitById(103)
                        .takeOver(getModel().getPlayerByName("Dutch's King")))
                .get(5, TimeUnit.SECONDS);
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(
                "The cargo or colony changed. Nothing was unloaded. Check the cargo and available storage, then try again.");
        colony.closeWarehouseWarning();
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyWagon(103, "Dutch's King", COLONY, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // Foreign cargo is no longer offered in the colony's carrier controls.
        colony.selectUnitFromPort(0);
        wagon.verifyCarriers(List.of(3), 3);
    }

    @Test
    @Tag("local")
    void TC_07_wagon_rejects_passengers_and_ship_only_icon_dragging()
            throws Exception {
        // Select the wagon while an ordinary colonist waits outside the colony.
        prepareAtColony(false);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.selectUnitFromPort(1);
        final WagonUnloadPage wagon = new WagonUnloadPage(getContext());
        wagon.verifyColonistOutside(104, "Dutch", COLONY);

        // An attempted passenger drop onto the empty hold cannot board the
        // wagon.
        new org.microcol.page.PassengerTransferPage(getContext())
                .dragPassengerToSlot(104, 1, false);
        wagon.verifyColonistOutside(104, "Dutch", COLONY);
        wagon.verifyWagon(103, "Dutch", COLONY, 30);

        // Dragging the wagon icon does not start a ship operation or consume
        // its cargo.
        wagon.dragWagonIconToEmptyCargoSlot(1, 1);
        wagon.verifyCarriers(List.of(3, 103), 103);
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // A wagon is a carrier, not a passenger: the empty hold of the port
        // ship does not accept the wagon icon either.
        colony.selectUnitFromPort(0);
        wagon.verifyCarriers(List.of(3, 103), 3);
        wagon.dragWagonIconToEmptyCargoSlot(1, 2);
        wagon.verifyCarriers(List.of(3, 103), 3);
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        wagon.verifyWagonNotAboard(3, 103);
        colony.verifyNumberOfGoodsInWrehouse(ORE_INDEX, 90);

        // Continue keeps the wagon and the colonist unchanged outside.
        colony.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        wagon.verifyWagon(103, "Dutch", COLONY, 30);
        wagon.verifyWagonNotAboard(3, 103);
        wagon.verifyColonistOutside(104, "Dutch", COLONY);
    }

    @Test
    @Tag("local")
    void TC_08_czech_map_names_identify_the_wagon() throws Exception {
        // Select Czech through settings and open the same real wagon fixture.
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();

        // The actual map status identifies the wagon in the selected language.
        game.moveMouseAtLocation(ORIGIN);
        game.verifyThatStatusBarContains("povozem");
        new WagonUnloadPage(getContext()).verifyWagon(103, "Dutch", ORIGIN, 30);
    }

    private void prepareAtColony(final boolean upgraded) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(FIXTURE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.get("id").getAsInt() == 103) {
                unit.getAsJsonObject("placeMap").getAsJsonObject("location")
                        .addProperty("x", 24);
            }
        });
        if (upgraded) {
            model.getAsJsonArray("colonies").get(0).getAsJsonObject()
                    .getAsJsonArray("constructions").forEach(value -> {
                        final JsonObject building = value.getAsJsonObject();
                        if (building.get("type").getAsString()
                                .equals("BASIC_WAREHOUSE")) {
                            building.addProperty("type", "WAREHOUSE");
                        }
                    });
        }
        final File prepared = new File(System.getProperty("user.home"),
                "wagon-prepared.microcol");
        Files.writeString(prepared.toPath(), saved.toString());
        when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
