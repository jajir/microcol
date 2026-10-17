package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.page.CongressPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Recruiting De Soto reveals the stationary observer's second ring once. */
@ExtendWith(ApplicationExtension.class)
public class TC_69_F60_desoto_sight_test extends AbstractMicroColTest {
    private static final Location OBSERVER = Location.of(29, 16);
    private static final Location SECOND_RING = Location.of(31, 16);
    private static final Location BEYOND = Location.of(32, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F60-desoto-sight.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_arrival_reveals_without_moving_and_continue_retains_discovery()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        CongressPage congress = new CongressPage(getContext());
        // A stationary ordinary colonist knows only its first ring.
        fog.centerFrontier();
        fog.verifyUnitLocation(4, OBSERVER);
        fog.verifySightRadius(4, 1);
        fog.verifyExplored(SECOND_RING, false);
        fog.verifyExplored(BEYOND, false);
        fog.verifyFogPixels(SECOND_RING);

        // Choosing an objective is not arrival and reveals no extra terrain.
        congress.open();
        congress.select(Founder.HERNANDO_DE_SOTO);
        congress.choose();
        congress.close();
        fog.verifyExplored(SECOND_RING, false);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fog.verifyExplored(SECOND_RING, false);

        // One actual town-hall bell completes recruitment during the next turn.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Hernando de Soto has joined Congress.");
        report.orElseThrow().close();
        fog.centerFrontier();
        fog.verifyUnitLocation(4, OBSERVER);
        fog.verifySightRadius(4, 2);
        fog.verifyExplored(SECOND_RING, true);
        fog.verifyExplored(BEYOND, false);
        fog.verifyNoFogBorder(Location.of(30, 16));
        fog.verifyRevealedPixels(SECOND_RING);
        fog.verifyFogPixels(BEYOND);
        fog.captureDialog("rightPanel", "r69-desoto-arrival.png");

        // Continue retains both the founder and discovery without movement.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fog.centerFrontier();
        fog.verifyUnitLocation(4, OBSERVER);
        fog.verifySightRadius(4, 2);
        fog.verifyExplored(SECOND_RING, true);
        fog.verifyExplored(BEYOND, false);
        fog.verifyNoFogBorder(Location.of(30, 16));
        fog.verifyRevealedPixels(SECOND_RING);
        congress.open();
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "Hernando de Soto");
        congress.close();
    }
}
