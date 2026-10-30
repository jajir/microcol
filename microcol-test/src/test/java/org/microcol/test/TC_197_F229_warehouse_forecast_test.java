package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.GameBarWidthPage;
import org.microcol.page.GamePage;
import org.microcol.page.InGameLayoutPage;
import org.microcol.page.IndustrialProductionPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Visible signed stock changes agree with worker actions and actual turns. */
@ExtendWith(ApplicationExtension.class)
public class TC_197_F229_warehouse_forecast_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F229-warehouse-forecast.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_worker_consumes_tobacco_until_empty_and_shows_signed_changes()
            throws Exception {
        // Three residents eat six food; the unattended center provides four.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        final IndustrialProductionPage industry = new IndustrialProductionPage(
                getContext());
        industry.verifyForecast(GoodsType.CORN, 50, -2);
        industry.verifyForecast(GoodsType.TOBACCO, 4, 0);
        industry.verifyForecast(GoodsType.CIGARS, 0, 0);

        // Assign a fourth resident: refresh red deficits and green cigars now.
        industry.assignWorker(4, "id_tobacco");
        new ColonyJobPage(getContext()).verifyIndoorJob(4,
                ConstructionType.TOBACCONISTS_HOUSE, 1);
        industry.verifyForecast(GoodsType.CORN, 50, -4);
        industry.verifyForecast(GoodsType.TOBACCO, 4, -3);
        industry.verifyForecast(GoodsType.CIGARS, 0, 3);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0359-tobacco-consumption.png");
        colony.close();

        // One real turn consumes three leaves; the last leaf limits the next.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        industry.verifyForecast(GoodsType.CORN, 46, -4);
        industry.verifyForecast(GoodsType.TOBACCO, 1, -1);
        industry.verifyForecast(GoodsType.CIGARS, 3, 1);
        colony.close();

        // The next turn uses only that leaf and clears both production deltas.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        new GameBarWidthPage(getContext()).resizeContent(1000, 925);
        industry.verifyForecast(GoodsType.CORN, 42, -4);
        industry.verifyForecast(GoodsType.TOBACCO, 0, 0);
        industry.verifyForecast(GoodsType.CIGARS, 4, 0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0359-tobacco-empty.png");
        colony.close();

        // With the worker still assigned, an empty input produces no cigars.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        industry.verifyForecast(GoodsType.CORN, 38, -4);
        industry.verifyForecast(GoodsType.TOBACCO, 0, 0);
        industry.verifyForecast(GoodsType.CIGARS, 4, 0);
        new ColonyJobPage(getContext()).verifyIndoorJob(4,
                ConstructionType.TOBACCONISTS_HOUSE, 1);
    }
}
