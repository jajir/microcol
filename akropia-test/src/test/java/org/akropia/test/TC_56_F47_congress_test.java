package org.akropia.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Founder;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Banked owls purchase a permanent discovery and its ship without advancing the
 * turn.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_56_F47_congress_test extends AbstractAkropiaTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F47-congress.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_choose_jones_produce_arrive_and_continue_once()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.rememberWorld();
        // Discover the shortcut, open Knowledge and return without a purchase.
        congress.hoverOpenButton();
        game.verifyThatStatusBarContains(
                "Open Knowledge and acquire a bonus with owls.");
        congress.openWithShortcut();
        congress.verifyFillsGameWindow("Back to game");
        congress.verifyWorldUnchanged();
        congress.closeWithEscape();
        congress.verifyReturnedToGame();
        congress.verifyWorldUnchanged();
        congress.open();
        congress.verifyFillsGameWindow("Back to game");
        congress.verifyWorldUnchanged();
        congress.verifyOffer(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.verifyProgress(99, 100,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "None");
        congress.captureDialog("congressDialog", "r104-congress-offer.png");
        congress.verifySelectionRequired();
        congress.verifyCardColumns(3);
        // A card explains the benefit before the separate confirmation.
        congress.select(Founder.FERDINAND_MAGELLAN);
        congress.verifyFounderSelection(Founder.FERDINAND_MAGELLAN,
                "Ships gain +1 movement. New voyages to Home port from the western edge take half as long, rounded up.",
                false);
        congress.select(Founder.JOHN_PAUL_JONES);
        congress.verifyFounderSelection(Founder.JOHN_PAUL_JONES,
                "Grants one free frigate once.", false);
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
        // Produce the missing owl before buying; no discovery is selected in
        // advance.
        game.nextTurnAndCloseDialogs();
        congress.open();
        congress.verifyProgress(100, 100,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "None");
        congress.select(Founder.JOHN_PAUL_JONES);
        congress.verifyFounderSelection(Founder.JOHN_PAUL_JONES,
                "Grants one free frigate once.");
        congress.verifyKnowledgeArtwork(Founder.JOHN_PAUL_JONES);
        // Explicit confirmation charges the account and grants the reward
        // immediately.
        congress.choose();
        congress.verifyProgress(0, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Master shipbuilding");
        congress.verifyReward(1, 10000, 1);
        congress.captureDialog("congressDialog", "r104-congress-member.png");
        congress.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyReward(1, 10000, 1);
        congress.verifyProgress(0, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Master shipbuilding");
        congress.close();
        // The next offer banks new owls and never repeats the acquisition
        // effect.
        game.nextTurnAndCloseDialogs();
        congress.open();
        congress.verifyProgress(1, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Master shipbuilding");
        congress.verifyReward(1, 10000, 2);
        congress.close();
    }

    @Test
    @Tag("local")
    void TC_03_knowledge_help_opens_its_topic_without_spending_owls()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        congress.rememberWorld();
        congress.open();
        congress.openKnowledgeHelp();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.verifyDialogOpen();
        help.verifySelectedTopic("Knowledge and historical references", "owls");
        help.verifyReferenceRows(25);
        help.closeWithOk();
        congress.verifyWorldUnchanged();
        congress.verifyReturnedToGame();
    }

    @Test
    @Tag("local")
    void TC_02_changed_objective_rejects_old_confirmation() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
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
        congress.verifyProgress(100, 100, "Planned discovery: Large workshops",
                "None");
        congress.close();
    }
}
