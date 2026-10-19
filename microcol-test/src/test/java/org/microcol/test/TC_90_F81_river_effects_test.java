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

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location SMALL_LOCATION = Location.of(23, 12);
    private final static Location LARGE_LOCATION = Location.of(24, 12);

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
        river.verifyRiverWidth(SMALL_LOCATION, RiverSize.SMALL, COLONY_LOCATION,
                LARGE_LOCATION);
        river.verifyRiverWidth(LARGE_LOCATION, RiverSize.LARGE, SMALL_LOCATION);

        // The third colonist steps from the colony onto the small river: one
        // third is spent, so two remain.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(SMALL_LOCATION);
        river.captureUnit(SMALL_LOCATION);
        river.verifyCapturedUnitAt(SMALL_LOCATION);
        river.verifyUnitMovement(2);
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
    void TC_02_small_river_adds_one_to_the_previewed_sugar_and_the_next_turn()
            throws Exception {
        // Load a copy where the Delft worker has left the small river square.
        RiverFixture.prepareFieldWorker(getContext(), testFileName,
                "river-f81-field.microcol", WORKER_ID);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final RiverPage river = new RiverPage(getContext());

        // Grassland sugar is 1; the small river adds 1 and the large river 2.
        // Without an output a river adds nothing.
        river.verifyOutput(SMALL_LOCATION, GoodsType.SUGAR, 2);
        river.verifyOutput(LARGE_LOCATION, GoodsType.SUGAR, 3);
        river.verifyOutput(LARGE_LOCATION, GoodsType.FUR, 0);

        // Open Delft and preview the sugar job of the freed worker; cancel it.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION, "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SUGAR);
        jobs.verifyDetails("Field coordinates: 23, 12", true);
        // River 2 minus the Tory penalty of -1 at zero liberty is 1 sugar.
        jobs.verifyDetails("Best eligible field: 1 Sugar cane per turn", true);
        jobs.cancel();
        jobs.verifyClosed();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 0));

        // Confirm the same job; the field produces the previewed sugar.
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SUGAR);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID, SMALL_LOCATION,
                Goods.of(GoodsType.SUGAR, 1));

        // The next normal turn stores exactly the previewed sugar.
        gamePage = colony.close();
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 1));

        // Save and Continue add no sugar on load and keep the river output.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 1));
        river.verifyOutput(SMALL_LOCATION, GoodsType.SUGAR, 2);
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
        river.captureUnit(COLONY_LOCATION);
        fog.forgetExploration();
        river.observeCapturedUnit();
        fog.verifyExplored(SMALL_LOCATION, true);
        fog.verifyExplored(LARGE_LOCATION, false);

        // The known small square still joins the known colony square, but draws
        // nothing toward the unexplored large square.
        river.verifyRiverWidth(SMALL_LOCATION, RiverSize.SMALL,
                COLONY_LOCATION);
        river.verifyNoRiverToward(SMALL_LOCATION, LARGE_LOCATION);
    }

}
