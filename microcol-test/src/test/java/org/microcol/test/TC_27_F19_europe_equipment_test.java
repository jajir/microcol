package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceLocation;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Confirm European market quotes on the same colonist and take the gear home.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_27_F19_europe_equipment_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.microcol");
    private static final String CHANGED = "The offer changed. Review the updated price and confirm again or cancel.";
    private static final String INCOMPATIBLE = "Sell the incompatible equipment first. Horses and muskets can be combined; tools require an unequipped colonist.";
    private static final String FUNDS = "Not enough gold. Nothing was changed. Cancel and obtain the required gold.";
    private static final String UNAVAILABLE = "This colonist is no longer available on your European pier during your turn. Nothing was changed. Cancel and select the colonist again.";
    private static final String RESTRICTED = "Trade with your European port is unavailable. Nothing was changed.";

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_buy_and_sell_each_lot_without_replacing_the_colonist()
            throws Exception {
        // Cancel both ways before accepting the displayed taxed purchase.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        equipment.open(100, "Buy 50 muskets");
        equipment.verifyChoice("Buy 50 muskets", true, "");
        equipment.verifyQuote(
                "50 Muskets\nUnit price: 3 gold\nTax: 17% (25 gold)\nTotal: 175 gold");
        equipment.captureDialog("europeEquipmentDialog",
                "europe-equipment-buy.png");
        equipment.cancel();
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        equipment.open(100, "Buy 50 muskets");
        equipment.escape();
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        equipment.open(100, "Buy 50 muskets");
        equipment.confirm();
        equipment.verifyState(100, 825, true, false, 0, PlaceEuropePier.class);
        europe.verifyGold(825);

        // Add horses without losing muskets, then sell each item separately.
        equipment.open(100, "Buy 50 horses");
        equipment.verifyQuote(
                "50 Horses\nUnit price: 5 gold\nTax: 17% (42 gold)\nTotal: 292 gold");
        equipment.confirm();
        equipment.verifyState(100, 533, true, true, 0, PlaceEuropePier.class);
        equipment.open(100, "Sell muskets");
        equipment.verifyQuote(
                "50 Muskets\nUnit price: 2 gold\nTax: 17% (17 gold)\nTotal: 83 gold");
        equipment.confirm();
        equipment.verifyState(100, 616, false, true, 0, PlaceEuropePier.class);
        equipment.open(100, "Sell horses");
        equipment.confirm();
        equipment.verifyState(100, 699, false, false, 0, PlaceEuropePier.class);

        // A fresh tools purchase is exactly 100, and its sale uses the bid.
        equipment.open(100, "Buy 100 tools");
        equipment.verifyQuote(
                "100 Tools\nUnit price: 2 gold\nTax: 17% (34 gold)\nTotal: 234 gold");
        equipment.confirm();
        equipment.verifyState(100, 465, false, false, 100,
                PlaceEuropePier.class);
        // The 100 purchased tools are -800 eighths of Dutch pressure, one full
        // step, so the tool bid rose from 1 to 2: 200 gross, 34 tax, 166 net.
        equipment.open(100, "Sell remaining tools");
        equipment.verifyQuote(
                "100 Tools\nUnit price: 2 gold\nTax: 17% (34 gold)\nTotal: 166 gold");
        equipment.confirm();
        equipment.verifyState(100, 631, false, false, 0, PlaceEuropePier.class);
        europe.verifyGold(631);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_02_remaining_tools_are_sold_explicitly_before_other_equipment()
            throws Exception {
        // A returning pioneer cannot silently discard the remaining 37 tools.
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        equipment.open(101, "Buy 50 muskets");
        equipment.verifyChoice("Buy 50 muskets", false, INCOMPATIBLE);
        equipment.cancel();
        equipment.verifyState(101, 1000, false, false, 37,
                PlaceEuropePier.class);
        equipment.open(101, "Sell remaining tools");
        equipment.verifyQuote(
                "37 Tools\nUnit price: 1 gold\nTax: 17% (7 gold)\nTotal: 30 gold");
        equipment.captureDialog("europeEquipmentDialog",
                "europe-equipment-partial-tools.png");
        equipment.confirm();
        equipment.verifyState(101, 1030, false, false, 0,
                PlaceEuropePier.class);

        // Tools also cannot replace horses; the player must sell first.
        equipment.open(101, "Buy 50 horses");
        equipment.confirm();
        equipment.open(101, "Buy 100 tools");
        equipment.verifyChoice("Buy 100 tools", false, INCOMPATIBLE);
        equipment.escape();
        equipment.verifyState(101, 738, false, true, 0, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_03_one_gold_short_is_rejected_and_exact_funds_succeed()
            throws Exception {
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        // Simulate another expense while the original offer is visible.
        equipment.open(100, "Buy 50 muskets");
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(174));
        equipment.confirm();
        equipment.verifyChoice("Buy 50 muskets", false, FUNDS);
        equipment.captureDialog("europeEquipmentDialog",
                "europe-equipment-insufficient.png");
        equipment.cancel();
        equipment.verifyState(100, 174, false, false, 0, PlaceEuropePier.class);

        // Reopen after obtaining exactly the missing gold; no overdraft is
        // used.
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(175));
        equipment.open(100, "Buy 50 muskets");
        equipment.verifyChoice("Buy 50 muskets", true, "");
        equipment.confirm();
        equipment.verifyState(100, 0, true, false, 0, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_04_changed_price_requires_a_second_confirmation() throws Exception {
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        // The market changes while the player is deciding on the old quote.
        equipment.open(100, "Buy 50 muskets");
        getContext().getRobot().interact(() -> getModel().getEurope()
                .getGoodsTradeForType(GoodsType.MUSKET, EuropeanNation.DUTCH)
                .setBuyPrice(4));
        equipment.confirm();
        equipment.verifyChoice("Buy 50 muskets", true, CHANGED);
        equipment.verifyQuote(
                "50 Muskets\nUnit price: 4 gold\nTax: 17% (34 gold)\nTotal: 234 gold");
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        equipment.captureDialog("europeEquipmentDialog",
                "europe-equipment-changed.png");

        // The refreshed offer is executed only after an explicit second click.
        equipment.confirm();
        equipment.verifyState(100, 766, true, false, 0, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_05_moved_colonist_cannot_accept_an_open_offer() throws Exception {
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        // A competing move invalidates the subject, with no partial charge.
        equipment.open(100, "Buy 50 muskets");
        getContext().getRobot().interact(() -> getModel().getUnitById(100)
                .placeToLocation(Location.of(24, 16)));
        equipment.confirm();
        equipment.verifyChoice("Buy 50 muskets", false, UNAVAILABLE);
        equipment.cancel();
        equipment.verifyState(100, 1000, false, false, 0, PlaceLocation.class);

    }

    @Test
    @Tag("local")
    void TC_06_keyboard_choices_confirm_the_same_offer() throws Exception {
        WelcomePage.of(getContext()).loadGame().openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        // A focused pier icon exposes the same choices to the keyboard.
        equipment.openWithKeyboard(100, 0);
        equipment.verifyChoice("Buy 50 muskets", true, "");
        equipment.escape();
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        equipment.openWithKeyboard(100, 1);
        equipment.verifyChoice("Buy 50 horses", true, "");
        equipment.confirmWithEnter();
        equipment.verifyState(100, 708, false, true, 0, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_07_equipment_and_identity_survive_embarkation_voyage_and_continue()
            throws Exception {
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        // Purchase and board the equipped person through the actual pier.
        equipment.open(100, "Buy 100 tools");
        equipment.confirm();
        europe.selectUnitFromPort(0);
        equipment.embark(100);
        equipment.verifyState(100, 766, false, false, 100,
                PlaceCargoSlot.class);
        equipment.verifyPassenger(100, 102);
        equipment.depart(102);
        final int duration = equipment.verifyVoyageSample(102);
        final int weather = equipment.getVoyageWeather(102);
        GamePage game = europe.close();

        // Save at departure so any sampled duration remains in transit.
        game.exitToMenu().verifyMainScreen();
        game = new FreePlayAuditPage(getContext()).continueGame();
        equipment.verifyVoyageMetadata(102, weather, duration, duration);
        equipment.verifyState(100, 766, false, false, 100,
                PlaceCargoSlot.class);
        equipment.verifyPassenger(100, 102);
        for (int remaining = duration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                equipment.verifyVoyageMetadata(102, weather, duration,
                        remaining);
            }
        }
        equipment.verifyArrived(102);
        equipment.verifyState(100, 766, false, false, 100,
                PlaceCargoSlot.class);
        equipment.verifyPassenger(100, 102);

        // Sail from the actual eastern arrival through four ordinary sea turns.
        equipment.verifyCarrierAt(102, Location.of(49, 15));
        equipment.showSeaRoute(Location.of(49, 15), Location.of(43, 16));
        game.moveMouseAtLocation(Location.of(49, 15));
        game.dragMouseAtLocation(Location.of(43, 16));
        equipment.verifyCarrierAt(102, Location.of(43, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(43, 16), Location.of(37, 16));
        game.moveMouseAtLocation(Location.of(43, 16));
        game.dragMouseAtLocation(Location.of(37, 16));
        equipment.verifyCarrierAt(102, Location.of(37, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(37, 16), Location.of(31, 16));
        game.moveMouseAtLocation(Location.of(37, 16));
        game.dragMouseAtLocation(Location.of(31, 16));
        equipment.verifyCarrierAt(102, Location.of(31, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(31, 16), Location.of(26, 16));
        game.moveMouseAtLocation(Location.of(31, 16));
        game.dragMouseAtLocation(Location.of(26, 16));
        equipment.verifyCarrierAt(102, Location.of(26, 16));
        game.nextTurnAndCloseDialogs();

        // Enter the owned colony and land outside its workforce with the tools.
        equipment.showSeaRoute(Location.of(26, 16), Location.of(24, 16));
        game.moveMouseAtLocation(Location.of(26, 16));
        game.dragMouseAtLocation(Location.of(24, 16));
        game.waitForNumberOfUnitsAt(Location.of(24, 16), 2);
        org.microcol.page.ColonyScreen colony = game
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        // Arrival already landed the passenger; no manual unload is needed.
        colony.verifyNumberOfUnitsAtPier(1);
        equipment.verifyState(100, 766, false, false, 100, PlaceLocation.class);
        equipment.verifyLandedPassenger(100, 102, Location.of(24, 16));
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        equipment.verifyState(100, 766, false, false, 100, PlaceLocation.class);
        equipment.verifyLandedPassenger(100, 102, Location.of(24, 16));
    }

    @Test
    @Tag("local")
    void TC_08_closed_european_trade_blocks_purchase_and_sale()
            throws Exception {
        // Load the same fixture after independence, with its pier still
        // occupied.
        com.google.gson.JsonObject saved = new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(FIXTURE.toPath()),
                com.google.gson.JsonObject.class);
        saved.getAsJsonObject("model").getAsJsonObject("gameManager")
                .getAsJsonArray("players").get(0).getAsJsonObject()
                .addProperty("declaredIndependence", true);
        File restricted = new File(System.getProperty("user.home"),
                "europe-restricted.microcol");
        java.nio.file.Files.writeString(restricted.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(restricted);
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        equipment.verifyMarketUnavailable(RESTRICTED);
        europe.selectUnitFromPort(0);
        europe.sellGoods(1,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.ORE));
        europe.verifyGoodsInShip(1,
                org.microcol.model.Goods.of(GoodsType.ORE, 7));
        europe.buyGoodsAndMoveItToCargoSlot(
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.MUSKET), 0);
        europe.verifyCargoSlotEmpty(0, 0);
        europe.verifyGold(1000);
        equipment.captureDialog("europeMarketRestriction",
                "europe-market-restricted.png");
        equipment.open(100, "Buy 50 muskets");
        equipment.verifyChoice("Buy 50 muskets", false, RESTRICTED);
        equipment.cancel();
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        equipment.open(101, "Sell remaining tools");
        equipment.verifyChoice("Sell remaining tools", false, RESTRICTED);
        equipment.cancel();
        equipment.verifyState(101, 1000, false, false, 37,
                PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_10_protested_muskets_stay_closed_while_horses_keep_their_quote()
            throws Exception {
        // A real protest destroys the muskets and boycotts them; the rate
        // stays 17%.
        org.microcol.page.TaxDemandPage taxes = new org.microcol.page.TaxDemandPage(
                getContext());
        taxes.prepare(new org.microcol.page.TaxDemandPage.Fixture(FIXTURE,
                "tax-equipment").due()
                .warehouse(java.util.Map.of(GoodsType.MUSKET, 100,
                        GoodsType.CORN, 40)));
        WelcomePage.of(getContext()).loadGame();
        taxes.startUpdate();
        taxes.protest();
        taxes.finishUpdate();
        taxes.verifyTax(17, java.util.Map.of(GoodsType.MUSKET, 400));
        // The pier refuses to buy the closed commodity and changes nothing.
        EuropePortScreen europe = GamePage.of(getContext()).openEuroperPort();
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        taxes.verifyBoycottMarked(GoodsType.MUSKET, true);
        taxes.verifyBoycottMarked(GoodsType.HORSE, false);
        equipment.open(100, "Buy 50 muskets");
        equipment.verifyChoice("Buy 50 muskets", false,
                "This commodity is boycotted by the Crown. Nothing was changed.");
        equipment.cancel();
        equipment.verifyState(100, 1000, false, false, 0,
                PlaceEuropePier.class);
        // Another commodity keeps the unchanged taxed quote.
        equipment.open(100, "Buy 50 horses");
        equipment.verifyQuote(
                "50 Horses\nUnit price: 5 gold\nTax: 17% (42 gold)\nTotal: 292 gold");
        equipment.confirm();
        equipment.verifyState(100, 708, false, true, 0, PlaceEuropePier.class);
        europe.verifyGold(708);
        // Continue keeps the closed commodity without asking again.
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        taxes.verifyTax(17, java.util.Map.of(GoodsType.MUSKET, 400));
        equipment.verifyState(100, 708, false, true, 0, PlaceEuropePier.class);
    }

    @Test
    @Tag("local")
    void TC_09_czech_offer_is_readable_and_keyboard_confirmation_works()
            throws Exception {
        // Select Czech through the existing settings and open Europe with E.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        new FreePlayAuditPage(getContext()).press(javafx.scene.input.KeyCode.E);
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        equipment.open(100, "Koupit 50 mušket");
        equipment.verifyChoice("Koupit 50 mušket", true, "");
        equipment.captureDialog("europeEquipmentDialog",
                "europe-equipment-czech.png");
        equipment.confirmWithEnter();
        equipment.verifyState(100, 825, true, false, 0, PlaceEuropePier.class);
    }
}
