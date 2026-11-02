package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.ConstructionType;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.ColonyScreen;
import org.akropia.page.ColonyJobPage;
import org.akropia.page.GameBarWidthPage;
import org.akropia.page.GamePage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.IndustrialProductionPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Visible signed stock changes agree with worker actions and actual turns. */
@ExtendWith(ApplicationExtension.class)
public class TC_197_F229_warehouse_forecast_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F229-warehouse-forecast.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_worker_consumes_grapes_until_empty_and_shows_signed_changes()
            throws Exception {
        // Three residents eat six food; the unattended center provides four.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        final IndustrialProductionPage industry = new IndustrialProductionPage(
                getContext());
        industry.verifyForecast(GoodsType.GRAIN, 50, -2);
        industry.verifyForecast(GoodsType.GRAPES, 4, 0);
        industry.verifyForecast(GoodsType.WINE, 0, 0);

        // Assign a fourth resident: refresh red deficits and green wine now.
        industry.assignWorker(4, "id_winery");
        new ColonyJobPage(getContext()).verifyIndoorJob(4,
                ConstructionType.SMALL_WINERY, 1);
        industry.verifyForecast(GoodsType.GRAIN, 50, -4);
        industry.verifyForecast(GoodsType.GRAPES, 4, -3);
        industry.verifyForecast(GoodsType.WINE, 0, 3);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0359-grapes-consumption.png");
        colony.close();

        // One real turn consumes three leaves; the last leaf limits the next.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        industry.verifyForecast(GoodsType.GRAIN, 46, -4);
        industry.verifyForecast(GoodsType.GRAPES, 1, -1);
        industry.verifyForecast(GoodsType.WINE, 3, 1);
        colony.close();

        // The next turn uses only that leaf and clears both production deltas.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        new GameBarWidthPage(getContext()).resizeContent(1000, 925);
        industry.verifyForecast(GoodsType.GRAIN, 42, -4);
        industry.verifyForecast(GoodsType.GRAPES, 0, 0);
        industry.verifyForecast(GoodsType.WINE, 4, 0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0359-grapes-empty.png");
        colony.close();

        // With the worker still assigned, an empty input produces no wine.
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        industry.verifyForecast(GoodsType.GRAIN, 38, -4);
        industry.verifyForecast(GoodsType.GRAPES, 0, 0);
        industry.verifyForecast(GoodsType.WINE, 4, 0);
        new ColonyJobPage(getContext()).verifyIndoorJob(4,
                ConstructionType.SMALL_WINERY, 1);
    }
}
