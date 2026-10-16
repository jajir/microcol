package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SentryPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_45_F36_sentry_boarding_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F36-sentry-boarding.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_pier_cancel_activate_continue_and_capacity_order()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        final SentryPage sentry = new SentryPage(getContext());
        // Dismissing orders changes nothing; explicit orders retain movement.
        sentry.openPierOrders(100);
        sentry.cancelMenu();
        sentry.verifyWaitingOnPier(100, false);
        sentry.openPierOrders(100);
        sentry.choosePierOrder(100);
        sentry.verifyWaitingOnPier(100, true);
        sentry.openPierOrders(100);
        sentry.choosePierOrder(100);
        sentry.verifyWaitingOnPier(100, false);
        // Reverse assignment order so the departure must use stable identity.
        sentry.openPierOrders(101);
        sentry.choosePierOrder(101);
        sentry.openPierOrders(100);
        sentry.choosePierOrder(100);
        sentry.verifyWaitingOnPier(101, true);
        sentry.captureDialog("sentry-marker-101", "sentry-pier.png");
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        sentry.verifyWaitingOnPier(100, true);
        sentry.verifyWaitingOnPier(101, true);
        // The one free hold takes 100; 101 waits and unordered 103 stays
        // ashore.
        new EuropeEquipmentPage(getContext()).depart(102);
        sentry.verifyPassenger(100, 102, 1, 0);
        sentry.verifyWaitingOnPier(101, true);
        sentry.verifyWaitingOnPier(103, false);
    }

    @Test
    @Tag("local")
    void TC_02_map_sentry_preserves_moves_then_boards_departing_ship()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final SentryPage sentry = new SentryPage(getContext());
        // Toggle an equipped passenger's order without spending movement.
        game.selectTile(Location.of(24, 15));
        sentry.verifyMapAction("Sentry");
        sentry.changeMapOrder();
        sentry.verifyOrder(104, true, 1);
        sentry.verifyMapAction("Activate");
        // Dragging does not silently cancel a persistent order.
        game.moveMouseAtLocation(Location.of(24, 15));
        game.dragMouseAtLocationWithFeedback(Location.of(24, 14),
                "End sentry and keep remaining movement.");
        sentry.verifyMapPosition(104, Location.of(24, 15));
        sentry.verifyOrder(104, true, 1);
        game.selectTile(Location.of(24, 15));
        sentry.changeMapOrder();
        sentry.verifyOrder(104, false, 1);
        sentry.changeMapOrder();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        sentry.verifyOrder(104, true, 1);
        // A real departure loads the adjacent pioneer for one passenger move.
        game.moveMouseAtLocation(Location.of(25, 15));
        game.dragMouseAtLocation(Location.of(25, 16));
        game.waitForNumberOfUnitsAt(Location.of(25, 16), 1);
        sentry.verifyPassenger(104, 3, 0, 80);
    }

    @Test
    @Tag("local")
    void TC_03_czech_map_order_labels() throws Exception {
        final org.microcol.page.SettingPage setting = WelcomePage
                .of(getContext()).openSetting();
        setting.setCzechLanguage();
        setting.goBack();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final SentryPage sentry = new SentryPage(getContext());
        game.selectTile(Location.of(24, 15));
        sentry.verifyMapAction("Hlídka");
        sentry.changeMapOrderWithKeyboard();
        sentry.verifyMapAction("Aktivovat");
        sentry.captureDialog("sentryUnit", "sentry-map-czech.png");
        sentry.verifyOrder(104, true, 1);
    }
}
