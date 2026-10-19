package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.TerrainProductionPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that a forested grassland square offers its aid-card lumber in the job
 * preview, that the next normal turn stores exactly the quoted lumber, and that
 * Save and Continue add no lumber on load.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_86_F77_terrain_production_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location FOREST_LOCATION = Location.of(23, 12);

    /** Delft: the colony next to the forest square, with ID 1. */
    private final static int DELFT_ID = 1;

    /** Free colonist that worked the square before the fixture frees it. */
    private final static int WORKER_ID = 22;

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_forested_grassland_preview_matches_the_next_turn_and_continue()
            throws Exception {
        // Plant a forest on Delft's east grassland square and stand its former
        // worker in the colony centre, so the square is free to be chosen.
        TerrainProductionFixture.prepare(getContext(), testFileName,
                "terrain-f77-lumber.microcol", COLONY_LOCATION, FOREST_LOCATION,
                WORKER_ID);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());

        // The forested grassland square has the aid-card baseline.
        production.verifyBaseline(FOREST_LOCATION, 3, 2, 6);

        // Open Delft and cancel the lumber preview; nothing is assigned.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION, "Delft");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails("Field coordinates: 23, 12", true);
        // Baseline 6 minus the Tory penalty of -1 that applies to every job of
        // Delft at zero liberty is the effective 5 shown to the player.
        jobs.verifyDetails("Best eligible field: 5 Lumber per turn", true);
        jobs.cancel();
        jobs.verifyClosed();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.LUMBER, 0));

        // Confirm the same job; the field produces the forested lumber.
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.confirm();
        jobs.verifyClosed();
        production.verifyFieldJob(DELFT_ID, WORKER_ID, FOREST_LOCATION,
                Goods.of(GoodsType.LUMBER, 5));

        // The next normal turn stores exactly the quoted lumber.
        gamePage = colony.close();
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.LUMBER, 5));

        // Save and Continue add no lumber on load and keep the baseline.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.LUMBER, 5));
        production.verifyBaseline(FOREST_LOCATION, 3, 2, 6);
    }

}
