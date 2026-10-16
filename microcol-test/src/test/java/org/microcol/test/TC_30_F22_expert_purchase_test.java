package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceLocation;
import org.microcol.model.PlaceConstructionSlot;
import org.microcol.model.GoodsType;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Hiring, transporting and employing a persistent European specialist. */
@ExtendWith(ApplicationExtension.class)
public class TC_30_F22_expert_purchase_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_buy_once_reopen_and_continue() throws Exception {
        // Find the priced specialty and cancel without changing the pier.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        europe.openBuyUnitScreen();
        purchase.verifyOffer(true, "");
        purchase.captureDialog("mainVbox", "expert-hiring.png");
        purchase.escape();
        purchase.verifyTreasuryAndPierExperts(4400, List.of());

        // A duplicated confirmation buys only one expert, even with funds for
        // two.
        europe.openBuyUnitScreen();
        purchase.confirmAndDeliverDuplicateAction();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(103));
        europe.verifyGold(2200);

        // Reopening is an independent purchase; exactly 2200 gold is
        // sufficient.
        europe.openBuyUnitScreen();
        purchase.verifyOffer(true, "");
        purchase.confirm();
        purchase.verifyTreasuryAndPierExperts(0, List.of(103, 104));
        GamePage game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.openEuroperPort();
        purchase.verifyTreasuryAndPierExperts(0, List.of(103, 104));
    }

    @Test
    @Tag("local")
    void TC_02_one_gold_short_disables_hiring_with_visible_reason()
            throws Exception {
        // Prepare a player with one gold less than the advertised price.
        com.google.gson.JsonObject saved = new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(FIXTURE.toPath()),
                com.google.gson.JsonObject.class);
        saved.getAsJsonObject("model").getAsJsonObject("gameManager")
                .getAsJsonArray("players").forEach(value -> {
                    if (value.getAsJsonObject().get("name").getAsString()
                            .equals("Dutch")) {
                        value.getAsJsonObject().addProperty("gold", 2199);
                    }
                });
        File fixture = new File(System.getProperty("user.home"),
                "expert-one-short.microcol");
        java.nio.file.Files.writeString(fixture.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(fixture);
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.openBuyUnitScreen();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        purchase.verifyOffer(false, "Not enough gold.");
        purchase.escape();
        purchase.verifyTreasuryAndPierExperts(2199, List.of());
    }

    @Test
    @Tag("local")
    void TC_03_expense_during_offer_rejects_without_creating_an_expert()
            throws Exception {
        // The quote is affordable when opened, but another expense consumes
        // funds.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.openBuyUnitScreen();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        purchase.verifyOffer(true, "");
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(2199));
        purchase.confirm();
        purchase.verifyInsufficientFundsWarning();
        purchase.closeWarning();
        purchase.verifyOffer(false, "Not enough gold.");
        purchase.escape();
        purchase.verifyTreasuryAndPierExperts(2199, List.of());
    }

    @Test
    @Tag("local")
    void TC_04_hired_expert_sails_home_and_works_in_the_smithy()
            throws Exception {
        // Hire the specialist, equip them, and persist the same person on the
        // pier.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        europe.openBuyUnitScreen();
        purchase.confirm();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(103));
        equipment.open(103, "Buy 50 muskets");
        equipment.confirm();
        purchase.verifyExpertState(103, 2025, true, PlaceEuropePier.class);
        GamePage game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifyExpertState(103, 2025, true, PlaceEuropePier.class);
        europe = game.openEuroperPort();
        europe.selectUnitFromPort(0);
        equipment.embark(103);
        equipment.verifyPassenger(103, 102);
        purchase.verifyExpertState(103, 2025, true, PlaceCargoSlot.class);
        equipment.depart(102);
        final int voyageDuration = equipment.verifyVoyageSample(102);
        final int voyageWeather = equipment.getVoyageWeather(102);
        game = europe.close();

        // Continue keeps the identity, equipment and sampled voyage together.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        equipment.verifyVoyageMetadata(102, voyageWeather, voyageDuration,
                voyageDuration);
        equipment.verifyPassenger(103, 102);
        purchase.verifyExpertState(103, 2025, true, PlaceCargoSlot.class);
        for (int remaining = voyageDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                equipment.verifyVoyageMetadata(102, voyageWeather,
                        voyageDuration, remaining);
            }
        }
        equipment.verifyArrived(102);

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
        game.waitForNumberOfUnitsAt(Location.of(24, 16), 1);
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        colony.selectUnitFromPort(0);
        new org.microcol.page.PassengerTransferPage(getContext())
                .disembarkInPort(103, 0, false);
        purchase.verifyExpertState(103, 2025, true, PlaceLocation.class);
        equipment.verifyLandedPassenger(103, 102, Location.of(24, 16));
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifyExpertState(103, 2025, true, PlaceLocation.class);
        equipment.verifyLandedPassenger(103, 102, Location.of(24, 16));

        // Assign the expert through the colony panel; equipment returns to
        // stock.
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        purchase.hoverSmithy();
        purchase.verifyWorkplaceDescription("Blacksmith house");
        purchase.assignToSmithy(103);
        purchase.hoverSmithy();
        purchase.verifyWorkplaceDescription(
                "Blacksmith house: Master Blacksmith");
        purchase.verifyExpertState(103, 2025, false,
                PlaceConstructionSlot.class);
        purchase.verifySmithyWorker(103, 0, 90);
        colony.verifyThatProductionIs(GoodsType.TOOLS, 6);
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifyExpertState(103, 2025, false,
                PlaceConstructionSlot.class);
        purchase.verifySmithyWorker(103, 0, 90);
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        purchase.verifySmithyWorker(103, 6, 84);
        purchase.hoverSmithy();
        purchase.verifyWorkplaceDescription(
                "Blacksmith house: Master Blacksmith");
        purchase.captureDialog("id_blacksmith", "expert-smithy.png");

        // Leaving the job removes the worker's name from the building
        // description.
        purchase.leaveSmithy(103);
        purchase.verifyExpertState(103, 2025, false, PlaceLocation.class);
        purchase.hoverSmithy();
        purchase.verifyWorkplaceDescription("Blacksmith house");
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_05_removed_buyer_cannot_confirm_a_stale_offer() throws Exception {
        // Simulate removal of the buyer while their old offer is still open.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.openBuyUnitScreen();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        purchase.verifyOffer(true, "");
        org.microcol.model.Player buyer = getModel().getPlayerByName("Dutch");
        getContext().getRobot().interact(
                () -> getModel().getPlayerStore().getPlayers().remove(buyer));
        purchase.confirm();
        purchase.verifyUnavailableWarning();
        purchase.closeUnavailableWarning();
        // Restore the simulated membership loss to inspect the untouched
        // player.
        getContext().getRobot().interact(
                () -> getModel().getPlayerStore().getPlayers().add(0, buyer));
        purchase.verifyTreasuryAndPierExperts(4400, List.of());
        europe.verifyGold(4400);
    }

    @Test
    @Tag("local")
    void TC_06_czech_hiring_offer_supports_keyboard_confirmation()
            throws Exception {
        // Choose Czech in settings, then hire through the keyboard focus order.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        settings.setCzechLanguage();
        EuropePortScreen europe = settings.goBack().loadGame()
                .openEuroperPort("Evropa");
        europe.openBuyUnitScreen();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        purchase.verifyCzechOffer();
        purchase.captureDialog("mainVbox", "expert-hiring-czech.png");
        purchase.confirmWithKeyboard();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(103));
    }

    @Test
    @Tag("local")
    void TC_07_replaced_game_rejects_the_old_hiring_offer() throws Exception {
        // An asynchronous load replaces the game while the old offer is open.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.openBuyUnitScreen();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        purchase.verifyOffer(true, "");
        org.microcol.model.Model previous = getModel();
        getContext().getRobot()
                .interact(() -> getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameController.class)
                        .loadModelFromFile(FIXTURE));
        purchase.confirm();
        purchase.verifyUnavailableWarning();
        purchase.closeUnavailableWarning();
        purchase.verifyReplacedGameUntouched(previous, 4400);
        purchase.verifyTreasuryAndPierExperts(4400, List.of());
    }

    @Test
    @Tag("local")
    void TC_08_each_ship_renders_boards_and_continues_its_voyage()
            throws Exception {
        String[] types = { "CARAVEL", "MERCHANTMAN", "GALLEON", "PRIVATEER",
                "FRIGATE", "MAN_OF_WAR" };
        int[] capacities = { 2, 4, 6, 2, 4, 6 };
        for (int i = 0; i < types.length; i++) {
            String typeName = types[i];
            int holds = capacities[i];
            // Prepare the same port journey for each supported carrier, with
            // one
            // goods hold.
            org.microcol.model.UnitType type = org.microcol.model.UnitType
                    .valueOf(typeName);
            com.google.gson.JsonObject saved = new com.google.gson.Gson()
                    .fromJson(java.nio.file.Files.readString(FIXTURE.toPath()),
                            com.google.gson.JsonObject.class);
            for (com.google.gson.JsonElement value : saved
                    .getAsJsonObject("model").getAsJsonArray("units")) {
                com.google.gson.JsonObject unit = value.getAsJsonObject();
                if (unit.get("id").getAsInt() == 102) {
                    unit.addProperty("type", typeName);
                    unit.addProperty("availableMoves", type.getSpeed());
                    com.google.gson.JsonArray slots = new com.google.gson.JsonArray();
                    for (int index = 0; index < holds; index++) {
                        com.google.gson.JsonObject slot = new com.google.gson.JsonObject();
                        if (index == 1) {
                            slot.addProperty("goodsType", "ORE");
                            slot.addProperty("amount", 7);
                        }
                        slots.add(slot);
                    }
                    unit.getAsJsonObject("cargo").add("slots", slots);
                }
            }
            File fixture = new File(System.getProperty("user.home"),
                    "ship-roster-" + typeName + ".microcol");
            java.nio.file.Files.writeString(fixture.toPath(), saved.toString());
            org.mockito.Mockito.when(getContext()
                    .getClassFromGuice(
                            org.microcol.gui.FileSelectingService.class)
                    .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                    .thenReturn(fixture);
            EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                    .openEuroperPort();
            europe.verifyShip(102, type, holds);
            europe.selectUnitFromPort(0);
            europe.verifyAvailableHoldCount(holds);
            EuropeEquipmentPage equipment = new EuropeEquipmentPage(
                    getContext());
            equipment.embark(100);
            equipment.verifyPassenger(100, 102);

            // Depart through the visible Europe panel, then save and continue
            // in
            // transit.
            equipment.depart(102);
            final int voyageDuration = equipment.verifyVoyageSample(102);
            final int voyageWeather = equipment.getVoyageWeather(102);
            GamePage game = europe.close();
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            equipment.verifyPassenger(100, 102);
            equipment.verifyVoyageMetadata(102, voyageWeather, voyageDuration,
                    voyageDuration);
            for (int remaining = voyageDuration
                    - 1; remaining >= 0; remaining--) {
                game.nextTurnAndCloseDialogs();
                if (remaining > 0) {
                    equipment.verifyVoyageMetadata(102, voyageWeather,
                            voyageDuration, remaining);
                }
            }
            equipment.verifyArrived(102);
            equipment.verifyPassenger(100, 102);
            game.exitToMenu();
        }
    }

    @Test
    @Tag("local")
    void TC_09_buy_new_ship_and_preserve_it_after_continue() throws Exception {
        String[] types = { "CARAVEL", "MERCHANTMAN", "PRIVATEER" };
        String[] names = { "Caravel", "Merchantman", "Privateer" };
        int[] prices = { 1000, 2000, 2000 };
        int[] capacities = { 2, 4, 2 };
        for (int i = 0; i < types.length; i++) {
            String typeName = types[i];
            String name = names[i];
            int price = prices[i];
            int holds = capacities[i];
            // The offer exposes the named ship and its exact price, with no
            // royal
            // ship for sale.
            org.microcol.model.UnitType type = org.microcol.model.UnitType
                    .valueOf(typeName);
            EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                    .openEuroperPort();
            org.microcol.page.BuyUnitScreen offer = europe.openBuyUnitScreen();
            offer.verifyShipOffer(type, name, price, true);
            offer.verifyRoyalShipNotOffered();
            europe = offer.buyUnitByName(name);
            europe.verifyGold(4400 - price);
            europe.verifyShip(103, type, holds);
            europe.selectUnitFromPort(1);
            europe.verifyAvailableHoldCount(holds);

            // Menu and Continue retain the purchased identity and the single
            // charge.
            europe.close().exitToMenu();
            europe = new FreePlayAuditPage(getContext()).continueGame()
                    .openEuroperPort();
            europe.verifyGold(4400 - price);
            europe.verifyShip(103, type, holds);
            europe.close().exitToMenu();
        }
    }

    @Test
    @Tag("local")
    void TC_10_czech_ship_offers_are_named_and_royal_ship_is_absent()
            throws Exception {
        // The expanded offer keeps localized names, prices and accessible
        // confirmation controls.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        settings.setCzechLanguage();
        EuropePortScreen europe = settings.goBack().loadGame()
                .openEuroperPort("Evropa");
        org.microcol.page.BuyUnitScreen offer = europe.openBuyUnitScreen();
        offer.verifyShipOffer(org.microcol.model.UnitType.CARAVEL, "karavela",
                1000, true);
        offer.verifyShipOffer(org.microcol.model.UnitType.MERCHANTMAN,
                "obchodní loď", 2000, true);
        offer.verifyShipOffer(org.microcol.model.UnitType.PRIVATEER,
                "korzárská loď", 2000, true);
        offer.verifyRoyalShipNotOffered();
        new ExpertPurchasePage(getContext()).captureDialog("mainVbox",
                "ship-roster-czech.png");
        new ExpertPurchasePage(getContext()).escape();
        europe.verifyGold(4400);
    }

    @Test
    @Tag("local")
    void TC_11_complete_specialist_offer_english() throws Exception {
        // Inspect every priced specialist, including entries below the
        // viewport.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        europe.openBuyUnitScreen();
        purchase.revealSpecialist("EXPERT_FARMER");
        purchase.verifySpecialistOffer("EXPERT_FARMER", "Expert Farmer", 1000,
                "Hire specialist for ");
        purchase.revealSpecialist("EXPERT_FISHERMAN");
        purchase.verifySpecialistOffer("EXPERT_FISHERMAN", "Expert Fisherman",
                1000, "Hire specialist for ");
        purchase.revealSpecialist("EXPERT_FUR_TRAPPER");
        purchase.verifySpecialistOffer("EXPERT_FUR_TRAPPER",
                "Expert Fur Trapper", 1000, "Hire specialist for ");
        purchase.revealSpecialist("EXPERT_SILVER_MINER");
        purchase.verifySpecialistOffer("EXPERT_SILVER_MINER",
                "Expert Silver Miner", 1000, "Hire specialist for ");
        purchase.revealSpecialist("EXPERT_LUMBERJACK");
        purchase.verifySpecialistOffer("EXPERT_LUMBERJACK", "Expert Lumberjack",
                1000, "Hire specialist for ");
        purchase.revealSpecialist("EXPERT_ORE_MINER");
        purchase.verifySpecialistOffer("EXPERT_ORE_MINER", "Expert Ore Miner",
                1000, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_CARPENTER");
        purchase.verifySpecialistOffer("MASTER_CARPENTER", "Master Carpenter",
                1000, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_DISTILLER");
        purchase.verifySpecialistOffer("MASTER_DISTILLER", "Master Distiller",
                2000, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_WEAVER");
        purchase.verifySpecialistOffer("MASTER_WEAVER", "Master Weaver", 2000,
                "Hire specialist for ");
        purchase.revealSpecialist("MASTER_TOBACCONIST");
        purchase.verifySpecialistOffer("MASTER_TOBACCONIST",
                "Master Tobacconist", 2000, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_FUR_TRADER");
        purchase.verifySpecialistOffer("MASTER_FUR_TRADER", "Master Fur Trader",
                2000, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_BLACKSMITH");
        purchase.verifySpecialistOffer("MASTER_BLACKSMITH", "Master Blacksmith",
                2200, "Hire specialist for ");
        purchase.revealSpecialist("MASTER_GUNSMITH");
        purchase.verifySpecialistOffer("MASTER_GUNSMITH", "Master Gunsmith",
                2000, "Hire specialist for ");
        purchase.revealSpecialist("FIREBRAND_PREACHER");
        purchase.verifySpecialistOffer("FIREBRAND_PREACHER",
                "Firebrand Preacher", 3000, "Hire specialist for ");
        purchase.revealSpecialist("ELDER_STATESMAN");
        purchase.verifySpecialistOffer("ELDER_STATESMAN", "Elder Statesman",
                3000, "Hire specialist for ");
        purchase.revealSpecialist("SEASONED_SCOUT");
        purchase.verifySpecialistOffer("SEASONED_SCOUT", "Seasoned Scout", 1000,
                "Hire specialist for ");
        purchase.revealSpecialist("HARDY_PIONEER");
        purchase.verifySpecialistOffer("HARDY_PIONEER", "Hardy Pioneer", 1000,
                "Hire specialist for ");
        purchase.revealSpecialist("VETERAN_SOLDIER");
        purchase.verifySpecialistOffer("VETERAN_SOLDIER", "Veteran Soldier",
                2000, "Hire specialist for ");
        purchase.revealSpecialist("JESUIT_MISSIONARY");
        purchase.verifySpecialistOffer("JESUIT_MISSIONARY", "Jesuit Missionary",
                3000, "Hire specialist for ");
        purchase.verifyExcludedPlanters();
        purchase.captureDialog("mainVbox", "specialists-english.png");
        purchase.escape();
        europe.verifyGold(4400);

        // Reach the final list entry by keyboard and retain it after disk
        // reload.
        europe.openBuyUnitScreen();
        purchase.confirmSpecialistWithKeyboard("JESUIT_MISSIONARY");
        purchase.verifyPurchasedSpecialist(103, "JESUIT_MISSIONARY", 1400);
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifyPurchasedSpecialist(103, "JESUIT_MISSIONARY", 1400);
    }

    @Test
    @Tag("local")
    void TC_12_complete_specialist_offer_cs() throws Exception {
        // Inspect every priced specialist, including entries below the
        // viewport.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        settings.setCzechLanguage();
        EuropePortScreen europe = settings.goBack().loadGame()
                .openEuroperPort("Evropa");
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        europe.openBuyUnitScreen();
        purchase.revealSpecialist("EXPERT_FARMER");
        purchase.verifySpecialistOffer("EXPERT_FARMER", "zkušený farmář", 1000,
                "Najmout odborníka za ");
        purchase.revealSpecialist("EXPERT_FISHERMAN");
        purchase.verifySpecialistOffer("EXPERT_FISHERMAN", "zkušený rybář",
                1000, "Najmout odborníka za ");
        purchase.revealSpecialist("EXPERT_FUR_TRAPPER");
        purchase.verifySpecialistOffer("EXPERT_FUR_TRAPPER",
                "zkušený lovec kožešin", 1000, "Najmout odborníka za ");
        purchase.revealSpecialist("EXPERT_SILVER_MINER");
        purchase.verifySpecialistOffer("EXPERT_SILVER_MINER",
                "zkušený těžař stříbra", 1000, "Najmout odborníka za ");
        purchase.revealSpecialist("EXPERT_LUMBERJACK");
        purchase.verifySpecialistOffer("EXPERT_LUMBERJACK",
                "zkušený dřevorubec", 1000, "Najmout odborníka za ");
        purchase.revealSpecialist("EXPERT_ORE_MINER");
        purchase.verifySpecialistOffer("EXPERT_ORE_MINER", "zkušený těžař rudy",
                1000, "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_CARPENTER");
        purchase.verifySpecialistOffer("MASTER_CARPENTER", "mistr tesař", 1000,
                "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_DISTILLER");
        purchase.verifySpecialistOffer("MASTER_DISTILLER", "mistr palírník",
                2000, "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_WEAVER");
        purchase.verifySpecialistOffer("MASTER_WEAVER", "mistr tkadlec", 2000,
                "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_TOBACCONIST");
        purchase.verifySpecialistOffer("MASTER_TOBACCONIST",
                "mistr výrobce doutníků", 2000, "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_FUR_TRADER");
        purchase.verifySpecialistOffer("MASTER_FUR_TRADER", "mistr kožešník",
                2000, "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_BLACKSMITH");
        purchase.verifySpecialistOffer("MASTER_BLACKSMITH", "mistr kovář", 2200,
                "Najmout odborníka za ");
        purchase.revealSpecialist("MASTER_GUNSMITH");
        purchase.verifySpecialistOffer("MASTER_GUNSMITH", "mistr puškař", 2000,
                "Najmout odborníka za ");
        purchase.revealSpecialist("FIREBRAND_PREACHER");
        purchase.verifySpecialistOffer("FIREBRAND_PREACHER", "horlivý kazatel",
                3000, "Najmout odborníka za ");
        purchase.revealSpecialist("ELDER_STATESMAN");
        purchase.verifySpecialistOffer("ELDER_STATESMAN", "zkušený státník",
                3000, "Najmout odborníka za ");
        purchase.revealSpecialist("SEASONED_SCOUT");
        purchase.verifySpecialistOffer("SEASONED_SCOUT", "zkušený průzkumník",
                1000, "Najmout odborníka za ");
        purchase.revealSpecialist("HARDY_PIONEER");
        purchase.verifySpecialistOffer("HARDY_PIONEER", "zdatný pionýr", 1000,
                "Najmout odborníka za ");
        purchase.revealSpecialist("VETERAN_SOLDIER");
        purchase.verifySpecialistOffer("VETERAN_SOLDIER", "vojenský veterán",
                2000, "Najmout odborníka za ");
        purchase.revealSpecialist("JESUIT_MISSIONARY");
        purchase.verifySpecialistOffer("JESUIT_MISSIONARY",
                "jezuitský misionář", 3000, "Najmout odborníka za ");
        purchase.verifyExcludedPlanters();
        purchase.captureDialog("mainVbox", "specialists-cs.png");
        purchase.escape();
        europe.verifyGold(4400);

        // Reach the final list entry by keyboard and retain it after disk
        // reload.
        europe.openBuyUnitScreen();
        purchase.confirmSpecialistWithKeyboard("JESUIT_MISSIONARY");
        purchase.verifyPurchasedSpecialist(103, "JESUIT_MISSIONARY", 1400);
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifyPurchasedSpecialist(103, "JESUIT_MISSIONARY", 1400);
    }
}
