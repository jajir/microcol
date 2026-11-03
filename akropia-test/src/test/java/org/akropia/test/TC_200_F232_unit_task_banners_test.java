package org.akropia.test;

import java.io.File;
import org.akropia.page.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** R-0402/R-0403: shared identities and large flags in player help. */
@ExtendWith(ApplicationExtension.class)
public class TC_200_F232_unit_task_banners_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F232-unit-task-banners.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_twelve_identities_are_available_in_help() throws Exception {
        // Help exposes every identity with its large flag and the agreed
        // narrative.
        WelcomePage.of(getContext()).loadGame();
        final ColonizopediaPage help = ColonizopediaPage.of(getContext());
        help.openWithHKey();
        help.selectTopicWithMouse("City states and local peoples");
        help.verifyReferenceRows(12);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0402-identities-help.png");
        help.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_02_ship_rider_and_worker_banners_follow_order_changes()
            throws Exception {
        // A working person, idle rider and idle ship retain their owner
        // banners.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final WorldWorkersPage banners = new WorldWorkersPage(getContext());
        banners.verifyTask(0, "w");
        banners.verifyTask(5, "");
        banners.verifyTask(3, "");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0403-idle-rider-ship-worker.png");

        // A pending fortification is visible immediately, even with zero
        // movement.
        game.selectTile(org.akropia.model.Location.of(24, 15));
        final FortifyPage fortify = new FortifyPage(getContext());
        fortify.changeOrder();
        banners.verifyTask(5, "f");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0403-rider-fortifying.png");
        fortify.changeOrder();
        banners.verifyTask(5, "");

        // A ship's sentry order changes and clears the same banner.
        game.selectTile(org.akropia.model.Location.of(29, 16));
        banners.changeOrder("sentryUnit");
        banners.verifyTask(3, "s");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0403-ship-sentry.png");
        banners.changeOrder("sentryUnit");
        banners.verifyTask(3, "");
    }
}
