package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.ResourceOverlay;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ResourcePage;
import org.microcol.page.TerrainProductionPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify the authored special resources of Delft: a prime sugar land is drawn
 * with its marker and named when a known square is inspected, an unexplored
 * square reveals nothing, and the sugar the job preview quotes is the sugar the
 * next turn stores, unchanged through Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_91_F82_resource_overlays_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);

    /** Delft: the colony with the prime sugar land as its east field. */
    private final static int DELFT_ID = 1;

    /** Delft worker that worked the sugar square before the fixture. */
    private final static int WORKER_ID = 22;

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_known_sugar_land_shows_its_marker_name_and_matching_output()
            throws Exception {
        // Load a copy that carries the authored resources.
        ResourceFixture.prepare(getContext(), testFileName,
                "resource-f82-known.microcol", WORKER_ID);
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final ResourcePage resource = new ResourcePage(getContext());

        // The known sugar land shows its own marker on the real map.
        resource.verifyMarkerPainted(ResourceFixture.SUGAR_LOCATION,
                ResourceOverlay.PRIME_SUGAR);

        // Inspecting the square names the resource in the right panel.
        gamePage.selectTile(ResourceFixture.SUGAR_LOCATION);
        resource.verifyInspectedName("Prime sugar cane land");
        gamePage.captureDialog("tileName", "r0274-resource-map.png");

        // Only the matching output gains the bonus: grassland sugar 1 becomes
        // 3, while corn 4, tobacco 3 and fur 0 are unchanged.
        resource.verifyResource(ResourceFixture.SUGAR_LOCATION,
                ResourceOverlay.PRIME_SUGAR);
        resource.verifyOutput(ResourceFixture.SUGAR_LOCATION, GoodsType.SUGAR,
                3);
        resource.verifyOutput(ResourceFixture.SUGAR_LOCATION, GoodsType.CORN,
                4);
        resource.verifyOutput(ResourceFixture.SUGAR_LOCATION, GoodsType.TOBACCO,
                3);
        resource.verifyOutput(ResourceFixture.SUGAR_LOCATION, GoodsType.FUR, 0);
    }

    @Test
    @Tag("local")
    void TC_02_unexplored_game_area_shows_no_marker_and_no_name()
            throws Exception {
        ResourceFixture.prepare(getContext(), testFileName,
                "resource-f82-hidden.microcol", WORKER_ID);
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final FogPage fog = new FogPage(getContext());
        final ResourcePage resource = new ResourcePage(getContext());

        // Keep only the sight of the worker in the colony centre, so the sugar
        // square is known and the game area two squares east is not.
        fog.forgetExploration();
        fog.observeFromUnit(WORKER_ID);
        fog.verifyExplored(ResourceFixture.SUGAR_LOCATION, true);
        fog.verifyExplored(ResourceFixture.GAME_LOCATION, false);

        // The known sugar land keeps its marker; the unexplored square paints
        // fog only, with no marker at its position.
        resource.verifyMarkerPainted(ResourceFixture.SUGAR_LOCATION,
                ResourceOverlay.PRIME_SUGAR);
        resource.verifyNoMarkerOnHiddenSquare(ResourceFixture.GAME_LOCATION);

        // Inspecting the unexplored square does not name its game area.
        gamePage.selectTile(ResourceFixture.GAME_LOCATION);
        resource.verifyInspectionOmits("Game area");
    }

    @Test
    @Tag("local")
    void TC_03_prime_sugar_preview_matches_next_turn_and_survives_continue()
            throws Exception {
        ResourceFixture.prepare(getContext(), testFileName,
                "resource-f82-field.microcol", WORKER_ID);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final ResourcePage resource = new ResourcePage(getContext());

        // Nothing is stored before the turn.
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 0));

        // Open Delft and preview the sugar job of the freed worker; cancel it.
        // The prime sugar 3 minus the Tory penalty of -1 at zero liberty is 2.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION, "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SUGAR);
        jobs.verifyDetails("Field coordinates: 23, 12", true);
        jobs.verifyDetails("Best eligible field: 2 Sugar cane per turn", true);
        jobs.cancel();
        jobs.verifyClosed();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 0));

        // Confirm the same job; the field is worked, but nothing is stored yet.
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SUGAR);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                ResourceFixture.SUGAR_LOCATION, Goods.of(GoodsType.SUGAR, 2));
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 0));

        // The next normal turn stores exactly the previewed sugar.
        gamePage = colony.close();
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 2));

        // Save and Continue keep the identity and output, and add no sugar on
        // load.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.SUGAR, 2));
        resource.verifyResource(ResourceFixture.SUGAR_LOCATION,
                ResourceOverlay.PRIME_SUGAR);
        resource.verifyOutput(ResourceFixture.SUGAR_LOCATION, GoodsType.SUGAR,
                3);
        resource.verifyMarkerPainted(ResourceFixture.SUGAR_LOCATION,
                ResourceOverlay.PRIME_SUGAR);
    }
}
