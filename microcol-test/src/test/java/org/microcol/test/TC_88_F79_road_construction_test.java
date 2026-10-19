package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RoadPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that a pioneer builds a road on a bare square without a question, that
 * the unit panel shows the owner updates left, that the work takes two owner
 * updates (one for a hardy pioneer), spends 20 tools once and records the road,
 * that cancelling leaves no road and no charge, and that a pioneer with 19
 * tools is told why it cannot start.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_88_F79_road_construction_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location ROAD_LOCATION = Location.of(23, 12);

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    /** Move the first colonist from the colony onto the bare square. */
    private GamePage moveToRoadSquare(final GamePage gamePage)
            throws Exception {
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(ROAD_LOCATION);
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(ROAD_LOCATION);
        return gamePage;
    }

    /** Equip a scoped copy and move an ordinary colonist onto the square. */
    private GamePage prepareAndMove(final String name, final int tools)
            throws Exception {
        RoadFixture.prepare(getContext(), testFileName, name, tools,
                ROAD_LOCATION);
        return moveToRoadSquare(WelcomePage.of(getContext()).loadGame());
    }

    /** Same as above, with every colonist saved as a hardy pioneer. */
    private GamePage prepareHardyAndMove(final String name) throws Exception {
        RoadFixture.prepareHardy(getContext(), testFileName, name, 100,
                ROAD_LOCATION);
        return moveToRoadSquare(WelcomePage.of(getContext()).loadGame());
    }

    @Test
    @Tag("local")
    void TC_01_road_completes_after_two_updates_and_survives_continue()
            throws Exception {
        // Load a prepared copy and bring a tooled colonist to the bare square.
        GamePage gamePage = prepareAndMove("road-f79-ordinary.microcol", 100);
        final RoadPage road = new RoadPage(getContext());
        road.captureUnit(ROAD_LOCATION);
        road.verifyControlVisible(true);
        road.verifyRoadAt(ROAD_LOCATION, false);

        // Starting spends the movement at once; no question is asked first.
        road.useControl();
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.buildRoad, 0, 100);
        road.verifyRemainingUpdates(2);
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRoadProgressShown(2);
        road.verifyRoadAt(ROAD_LOCATION, false);
        road.verifyControlVisible(false);
        road.verifyCancelControlVisible(true);

        // One update leaves one; Continue keeps the countdown and the panel.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRemainingUpdates(1);
        road.verifyRoadProgressShown(1);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRemainingUpdates(1);
        road.verifyRoadProgressShown(1);
        road.verifyRoadAt(ROAD_LOCATION, false);

        // The second update records the road and charges 20 tools once.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRoadAt(ROAD_LOCATION, true);
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 0, 80);
        road.verifyRoadProgressHidden();
        road.verifyCancelControlVisible(false);

        // Later updates and Continue neither charge nor build again.
        gamePage.nextTurnAndCloseDialogs();
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 3, 80);
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        road.verifyRoadAt(ROAD_LOCATION, true);
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 3, 80);

        // Once the pioneer steps away, the built road is painted on the map.
        gamePage.selectTile(ROAD_LOCATION);
        final Location emptyGrassland = Location.of(24, 12);
        gamePage.dragMouseAtLocation(emptyGrassland);
        road.verifyCapturedUnitAt(emptyGrassland);
        road.verifyRoadDrawn(ROAD_LOCATION);
    }

    @Test
    @Tag("local")
    void TC_02_cancel_discards_progress_without_road_or_refund()
            throws Exception {
        // Load a prepared copy and start the work.
        final GamePage gamePage = prepareAndMove("road-f79-cancel.microcol",
                100);
        final RoadPage road = new RoadPage(getContext());
        road.captureUnit(ROAD_LOCATION);
        road.useControl();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRemainingUpdates(1);
        road.verifyRoadProgressShown(1);

        // Cancel: no refund of movement or tools and no road.
        road.cancelWork();
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 0, 100);
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRoadProgressHidden();
        road.verifyCancelControlVisible(false);
        road.verifyRoadAt(ROAD_LOCATION, false);

        // Waiting beyond the original completion changes nothing.
        for (int update = 0; update < 3; update++) {
            gamePage.nextTurnAndCloseDialogs();
        }
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 3, 100);
        road.verifyRoadAt(ROAD_LOCATION, false);
    }

    @Test
    @Tag("local")
    void TC_03_too_few_tools_are_explained_and_start_nothing()
            throws Exception {
        // A pioneer holding 19 tools keeps the control to explain the reason.
        final GamePage gamePage = prepareAndMove("road-f79-tools.microcol", 19);
        final RoadPage road = new RoadPage(getContext());
        road.captureUnit(ROAD_LOCATION);
        road.verifyControlVisible(true);
        road.useControl();
        gamePage.verifyThatStatusBarContains(
                "Building a road needs a pioneer with at least 20 tools.");
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 3, 19);
        road.verifyRoadAt(ROAD_LOCATION, false);
        road.verifyCancelControlVisible(false);
    }

    @Test
    @Tag("local")
    void TC_04_hardy_pioneer_completes_after_one_update() throws Exception {
        final GamePage gamePage = prepareHardyAndMove(
                "road-f79-hardy.microcol");
        final RoadPage road = new RoadPage(getContext());
        road.captureUnit(ROAD_LOCATION);
        road.useControl();
        road.verifyRemainingUpdates(1);
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRoadProgressShown(1);

        // The single owner update records the road and charges 20 tools.
        gamePage.nextTurnAndCloseDialogs();
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyRoadAt(ROAD_LOCATION, true);
        road.verifyUnitWork(ROAD_LOCATION, UnitActionType.noAction, 0, 80);
        road.verifyRoadProgressHidden();
    }
}
