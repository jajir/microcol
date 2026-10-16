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
    private static final Location ENEMY = Location.of(33, 16);
    private static final Location COLONY = Location.of(33, 17);

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
            fog.verifyPreviewEmpty();
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
}
