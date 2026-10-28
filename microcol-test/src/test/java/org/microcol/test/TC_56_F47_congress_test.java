package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * A frozen offer becomes one lasting member and one real ship after production.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_56_F47_congress_test extends AbstractMicroColTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F47-congress.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_choose_jones_produce_arrive_and_continue_once()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.rememberWorld();
        congress.open();
        congress.verifyFillsGameWindow("Back to game");
        congress.verifyWorldUnchanged();
        congress.verifyOffer(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.verifyProgress(99, 100,
                "Choose the next founder. Bells wait until a choice is made.",
                "None");
        congress.captureDialog("congressDialog", "r104-congress-offer.png");
        congress.verifySelectionRequired();
        congress.verifyCardColumns(3);
        // A card explains the benefit before the separate confirmation.
        congress.select(Founder.FERDINAND_MAGELLAN);
        congress.verifyFounderSelection(Founder.FERDINAND_MAGELLAN,
                "Ships gain +1 movement. New voyages to Europe from the western edge take half as long, rounded up.");
        congress.select(Founder.JOHN_PAUL_JONES);
        congress.verifyFounderSelection(Founder.JOHN_PAUL_JONES,
                "John Paul Jones grants one free frigate on joining.");
        congress.verifyWorldUnchanged();
        congress.scrollReportToStart();
        congress.captureDialog("congressDialog",
                "r0332-congress-screen-small.png");
        congress.close();
        congress.verifyReturnedToGame();
        congress.verifyWorldUnchanged();
        // A wider window fits all five cards in one row using the same
        // controls.
        final MainScreenLayoutPage window = new MainScreenLayoutPage(
                getContext());
        window.resize(1280, 850);
        congress.open();
        congress.verifyFillsGameWindow("Back to game");
        congress.verifyCardColumns(5);
        congress.verifySelectionRequired();
        congress.captureDialog("congressDialog",
                "r0332-congress-screen-wide.png");
        // Escape also returns to the same map without confirming the card.
        congress.select(Founder.THOMAS_JEFFERSON);
        congress.closeWithEscape();
        congress.verifyReturnedToGame();
        congress.verifyWorldUnchanged();
        window.resize(900, 750);
        // Only explicit confirmation chooses the objective; Continue does not
        // reroll it.
        congress.open();
        congress.select(Founder.JOHN_PAUL_JONES);
        congress.choose();
        congress.verifyProgress(99, 100, "Working toward: John Paul Jones",
                "None");
        congress.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyProgress(99, 100, "Working toward: John Paul Jones",
                "None");
        congress.close();
        // The unattended town hall makes one bell in the normal national
        // update.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "John Paul Jones has joined Congress.");
        report.orElseThrow().close();
        congress.open();
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "John Paul Jones");
        congress.verifyReward(1, 10000, 1);
        congress.captureDialog("congressDialog", "r104-congress-member.png");
        congress.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyReward(1, 10000, 1);
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "John Paul Jones");
        congress.close();
        // Pending selection banks new bells and never repeats the joining
        // effect.
        game.nextTurnAndCloseDialogs();
        congress.open();
        congress.verifyProgress(1, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "John Paul Jones");
        congress.verifyReward(1, 10000, 2);
        congress.close();
    }

    @Test
    @Tag("local")
    void TC_02_changed_objective_rejects_old_confirmation() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.select(Founder.JOHN_PAUL_JONES);
        congress.chooseElsewhere(Founder.ADAM_SMITH);
        congress.rememberWorld();
        congress.choose();
        congress.verifyChanged();
        congress.verifyWorldUnchanged();
        congress.close();
        congress.open();
        congress.verifyProgress(99, 100, "Working toward: Adam Smith", "None");
        congress.close();
    }
}
