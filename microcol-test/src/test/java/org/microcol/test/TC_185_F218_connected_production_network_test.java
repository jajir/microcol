package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRouteKind;
import org.microcol.model.TradeRouteStop;
import org.microcol.model.UnitType;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.EducationPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ProductionNetworkPage;
import org.microcol.page.TradeRouteCargoDialog;
import org.microcol.page.TradeRouteDialog;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Two Dutch colonies sustain one production network (R-0218, UC-116). The
 * galleon in Delft brings ore that nobody has unloaded yet, so New Amsterdam
 * (id 0, (26,16)) has no ore and its two blacksmiths and its shop upgrade stand
 * still. The wagon follows the route New Amsterdam, Delft: it loads ore in
 * Delft and unloads it in New Amsterdam, two squares apart, one square a turn.
 * New Amsterdam also teaches a student. The prepared world is built by
 * {@link ProductionNetworkFixture} from F109 before the game loads; after that
 * every change is a player command or an owner turn.
 *
 * <p>
 * Independent ledger, turn counts after loading. The wagon starts at the Delft
 * stop, so a complete cycle runs from Delft through New Amsterdam back to
 * Delft. Wagon stops: Delft (index 1) at turns 1, 4, 8; New Amsterdam (index 0)
 * at turns 2, 6. Cycle 1 runs from turn 0 to turn 4, cycle 2 from turn 4 to
 * turn 8. New Amsterdam's blacksmiths turn 3 ore into 3 tools each a turn, from
 * the turn after the ore arrives, and the shop costs 64 hammers and 20 tools.
 * <ul>
 * <li>Turn 1: wagon between, nothing loaded (Delft ore 0); course 1 of 4.</li>
 * <li>Turn 2: wagon in New Amsterdam, nothing unloaded; half of cycle 1; no
 * tools; course 2. The player then unloads the galleon's 60 ore in Delft.</li>
 * <li>Turn 3: wagon between; course 3. Turn 4: wagon back in Delft with 60 ore,
 * Delft ore 0; cycle 1 complete; the student has become a Master Carpenter,
 * same unit 201. The first test saves and continues here.</li>
 * <li>Turn 5: wagon between. Turn 6: wagon in New Amsterdam, hold empty, ore
 * 60; half of cycle 2; tools still 0.</li>
 * <li>Turn 7: ore 54, tools 6. Turn 8: wagon back in Delft, loads nothing;
 * cycle 2 complete; ore 48, tools 12. Turn 9: ore 42, tools 18.</li>
 * <li>Turn 10: ore 36, tools 24; the shop is built once: hammers 64 to 0, tools
 * 24 to 4, house 0, shop 1.</li>
 * </ul>
 * Unrelated goods never change: muskets 50 in both colonies, no horses, and the
 * galleon's rum, sugar, tobacco and cotton 100 each. Residents stay 5 and 1.
 * The first test saves and continues mid-leg in cycle 1 and again between the
 * two complete cycles; the second runs without leaving the game, and both
 * expect the same balances.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_185_F218_connected_production_network_test
        extends AbstractMicroColTest {

    private static final File SOURCE = new File(
            "src/test/scenarios/F109-trade-route-assignment.microcol");
    private static final String PREPARED = "production-network-prepared.microcol";
    private static final Location NEW_AMSTERDAM_LOCATION = Location.of(26, 16);
    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int WAGON = ProductionNetworkFixture.WAGON;
    private static final int GALLEON = 3;
    private static final int TEACHER = ProductionNetworkFixture.TEACHER;
    private static final int STUDENT = ProductionNetworkFixture.STUDENT;
    private static final TradeRouteStop NEW_AMSTERDAM_STOP = TradeRouteStop
            .ofColony(NEW_AMSTERDAM);
    private static final TradeRouteStop DELFT_STOP = TradeRouteStop
            .ofColony(DELFT);
    /** Stop indices of the route New Amsterdam, Delft. */
    private static final int AT_NEW_AMSTERDAM = 0;
    private static final int AT_DELFT = 1;
    /** Index of the ore among the warehouse goods. */
    private static final int ORE_IN_WAREHOUSE = 6;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_ore_shortage_pauses_work_delivery_resumes_it_and_continue_keeps_the_ledger()
            throws Exception {
        // The prepared world is loaded as it was written.
        ProductionNetworkFixture.prepare(getContext(), SOURCE, PREPARED);
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        final EducationPage education = new EducationPage(getContext());
        final ConstructionPage construction = new ConstructionPage(
                getContext());
        final ProductionNetworkPage ledger = new ProductionNetworkPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();

        // Start of the ledger: no ore, no tools, 64 hammers, five and one
        // residents, a wagon in Delft and the galleon's ore still on board.
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.MUSKET, 50);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HORSE, 0);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyStock(DELFT, GoodsType.MUSKET, 50);
        ledger.verifyResidents(NEW_AMSTERDAM, 5);
        ledger.verifyResidents(DELFT, 1);
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_HOUSE,
                1);
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_SHOP,
                0);
        ledger.verifyUnitType(STUDENT, UnitType.COLONIST);
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHold(GALLEON, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyHold(GALLEON, 1, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON, 3, Goods.of(GoodsType.SUGAR, 100));
        cargo.verifyHold(GALLEON, 4, Goods.of(GoodsType.TOBACCO, 100));
        cargo.verifyHold(GALLEON, 5, Goods.of(GoodsType.COTTON, 100));

        // New Amsterdam teaches the student and queues the shop upgrade.
        ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        education.open();
        education.chooseTeacher(TEACHER);
        education.chooseStudent(STUDENT);
        education.verifyPreview("School", 4, "Master Carpenter");
        education.assign();
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 0, 4);
        education.close();
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.verifyOffer(true, "Available.", 64, 20);
        construction.confirm();
        construction.cancel();
        construction.verifyPickerClosed();
        ledger.verifyQueue(NEW_AMSTERDAM, "BLACKSMITHS_SHOP");
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        game = colony.close();

        // The route New Amsterdam, Delft carries ore from Delft to New
        // Amsterdam; the wagon in Delft starts at the Delft stop.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        route.open();
        route.verifyOpenedFor("Delft");
        route.enterName("Ore Line");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, NEW_AMSTERDAM_STOP);
        route.chooseStop(1, DELFT_STOP);
        route.create();
        route.verifyStoredRoute("Ore Line", TradeRouteKind.LAND,
                "New Amsterdam", "Delft");
        cargo.open();
        cargo.chooseRoute("Ore Line");
        cargo.chooseStop(AT_NEW_AMSTERDAM);
        cargo.addUnload(GoodsType.ORE);
        cargo.save();
        cargo.chooseStop(AT_DELFT);
        cargo.addLoad(GoodsType.ORE);
        cargo.save();
        cargo.chooseCarrier(0);
        cargo.chooseFirstStop(AT_DELFT);
        cargo.verifyAssignEnabled(true);
        cargo.assign();
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.close();
        cargo.verifyDialogClosed();
        game = colony.close();

        // Turn 1: Delft has no ore, so the Delft stop loads nothing. The wagon
        // is on its way, the blacksmiths have no ore, the course is at 1.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON, NEW_AMSTERDAM_LOCATION, DELFT_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 1, 4);

        // Turn 2: the wagon arrives empty, halfway through cycle 1. Nothing was
        // produced and nothing unrelated was touched.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.MUSKET, 50);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyStock(DELFT, GoodsType.MUSKET, 50);
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 2, 4);
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        construction.verifyStatus("Waiting for tools: 20.");
        game = colony.close();

        // Save and Continue in the middle of cycle 1 keep the wagon, its stop,
        // the course, the queue and every stock, and move nothing.
        cargo.rememberUnitLocation(WAGON);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyUnitAtRememberedLocation(WAGON);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        ledger.verifyQueue(NEW_AMSTERDAM, "BLACKSMITHS_SHOP");
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 2, 4);
        cargo.verifyHold(GALLEON, 0, Goods.of(GoodsType.ORE, 60));

        // The player restores the delivery: the galleon's ore goes to the Delft
        // warehouse, where the wagon will load it.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_IN_WAREHOUSE);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 60));
        cargo.verifyHoldEmpty(GALLEON, 0);
        ledger.verifyStock(DELFT, GoodsType.ORE, 60);
        game = colony.close();

        // Turn 3: the wagon sets out; still no ore in New Amsterdam.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON, NEW_AMSTERDAM_LOCATION, DELFT_LOCATION);
        ledger.verifyStock(DELFT, GoodsType.ORE, 60);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 3, 4);

        // Turn 4: back in Delft the wagon loads all 60 ore; cycle 1 is
        // complete. The student graduates as the same unit and still lives in
        // New Amsterdam.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHold(WAGON, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyNoCourse(NEW_AMSTERDAM);
        ledger.verifyUnitType(STUDENT, UnitType.MASTER_CARPENTER);
        ledger.verifyResident(NEW_AMSTERDAM, STUDENT);
        ledger.verifyResidents(NEW_AMSTERDAM, 5);

        // Save and Continue between the two complete cycles neither move, load
        // nor graduate anything again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHold(WAGON, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        ledger.verifyQueue(NEW_AMSTERDAM, "BLACKSMITHS_SHOP");
        ledger.verifyNoCourse(NEW_AMSTERDAM);
        ledger.verifyUnitType(STUDENT, UnitType.MASTER_CARPENTER);
        ledger.verifyResident(NEW_AMSTERDAM, STUDENT);
        ledger.verifyUnitType(TEACHER, UnitType.MASTER_CARPENTER);
        ledger.verifyResident(NEW_AMSTERDAM, TEACHER);
        ledger.verifyResidents(NEW_AMSTERDAM, 5);

        // Turn 5: the wagon carries the ore.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON, NEW_AMSTERDAM_LOCATION, DELFT_LOCATION);
        cargo.verifyHold(WAGON, 0, Goods.of(GoodsType.ORE, 60));
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);

        // Turn 6: the ore is unloaded once, halfway through cycle 2;
        // production has not run yet.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 60);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);

        // Turn 7: the blacksmiths resume: 6 ore become 6 tools, no free output.
        game.nextTurnAndCloseDialogs();
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 54);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 6);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        construction.verifyStatus("Waiting for tools: 14.");
        game = colony.close();

        // Turn 8: the wagon is back in Delft and cycle 2 is complete; Delft
        // holds no ore, so the stop loads nothing and spins nothing.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 48);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 12);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyStock(DELFT, GoodsType.MUSKET, 50);
        ledger.verifyQueue(NEW_AMSTERDAM, "BLACKSMITHS_SHOP");

        // Turn 9: two tools are still missing.
        game.nextTurnAndCloseDialogs();
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 42);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 18);
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        construction.verifyStatus("Waiting for tools: 2.");
        game = colony.close();

        // Turn 10: 24 tools exist, the shop is built once and pays 64 hammers
        // and 20 tools.
        game.nextTurnAndCloseDialogs();
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_SHOP,
                1);
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_HOUSE,
                0);
        ledger.verifyQueue(NEW_AMSTERDAM);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 36);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 4);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 0);

        // The end state: unrelated goods, workers and food are untouched.
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.MUSKET, 50);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HORSE, 0);
        ledger.verifyStock(DELFT, GoodsType.MUSKET, 50);
        ledger.verifyStock(DELFT, GoodsType.HORSE, 0);
        cargo.verifyHold(GALLEON, 1, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON, 3, Goods.of(GoodsType.SUGAR, 100));
        cargo.verifyHold(GALLEON, 4, Goods.of(GoodsType.TOBACCO, 100));
        cargo.verifyHold(GALLEON, 5, Goods.of(GoodsType.COTTON, 100));
        ledger.verifyResidents(NEW_AMSTERDAM, 5);
        ledger.verifyResidents(DELFT, 1);
        ledger.verifyResident(NEW_AMSTERDAM, STUDENT);
        ledger.verifyUnitType(STUDENT, UnitType.MASTER_CARPENTER);
        ledger.verifyColonyFed(NEW_AMSTERDAM);
        ledger.verifyColonyFed(DELFT);
    }

    @Test
    @Tag("local")
    void TC_02_uninterrupted_reference_path_reaches_the_same_balances()
            throws Exception {
        ProductionNetworkFixture.prepare(getContext(), SOURCE, PREPARED);
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        final EducationPage education = new EducationPage(getContext());
        final ConstructionPage construction = new ConstructionPage(
                getContext());
        final ProductionNetworkPage ledger = new ProductionNetworkPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();

        // The same start as the checkpoint path, with the same player commands.
        ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        education.open();
        education.chooseTeacher(TEACHER);
        education.chooseStudent(STUDENT);
        education.assign();
        education.close();
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.confirm();
        construction.cancel();
        construction.verifyPickerClosed();
        game = colony.close();
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        route.open();
        route.enterName("Ore Line");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, NEW_AMSTERDAM_STOP);
        route.chooseStop(1, DELFT_STOP);
        route.create();
        cargo.open();
        cargo.chooseRoute("Ore Line");
        cargo.chooseStop(AT_NEW_AMSTERDAM);
        cargo.addUnload(GoodsType.ORE);
        cargo.save();
        cargo.chooseStop(AT_DELFT);
        cargo.addLoad(GoodsType.ORE);
        cargo.save();
        cargo.chooseCarrier(0);
        cargo.chooseFirstStop(AT_DELFT);
        cargo.assign();
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        cargo.close();
        game = colony.close();

        // Turns 1 and 2: the first half of cycle 1 moves and produces nothing.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyCourse(NEW_AMSTERDAM, TEACHER, STUDENT,
                UnitType.MASTER_CARPENTER, 2, 4);

        // The player restores the delivery without leaving the game.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_IN_WAREHOUSE);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 60));
        game = colony.close();

        // Turns 3 and 4: back in Delft the wagon loads the ore and cycle 1 is
        // complete; the student graduates.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHold(WAGON, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyNoCourse(NEW_AMSTERDAM);
        ledger.verifyUnitType(STUDENT, UnitType.MASTER_CARPENTER);
        ledger.verifyResident(NEW_AMSTERDAM, STUDENT);

        // Turns 5 and 6: halfway through cycle 2 the ore is unloaded once.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_DELFT, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 60);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 0);

        // Turns 7 to 9: the blacksmiths turn 6 ore into 6 tools a turn. At
        // turn 8 the wagon is back in Delft and cycle 2 is complete.
        game.nextTurnAndCloseDialogs();
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 54);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 6);
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON, DELFT_LOCATION);
        cargo.verifyHoldEmpty(WAGON, 0);
        cargo.verifyAssignment(WAGON, "Ore Line", AT_NEW_AMSTERDAM, false);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 48);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 12);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        ledger.verifyQueue(NEW_AMSTERDAM, "BLACKSMITHS_SHOP");
        game.nextTurnAndCloseDialogs();
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 42);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 18);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 64);

        // Turn 10: the same end state as after Save and Continue.
        game.nextTurnAndCloseDialogs();
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_SHOP,
                1);
        ledger.verifyBuilding(NEW_AMSTERDAM, ConstructionType.BLACKSMITHS_HOUSE,
                0);
        ledger.verifyQueue(NEW_AMSTERDAM);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.ORE, 36);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.TOOLS, 4);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.HAMMERS, 0);
        ledger.verifyStock(NEW_AMSTERDAM, GoodsType.MUSKET, 50);
        ledger.verifyStock(DELFT, GoodsType.MUSKET, 50);
        ledger.verifyStock(DELFT, GoodsType.ORE, 0);
        cargo.verifyHold(GALLEON, 1, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON, 3, Goods.of(GoodsType.SUGAR, 100));
        cargo.verifyHold(GALLEON, 4, Goods.of(GoodsType.TOBACCO, 100));
        cargo.verifyHold(GALLEON, 5, Goods.of(GoodsType.COTTON, 100));
        ledger.verifyResidents(NEW_AMSTERDAM, 5);
        ledger.verifyResidents(DELFT, 1);
        ledger.verifyColonyFed(NEW_AMSTERDAM);
        ledger.verifyColonyFed(DELFT);
    }
}
