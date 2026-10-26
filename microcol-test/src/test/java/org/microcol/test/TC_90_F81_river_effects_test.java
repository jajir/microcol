package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.RiverSize;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RiverPage;
import org.microcol.page.ResourcePage;
import org.microcol.page.TerrainProductionPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify the authored river course of Delft: small and large squares are drawn
 * with their own width, a connected step costs one third in either direction
 * and survives Continue, and the small river adds exactly one to the sugar of
 * the field that the job preview quotes and the next turn stores.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_90_F81_river_effects_test extends AbstractMicroColTest {

    private final static Location SOURCE_LOCATION = Location.of(22, 11);

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location SMALL_LOCATION = Location.of(23, 11);
    private final static Location LARGE_LOCATION = Location.of(24, 11);

    /** Delft: the colony with the small river square as its east field. */
    private final static int DELFT_ID = 1;

    /** Delft worker that worked the small river square before the fixture. */
    private final static int WORKER_ID = 22;

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_river_courses_are_drawn_and_connected_travel_survives_continue()
            throws Exception {
        // Load a copy that carries the authored river course.
        RiverFixture.prepareCourse(getContext(), testFileName,
                "river-f81-travel.microcol");
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final RiverPage river = new RiverPage(getContext());

        // The small and large courses show their own widths on the real map.
        river.verifyRiverWidth(SMALL_LOCATION, RiverSize.SMALL, SOURCE_LOCATION,
                LARGE_LOCATION);
        river.verifyRiverWidth(LARGE_LOCATION, RiverSize.LARGE, SMALL_LOCATION);
        river.verifyCurvedCourse(SMALL_LOCATION);

        // The third colonist steps from the colony onto the small river: one
        // third is spent, so two remain.
        gamePage.selectTile(SOURCE_LOCATION);
        gamePage.getRightPanelUnit(0).selectUnit();
        gamePage.moveMouseAtLocation(SOURCE_LOCATION);
        gamePage.dragMouseAtLocation(SMALL_LOCATION);
        river.captureUnit(SMALL_LOCATION);
        river.verifyCapturedUnitAt(SMALL_LOCATION);
        river.verifyUnitMovement(2);
        // River founding and plowing explain rejection without spending tools
        // or movement.
        river.useControl("plowField");
        gamePage.verifyThatStatusBarContains("River squares cannot be plowed");
        river.verifyRejectedWork(SMALL_LOCATION, 100, 2);
        river.useControl("buildColony");
        gamePage.verifyThatStatusBarContains(
                "Found the colony beside the river");
        river.verifyRejectedWork(SMALL_LOCATION, 100, 2);
        gamePage.selectTile(SMALL_LOCATION);
        river.verifyMoveControlVisible(true);

        // The next step along the course to the large river is again a third.
        gamePage.moveMouseAtLocation(SMALL_LOCATION);
        gamePage.dragMouseAtLocation(LARGE_LOCATION);
        river.verifyCapturedUnitAt(LARGE_LOCATION);
        river.verifyUnitMovement(1);
        river.verifyMoveControlVisible(true);

        // The step back along the same course spends the last third.
        gamePage.moveMouseAtLocation(LARGE_LOCATION);
        gamePage.dragMouseAtLocation(SMALL_LOCATION);
        river.verifyCapturedUnitAt(SMALL_LOCATION);
        river.verifyUnitMovement(0);

        // Continue keeps the same pioneer with no movement left, and the empty
        // large square still shows its river.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        river.verifyCapturedUnitAt(SMALL_LOCATION);
        river.verifyUnitMovement(0);
        river.verifyRiverWidth(LARGE_LOCATION, RiverSize.LARGE, SMALL_LOCATION);

        // The next owner turn restores the full three thirds.
        gamePage.nextTurnAndCloseDialogs();
        river.verifyUnitMovement(3);
    }

    @Test
    @Tag("local")
    void TC_02_river_fishing_uses_fisherman_expertise_without_docks()
            throws Exception {
        // Load a copy where the Delft worker has left the small river square.
        RiverFixture.prepareFieldWorker(getContext(), testFileName,
                "river-f81-field.microcol", WORKER_ID);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final RiverPage river = new RiverPage(getContext());

        // Fishing replaces crops. An expert fisherman gets six before the Tory
        // penalty.
        river.verifyOutput(SMALL_LOCATION, GoodsType.CORN, 3);
        river.verifyOutput(LARGE_LOCATION, GoodsType.CORN, 4);
        river.verifyOutput(SMALL_LOCATION, GoodsType.SUGAR, 0);
        river.verifyOutput(LARGE_LOCATION, GoodsType.FUR, 0);
        river.verifyDockRequired(DELFT_ID, SMALL_LOCATION, false);
        final ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION,
                "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.CORN);
        jobs.verifyDetails("Field coordinates: 23, 11", true);
        jobs.verifyDetails("Best eligible field: 5 Corn per turn", true);
        jobs.cancel();
        jobs.verifyClosed();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.CORN, 50));
        // Confirm fishing and compare the colony's displayed net output with
        // the next stock.
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.CORN);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID, SMALL_LOCATION,
                Goods.of(GoodsType.CORN, 5));
        final int expectedFood = 50 + colony.getNetProduction(GoodsType.CORN);
        colony.close();
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.CORN, expectedFood));
        // Continue preserves fishing and does not grant food again.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.CORN, expectedFood));
        production.verifyFieldJob(DELFT_ID, WORKER_ID, SMALL_LOCATION,
                Goods.of(GoodsType.CORN, 5));
    }

    @Test
    @Tag("local")
    void TC_03_known_square_draws_no_river_toward_an_unexplored_neighbour()
            throws Exception {
        RiverFixture.prepareCourse(getContext(), testFileName,
                "river-f81-hidden.microcol");
        WelcomePage.of(getContext()).loadGame();
        final FogPage fog = new FogPage(getContext());
        final RiverPage river = new RiverPage(getContext());

        // Keep only the sight of the pioneer on the colony square, so the small
        // square is known and the large square beyond it is not.
        river.captureUnit(SOURCE_LOCATION);
        fog.forgetExploration();
        river.observeCapturedUnit();
        fog.verifyExplored(SMALL_LOCATION, true);
        fog.verifyExplored(LARGE_LOCATION, false);

        // The known small square still joins the known colony square, but draws
        // nothing toward the unexplored large square.
        river.verifyRiverWidth(SMALL_LOCATION, RiverSize.SMALL,
                SOURCE_LOCATION);
        river.verifyNoRiverToward(SMALL_LOCATION, LARGE_LOCATION);
    }

    @Test
    @Tag("local")
    void TC_04_river_deposits_and_bridge_preserve_production_and_reserves()
            throws Exception {
        final String kind = "SILVER";
        final org.microcol.model.ResourceOverlay deposit = org.microcol.model.ResourceOverlay
                .valueOf(kind);
        final GoodsType goods = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? GoodsType.SILVER
                : GoodsType.ORE;
        final int output = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? 3
                : 1;
        final int reserve = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? 100
                : 200;
        RiverFixture.prepareDeposit(getContext(), testFileName,
                "river-f81-deposit.microcol", deposit);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RiverPage river = new RiverPage(getContext());
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        // The deposit and bridge coexist; no forest or plowed field covers the
        // river.
        river.verifyBridge(SMALL_LOCATION);
        new ResourcePage(getContext()).verifyMarkerPainted(SMALL_LOCATION,
                deposit);
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve);
        final ColonyScreen colony = game.openColonyAt(COLONY_LOCATION, "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, goods);
        jobs.verifyDetails("Field coordinates: 23, 11", true);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID, SMALL_LOCATION,
                Goods.of(goods, output));
        colony.close();
        // Only committed production consumes the finite reserve, once per turn.
        game.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(goods, output));
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve - output);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(goods, output));
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve - output);
        new ResourcePage(getContext()).verifyMarkerPainted(SMALL_LOCATION,
                deposit);
    }

    @Test
    @Tag("local")
    void TC_05_river_ore_and_bridge_preserve_production_and_reserves()
            throws Exception {
        final String kind = "ORE";
        final org.microcol.model.ResourceOverlay deposit = org.microcol.model.ResourceOverlay
                .valueOf(kind);
        final GoodsType goods = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? GoodsType.SILVER
                : GoodsType.ORE;
        final int output = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? 3
                : 1;
        final int reserve = deposit == org.microcol.model.ResourceOverlay.SILVER
                ? 100
                : 200;
        RiverFixture.prepareDeposit(getContext(), testFileName,
                "river-f81-deposit.microcol", deposit);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RiverPage river = new RiverPage(getContext());
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        // The deposit and bridge coexist; no forest or plowed field covers the
        // river.
        river.verifyBridge(SMALL_LOCATION);
        new ResourcePage(getContext()).verifyMarkerPainted(SMALL_LOCATION,
                deposit);
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve);
        final ColonyScreen colony = game.openColonyAt(COLONY_LOCATION, "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, goods);
        jobs.verifyDetails("Field coordinates: 23, 11", true);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID, SMALL_LOCATION,
                Goods.of(goods, output));
        colony.close();
        // Only committed production consumes the finite reserve, once per turn.
        game.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(goods, output));
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve - output);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(goods, output));
        river.verifyRemainingDeposit(SMALL_LOCATION, goods, reserve - output);
        new ResourcePage(getContext()).verifyMarkerPainted(SMALL_LOCATION,
                deposit);
    }

}
