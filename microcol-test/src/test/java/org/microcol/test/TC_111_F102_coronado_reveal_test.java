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

/**
 * Coronado's arrival remembers the foreign Crown Post with its terrain once,
 * while its hidden residents stay hidden.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_111_F102_coronado_reveal_test extends AbstractMicroColTest {
    private static final Location CROWN_POST = Location.of(32, 16);
    private static final Location WEST_NEIGHBOR = Location.of(31, 16);
    private static final Location NORTH_EAST_NEIGHBOR = Location.of(33, 15);
    /** Two tiles beyond the colony: outside its neighborhood. */
    private static final Location BEYOND = Location.of(34, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F102-coronado-reveal.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_arrival_remembers_foreign_colony_and_keeps_it_after_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FogPage fog = new FogPage(getContext());
        CongressPage congress = new CongressPage(getContext());
        // Before Coronado, the Crown's colony and its terrain are unknown.
        fog.centerFrontier();
        fog.verifyExplored(CROWN_POST, false);
        fog.verifyExplored(WEST_NEIGHBOR, false);
        fog.verifyNoKnownColony(CROWN_POST);
        fog.verifyFogPixels(CROWN_POST);

        // Selecting Coronado and canceling Congress reveals nothing.
        congress.open();
        congress.select(Founder.FRANCISCO_DE_CORONADO);
        congress.close();
        fog.verifyExplored(CROWN_POST, false);
        fog.verifyNoKnownColony(CROWN_POST);

        // Choosing him is not arrival; the founder joins during the next turn.
        congress.open();
        congress.select(Founder.FRANCISCO_DE_CORONADO);
        congress.choose();
        congress.close();
        fog.verifyExplored(CROWN_POST, false);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fog.verifyExplored(CROWN_POST, false);
        fog.verifyNoKnownColony(CROWN_POST);

        // The arrival explores the colony and its neighbors, not beyond them.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Francisco de Coronado has joined Congress.");
        report.orElseThrow().close();
        fog.centerFrontier();
        fog.verifyExplored(CROWN_POST, true);
        fog.verifyExplored(WEST_NEIGHBOR, true);
        fog.verifyExplored(NORTH_EAST_NEIGHBOR, true);
        fog.verifyExplored(BEYOND, false);
        // Remembered, not currently seen; the Crown's colonist stays hidden.
        fog.verifyKnownColony(CROWN_POST, "Crown Post", "Dutch's King", false);
        fog.verifyCurrentSight(CROWN_POST, false);
        fog.verifyObservedUnits(CROWN_POST, 0);
        fog.verifyRevealedPixels(CROWN_POST);
        fog.verifyRevealedPixels(WEST_NEIGHBOR);

        // Continue from disk keeps the discovery without replaying the arrival.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fog.centerFrontier();
        fog.verifyExplored(CROWN_POST, true);
        fog.verifyExplored(BEYOND, false);
        fog.verifyKnownColony(CROWN_POST, "Crown Post", "Dutch's King", false);
        fog.verifyObservedUnits(CROWN_POST, 0);
        fog.verifyRevealedPixels(WEST_NEIGHBOR);
        congress.open();
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "Francisco de Coronado");
        congress.close();
    }
}
