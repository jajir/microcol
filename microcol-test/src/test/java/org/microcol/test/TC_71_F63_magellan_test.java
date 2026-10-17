package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Actual Magellan arrival changes future departures, not existing voyages. */
@ExtendWith(ApplicationExtension.class)
public class TC_71_F63_magellan_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F63-magellan.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_magellan_adds_movement_and_shortens_only_new_western_departure()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        AtlanticAuditPage voyage = new AtlanticAuditPage(getContext());
        voyage.verifyMovement(6);
        // Recruit through the actual Congress and one ordinary bell update.
        congress.open();
        congress.select(Founder.FERDINAND_MAGELLAN);
        congress.choose();
        congress.close();
        game.nextTurnAndCloseDialogs();
        voyage.verifyMagellanArrival(3);
        voyage.verifyMovement(7);
        // Existing voyage duration stays four; saved movement is not
        // replenished.
        game.moveMouseAtLocation(Location.of(3, 16));
        game.dragMouseAtLocation(Location.of(2, 16));
        voyage.verifyMovement(6);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        voyage.verifyMagellanArrival(3);
        voyage.verifyMovement(6);
        // A real west-edge departure applies the bonus to its sampled duration.
        game.moveMouseAtLocation(Location.of(2, 16));
        game.dragMouseAtLocation(Location.of(1, 16));
        final int duration = voyage.verifyVoyageSample(true, true);
        final int weather = voyage.getVoyageWeather();
        voyage.verifyMovement(5);
        EuropePortScreen europe = game.openEuroperPort();
        voyage.verifyVoyageText("Turns remaining: " + duration);
        voyage.captureDialog("voyage-remaining-3",
                "magellan-western-duration.png");
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        voyage.verifyVoyageMetadata(weather, duration, duration, true);
        voyage.verifyMovement(5);
        for (int remaining = duration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
        }
        voyage.verifyInEurope();
        voyage.verifyCargo();
        // Returning to the map uses the unchanged ordinary duration formula.
        europe = game.openEuroperPort();
        voyage.verifyVoyagePanelsSeparated();
        europe.selectUnitFromPort(0);
        voyage.sendShipBack();
        final int homeward = voyage.verifyVoyageSample(false, true);
        voyage.verifyVoyageText("Turns remaining: " + homeward);
        voyage.verifyCargo();
    }
}
