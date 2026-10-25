package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Unknown map contents stay hidden while exploration survives continuation. */
@ExtendWith(ApplicationExtension.class)
public class TC_26_F18_fog_of_war_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F18-fog-of-war.microcol");
    private static final Location START = Location.of(25, 16);
    private static final Location STEP = Location.of(26, 16);
    private static final Location ENEMY = Location.of(28, 16);
    private static final Location COLONY = Location.of(28, 17);
    private static final Location OWN_COLONY = Location.of(24, 16);
    private static final Location SHORE = Location.of(25, 16);
    private static final Location OUTSIDE = Location.of(27, 16);
    /** Sees the galleon at the enemy tile but not the colony tile below. */
    private static final Location HALF_SIGHT = Location.of(26, 14);
    private static final Location FAR_SEA = Location.of(40, 16);
    private static final String LAST_SEEN = "Last seen: colony Hidden Quebec of France";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_unknown_contents_and_movement_preview_remain_separate()
            throws Exception {
        // Hidden enemy and colony exist, but their tiles disclose no details.
        WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyExplored(ENEMY, false);
        fog.verifyExplored(COLONY, false);
        fog.inspect(ENEMY);
        fog.verifyUnknownInformation("This area is still hidden",
                "Hidden Quebec");
        fog.verifyFogPixels(ENEMY);
        fog.centerFrontier();
        fog.inspect(COLONY);
        fog.verifyUnknownInformation("This area is still hidden",
                "Hidden Quebec");
        fog.verifyFogPixels(COLONY);

        // A known destination has a path; unknown contents do not produce one.
        fog.preview(START, STEP);
        try {
            fog.verifyPreviewIncludes(STEP);
        } finally {
            fog.cancelPreview();
        }
        fog.verifyUnitLocation(3, START);
        fog.preview(START, ENEMY);
        try {
            fog.verifyPreviewEmpty(ENEMY);
        } finally {
            fog.cancelPreview();
        }
        fog.verifyUnitLocation(3, START);
        fog.verifyExplored(ENEMY, false);
        fog.captureDialog("rightPanel", "fog-unknown.png");
    }

    @Test
    @Tag("local")
    void TC_03_fog_covers_animation_without_suppressing_completion()
            throws Exception {
        // A diagnostic overlay exercises the same compositor as enemy movement.
        WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyExplored(STEP, true);
        fog.verifyExplored(ENEMY, false);
        fog.drawDiagnosticAnimation(STEP, ENEMY);
        fog.verifyAnimationCoveredOnlyInFog(STEP, ENEMY);
        fog.verifyUnitLocation(3, START);
    }

    @Test
    @Tag("local")
    void TC_02_exploration_is_retained_after_leaving_and_continuing()
            throws Exception {
        // Moving reveals the neighbouring frontier without changing the enemy.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyExplored(ENEMY, false);
        fog.move(START, STEP, 3);
        fog.verifyExplored(ENEMY, true);
        fog.verifyExplored(COLONY, true);
        fog.centerFrontier();
        fog.inspect(COLONY);
        game.verifyThatStatusBarContains("Hidden Quebec");
        fog.move(STEP, START, 3);
        fog.verifyExplored(ENEMY, true);

        // Continue preserves earlier discovery beyond the ship's current sight.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        fog.verifyUnitLocation(3, START);
        fog.verifyExplored(ENEMY, true);
        fog.verifyExplored(COLONY, true);
        fog.centerFrontier();
        fog.inspect(COLONY);
        game.verifyThatStatusBarContains("Hidden Quebec");
        fog.captureDialog("rightPanel", "fog-discovered-continued.png");
    }

    @Test
    @Tag("local")
    void TC_04_foreign_units_follow_present_sight_and_hidden_targets_are_refused()
            throws Exception {
        // Beyond sight the galleon is unknown and cannot be attacked.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyObservedUnits(ENEMY, 0);
        fog.rememberActionPoints(3);
        fog.verifyAttackRefused(3, ENEMY);

        // Sailing closer shows the galleon in the tile information.
        fog.move(START, STEP, 3);
        fog.verifyObservedUnits(ENEMY, 1);
        fog.centerFrontier();
        fog.inspect(ENEMY);
        game.verifyNumberOfUnitInRightPanel(1);

        // Sailing away keeps the explored sea but removes the galleon.
        fog.move(STEP, START, 3);
        fog.verifyExplored(ENEMY, true);
        fog.verifyObservedUnits(ENEMY, 0);
        fog.centerFrontier();
        fog.inspect(ENEMY);
        game.verifyNumberOfUnitInRightPanel(0);
        game.verifyThatStatusBarContains("This is ");
        fog.verifyStatusBarOmits("with");

        // Returning shows the galleon again, still beyond attack range.
        fog.move(START, STEP, 3);
        fog.verifyObservedUnits(ENEMY, 1);
        fog.rememberActionPoints(3);
        fog.verifyAttackRefused(3, ENEMY);
    }

    @Test
    @Tag("local")
    void TC_05_colony_knowledge_is_remembered_stale_and_survives_continue()
            throws Exception {
        // A colony nobody has seen is unknown.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyNoKnownColony(COLONY);

        // Seeing it records its name and owner.
        fog.move(START, STEP, 3);
        fog.verifyKnownColony(COLONY, "Hidden Quebec", "France", true);
        fog.rememberColonyObservation(COLONY);

        // Out of sight it is shown as the stale last observation.
        fog.move(STEP, START, 3);
        fog.verifyKnownColony(COLONY, "Hidden Quebec", "France", false);
        fog.centerFrontier();
        fog.inspect(COLONY);
        game.verifyThatStatusBarContains(LAST_SEEN);
        fog.verifyRightPanelContains(LAST_SEEN);

        // A hidden change is neither shown nor stored.
        fog.captureColonyByKing(COLONY, 4);
        fog.verifyKnownColony(COLONY, "Hidden Quebec", "France", false);
        fog.verifyColonyObservationUnchanged(COLONY);
        fog.inspect(COLONY);
        fog.verifyStatusBarOmits("Dutch's King");

        // Continue restores the same memory without refreshing it.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        fog.verifyUnitLocation(3, START);
        fog.verifyColonyObservationUnchanged(COLONY);
        fog.centerFrontier();
        fog.inspect(COLONY);
        game.verifyThatStatusBarContains(LAST_SEEN);
        fog.verifyStatusBarOmits("Dutch's King");

        // Seeing the colony again updates the memory.
        fog.move(START, STEP, 3);
        fog.verifyKnownColony(COLONY, "Hidden Quebec", "Dutch's King", true);
        fog.verifyColonyObservationRefreshed(COLONY, "Dutch's King");
    }

    @Test
    @Tag("local")
    void TC_06_owned_colony_sees_its_neighbors_and_capture_transfers_the_sight()
            throws Exception {
        // With the ship far away only the colony watches its neighbors.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.placeUnit(3, FAR_SEA);
        fog.forgetExploration();
        fog.verifyExplored(SHORE, false);
        fog.observeFromUnit(3);
        fog.verifyExplored(SHORE, true);
        fog.verifyExplored(Location.of(23, 15), true);
        fog.verifyExplored(OUTSIDE, false);
        fog.centerFrontier();
        fog.verifyCurrentSight(SHORE, true);
        fog.verifyCurrentSight(OUTSIDE, false);

        // A foreign galleon entering the neighborhood is shown, then hidden.
        fog.placeUnit(106, SHORE);
        fog.verifyObservedUnits(SHORE, 1);
        fog.inspect(SHORE);
        game.verifyNumberOfUnitInRightPanel(1);
        fog.placeUnit(106, OUTSIDE);
        fog.verifyObservedUnits(OUTSIDE, 0);
        fog.verifyExplored(OUTSIDE, false);
        fog.inspect(OUTSIDE);
        game.verifyNumberOfUnitInRightPanel(0);

        // The king takes the colony: its sight ends, the terrain stays known.
        fog.placeUnit(106, SHORE);
        fog.captureColonyByKing(OWN_COLONY, 4);
        fog.verifyCurrentSight(SHORE, false);
        fog.verifyObservedUnits(SHORE, 0);
        fog.verifyExplored(SHORE, true);
        fog.verifyKnownColony(OWN_COLONY, "New Amsterdam", "Dutch's King",
                false);
        fog.inspect(SHORE);
        game.verifyNumberOfUnitInRightPanel(0);
    }

    @Test
    @Tag("local")
    void TC_07_foreign_steps_outside_present_sight_schedule_no_animation()
            throws Exception {
        // Sail until the galleon is in sight, then watch the interface.
        WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.move(START, STEP, 3);
        fog.listenForForeignSteps();

        // A step between two observed tiles is animated.
        fog.announceForeignStep(106, ENEMY, OUTSIDE);
        fog.verifyForeignSteps(1, 1);

        // A step leaving sight and a step far away produce no frames.
        fog.announceForeignStep(106, ENEMY, Location.of(29, 16));
        fog.announceForeignStep(106, Location.of(30, 16), Location.of(31, 16));
        fog.verifyForeignSteps(1, 1);

        // After sailing away the same step is no longer shown.
        fog.move(STEP, START, 3);
        fog.listenForForeignSteps();
        fog.announceForeignStep(106, OUTSIDE, ENEMY);
        fog.verifyForeignSteps(0, 0);

        // A foreign fight is shown only when both fighters are in sight.
        fog.captureColonyByKing(COLONY, 4);
        fog.placeUnit(3, STEP);
        fog.listenForForeignSteps();
        fog.announceForeignFight(106, 4);
        fog.verifyForeignFights(1);
        fog.placeUnit(3, HALF_SIGHT);
        fog.announceForeignFight(106, 4);
        fog.verifyForeignFights(1);
        fog.placeUnit(3, START);
        fog.announceForeignFight(106, 4);
        fog.verifyForeignFights(1);
    }

    @Test
    @Tag("local")
    void TC_09_explored_grid_stops_at_unknown_boundary() throws Exception {
        // Enable the existing display preference through its checkbox.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        new org.microcol.page.FramedSettingsPage(getContext())
                .click("settingGrid");
        GamePage game = settings.goBack().loadGame();
        FogPage fog = new FogPage(getContext());
        fog.centerFrontier();
        fog.verifyGrid(OWN_COLONY, ENEMY, true);
        fog.verifyGrid(STEP, ENEMY, true);
        fog.verifyFogPixels(ENEMY);
        fog.verifyGridUnderRoundedFog();
        fog.verifyFogTileJoins();
        fog.saveMapEvidence("R-0268-explored-grid.png");

        // Exploration and its grid survive departure from sight and Continue.
        fog.move(START, STEP, 3);
        fog.move(STEP, START, 3);
        fog.verifyExplored(ENEMY, true);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        fog.centerFrontier();
        fog.verifyGrid(ENEMY, Location.of(30, 16), true);
        // The viewport can resize without moving the grid away from its tiles.
        fog.resizeMapWindow(1100, 800);
        fog.centerFrontier();
        fog.verifyGrid(ENEMY, Location.of(30, 16), true);
        fog.verifyGridUnderRoundedFog();
        fog.saveMapEvidence("R-0268-resized-grid.png");

        // Disabling the same preference removes lines without moving the ship.
        game.exitToMenu();
        settings = WelcomePage.of(getContext()).openSetting();
        new org.microcol.page.FramedSettingsPage(getContext())
                .click("settingGrid");
        settings.goBack();
        new FreePlayAuditPage(getContext()).continueGame();
        fog.centerFrontier();
        fog.verifyGrid(STEP, Location.of(30, 16), false);
        fog.verifyUnitLocation(3, START);
    }
}
