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
import org.microcol.page.ResourcePage;
import org.microcol.page.TerrainProductionPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Finite mineral deposits on Delft's east field, prepared near their boundary
 * before loading. Real mining turns take the bonus only as far as the reserve
 * allows: a pure silver deposit and a pure ore deposit run out, and a mixed
 * deposit drains its silver while its ore reserve stays intact. Previews, the
 * stored goods and the remaining units agree, and Save/Continue keeps them.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_102_F93_mineral_depletion_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);

    private final static String COLONY_NAME = "Delft";

    /** Delft: the colony whose east field holds the deposit. */
    private final static int DELFT_ID = 1;

    /** Delft worker that stood on the east field before the fixture. */
    private final static int WORKER_ID = 22;

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_pure_silver_deposit_gives_its_last_partial_units_then_stops()
            throws Exception {
        // A mountain with 3 silver units: two turns take 2 and then 1.
        MineralFixture.prepare(getContext(), testFileName,
                "mineral-f93-silver.microcol", WORKER_ID, "m", "si", 3, 0);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final ResourcePage resource = new ResourcePage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // Nothing is stored and the deposit is still full before the turn.
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 0));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 3);

        // The silver preview quotes the two units the deposit can still give.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION,
                COLONY_NAME);
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SILVER);
        jobs.verifyDetails("Best eligible field: 2 ", true);
        jobs.confirm();
        jobs.verifyClosed();
        gamePage = colony.close();

        // The first turn stores the previewed 2 and leaves one unit.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 2));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 1);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.SILVER, 1));

        // The second turn takes the last unit; the deposit is exhausted.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 3));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 0);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.SILVER, 0));

        // Save and Continue keep the empty deposit and add no silver on load.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 3));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 0);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.SILVER, 0));
    }

    @Test
    @Tag("local")
    void TC_02_pure_ore_deposit_gives_its_last_partial_unit_then_stops()
            throws Exception {
        // A hill with 3 ore units: two turns take 2 and then 1.
        MineralFixture.prepare(getContext(), testFileName,
                "mineral-f93-ore.microcol", WORKER_ID, "h", "or", 0, 3);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final ResourcePage resource = new ResourcePage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 3);

        // The ore preview quotes the baseline plus the two units left.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION,
                COLONY_NAME);
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.ORE);
        jobs.verifyDetails("Best eligible field: 5 ", true);
        jobs.confirm();
        jobs.verifyClosed();
        gamePage = colony.close();

        // The first turn stores 5 and leaves one unit in the deposit.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 5));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 1);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.ORE, 4));

        // Save and Continue keep the one remaining unit and its preview.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 5));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 1);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.ORE, 4));

        // The last unit comes on the next turn, then only the baseline is left.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 9));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 0);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.ORE, 3));
    }

    @Test
    @Tag("local")
    void TC_03_mixed_deposit_drains_silver_while_its_ore_reserve_stays_whole()
            throws Exception {
        // A mountain with 1 silver and 3 ore units: silver runs out first.
        MineralFixture.prepare(getContext(), testFileName,
                "mineral-f93-mixed.microcol", WORKER_ID, "m", "mm", 1, 3);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final TerrainProductionPage production = new TerrainProductionPage(
                getContext());
        final ResourcePage resource = new ResourcePage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // The silver preview quotes the one unit that is left.
        ColonyScreen colony = gamePage.openColonyAt(COLONY_LOCATION,
                COLONY_NAME);
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.SILVER);
        jobs.verifyDetails("Best eligible field: 1 ", true);
        jobs.confirm();
        jobs.verifyClosed();
        gamePage = colony.close();

        // The silver turn takes its last unit and leaves the ore untouched.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 1));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 0);
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 3);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.SILVER, 0));

        // Switch the same worker to ore: its full ore reserve is still there.
        colony = gamePage.openColonyAt(COLONY_LOCATION, COLONY_NAME);
        jobs.openFromField(COLONY_NAME, WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.ORE);
        jobs.verifyDetails("Best eligible field: 4 ", true);
        jobs.confirm();
        jobs.verifyClosed();
        gamePage = colony.close();

        // The ore turn stores 4 and leaves one ore unit.
        gamePage.nextTurnAndCloseDialogs();
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 4));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 1);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.ORE, 3));

        // Save and Continue keep the empty silver reserve beside the ore left.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouseGoods(DELFT_ID,
                Goods.of(GoodsType.SILVER, 1));
        production.verifyWarehouseGoods(DELFT_ID, Goods.of(GoodsType.ORE, 4));
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.SILVER, 0);
        resource.verifyMineralRemaining(MineralFixture.DEPOSIT_LOCATION,
                GoodsType.ORE, 1);
        production.verifyFieldJob(DELFT_ID, WORKER_ID,
                MineralFixture.DEPOSIT_LOCATION, Goods.of(GoodsType.ORE, 3));
    }
}
