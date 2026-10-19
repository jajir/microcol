package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RoadPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that a step between two road squares costs one third of a point in
 * either direction for the same pioneer, that its exact remaining thirds are
 * shown in the model and kept through Continue, and that the final third still
 * offers movement.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_89_F80_road_travel_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);
    private final static Location ROAD_LOCATION = Location.of(23, 12);

    private final static File testFileName = new File(
            "src/test/scenarios/F01-embark-disembark.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_road_steps_cost_one_third_both_ways_and_survive_continue()
            throws Exception {
        // Load a copy where the colony square and its neighbour are roads.
        RoadFixture.prepareWithRoads(getContext(), testFileName,
                "road-f80-travel.microcol", 100, COLONY_LOCATION,
                ROAD_LOCATION);
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final RoadPage road = new RoadPage(getContext());

        // The first colonist steps onto the road and is kept as the test unit.
        gamePage.getRightPanelUnit(2).selectUnit();
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        gamePage.dragMouseAtLocation(ROAD_LOCATION);
        road.captureUnit(ROAD_LOCATION);
        road.verifyCapturedUnitAt(ROAD_LOCATION);
        road.verifyUnitMovement(2);
        gamePage.selectTile(ROAD_LOCATION);
        road.verifyMoveControlVisible(true);

        // The step back along the road spends one more third, leaving one.
        gamePage.moveMouseAtLocation(ROAD_LOCATION);
        gamePage.dragMouseAtLocation(COLONY_LOCATION);
        road.verifyCapturedUnitAt(COLONY_LOCATION);
        road.verifyUnitMovement(1);
        road.verifyMoveControlVisible(true);

        // The empty road square shows its road on the real map.
        road.verifyRoadDrawn(ROAD_LOCATION);

        // Continue keeps the same pioneer with its exact final third and the
        // painted road.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        road.verifyCapturedUnitAt(COLONY_LOCATION);
        road.verifyUnitMovement(1);
        // The mouse must not highlight the road square while it is sampled.
        gamePage.moveMouseAtLocation(COLONY_LOCATION);
        road.verifyRoadDrawn(ROAD_LOCATION);

        // The next owner turn restores the full three thirds.
        gamePage.nextTurnAndCloseDialogs();
        road.verifyUnitMovement(3);
    }
}
