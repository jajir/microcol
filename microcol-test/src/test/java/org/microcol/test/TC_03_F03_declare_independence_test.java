package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_03_F03_declare_independence_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_03_declare_independence() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // declare independence
        final DialogMessagePage dialogMessagePage = gamePage
                .declareIndependence();
        dialogMessagePage.verifyReady();

        // close first dialog
        dialogMessagePage.close();

        // close second dialog
        dialogMessagePage.close();

        gamePage = GamePage.of(getContext());
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();
        gamePage.nextTurnAndCloseDialogs();

        // The royal expedition lands armed soldiers who can actually attack.
        gamePage.verifyMinimumArmedKingSoldiersOnMap(3);
    }

}
