package org.akropia.test;

import java.io.File;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** R-0405: local fishing is managed through its receiving colony. */
@ExtendWith(ApplicationExtension.class)
public class TC_202_F234_local_fishing_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F234-local-fishing.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_map_fisherman_opens_its_colony_and_cancelled_boat_can_return()
            throws Exception {
        // Double-clicking the boat opens its home city and selects this
        // fisherman.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        work.verifyFishing(0, 0, true);
        // Water shows fish as the source of Food, without changing the job.
        game.selectTile(Location.of(23, 16));
        new ResourcePage(getContext()).openTerrainDetails();
        work.verifyFishingYieldPresentation("Food", 3,
                "Fishing: provides 3 food per turn.");
        work.hoverFishingYield();
        game.verifyThatStatusBarContains("Fishing: provides 3 food per turn.");
        game.captureDialog("rightPanel", "food-fishing-map.png");
        final ColonyScreen colony = game.openColonyAt(Location.of(23, 16),
                "New Amsterdam");
        work.verifyFoodStockPresentation("Food");
        work.hoverFoodStock();
        colony.verifyThatStatusBarContains("Food from farming and fishing.");
        colony.showSurroundingArea();
        work.verifySelectedFieldWorker();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0405-fisherman-selected.png");

        // Canceling fishing returns home and consumes one point.
        colony.openCropMenu(0);
        work.choose("cancelColonyWork");
        work.verifyReturned(0, Location.of(24, 16));
        work.verifyFood(0, 2);
    }

    @Test
    @Tag("local")
    void TC_02_map_selection_preserves_fishing_and_context_returns_home() {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location water = Location.of(23, 16);

        // Selecting the fisherman never opens the city or releases its job.
        game.selectTile(water);
        work.verifyMapSelection(0, water, "w", 0, 3);
        work.verifyRecipientInPanel(0, "Works for New Amsterdam");
        work.pressMovementShortcut();
        game.hoverTile(water);
        game.dragToLocation(Location.of(24, 16));
        work.verifyMapSelection(0, water, "w", 0, 3);

        // The explicit map action returns the worker and pays one point.
        game.openUnitContextAt(water);
        work.verifyMenuAction("cancelColonyWork", "Cancel work", false);
        work.choose("cancelColonyWork");
        work.verifyReturned(0, Location.of(24, 16));
        work.verifyFood(0, 2);
    }

    @Test
    @Tag("local")
    void TC_03_no_movement_keeps_fishing_and_hides_cancellation() {
        LocalFishingFixture.exhaustFisherman(getContext());
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        final Location water = Location.of(23, 16);

        // An unaffordable return leaves the job and the fisherman's position.
        game.selectTile(water);
        work.verifyMapSelection(0, water, "w", 0, 0);
        game.openUnitContextAt(water);
        work.verifyMenuAction("cancelColonyWork", "Cancel work", true);
        work.verifyMenuAction("returnToColony", "Return to colony", true);
        work.closeContext();
        work.verifyWorker(0, water, "w", 0, 0);
        work.verifyFood(0, 4);
    }

    @Test
    @Tag("local")
    void TC_04_czech_fishing_and_shared_food_stock() throws Exception {
        // The same water yield and common stock use Czech food terminology.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final WorldWorkersPage work = new WorldWorkersPage(getContext());
        game.selectTile(Location.of(23, 16));
        new ResourcePage(getContext()).openTerrainDetails();
        work.verifyFishingYieldPresentation("Jídlo", 3,
                "Rybolov: jídlo za tah — 3.");
        work.hoverFishingYield();
        game.verifyThatStatusBarContains("Rybolov:");
        final ColonyScreen colony = game.openColonyAt(Location.of(23, 16),
                "New Amsterdam");
        work.verifyFoodStockPresentation("Jídlo");
        work.hoverFoodStock();
        colony.verifyThatStatusBarContains("Jídlo ze zemědělství a rybolovu.");
        colony.showSurroundingArea();
        work.verifySelectedFieldWorker();
        work.verifyFishing(0, 0, true);
        new InGameLayoutPage(getContext())
                .saveEvidence("food-fishing-czech-colony.png");
    }

}
