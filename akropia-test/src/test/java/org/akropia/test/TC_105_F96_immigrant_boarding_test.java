package org.akropia.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.UnitType;
import org.akropia.page.EuropeEquipmentPage;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.ImmigrantBoardingPage;
import org.akropia.page.RecruitmentPage;
import org.akropia.page.SentryPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_105_F96_immigrant_boarding_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F96-immigrant-boarding.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_immigrant_default_opt_out_front_recruit_depart_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        final ImmigrantBoardingPage boarding = new ImmigrantBoardingPage(
                getContext());
        final SentryPage sentry = new SentryPage(getContext());
        // Pre-loaded 100 starts willing, 101 was opted out, 103 is ordinary.
        boarding.verifyBoardingState(100, true, false);
        boarding.verifyBoardingState(101, false, false);
        boarding.verifyMarkerText(101, "Ashore");
        sentry.verifyWaitingOnPier(103, false);
        // Dismissing the menu changes nothing.
        boarding.openPierOrders(100);
        boarding.cancelMenu();
        boarding.verifyBoardingState(100, true, false);
        // Restore 101 with the same dock control.
        boarding.openPierOrders(101);
        boarding.chooseBoardingOrder(101);
        boarding.verifyBoardingState(101, true, false);
        // Paid recruitment creates a willing immigrant on the pier.
        final RecruitmentPage recruit = new RecruitmentPage(getContext());
        recruit.open();
        recruit.confirm(0);
        boarding.verifyRecruitedImmigrant(105, UnitType.COLONIST);
        boarding.verifyPierOrder(List.of(100, 101, 103, 105));
        // Promoting 105 leads the dock; promoting 100 replaces that choice.
        boarding.openPierOrders(105);
        boarding.chooseFrontOfQueue(105);
        boarding.verifyBoardingState(105, true, true);
        boarding.verifyMarkerText(105, "First");
        boarding.verifyPierOrder(List.of(105, 100, 101, 103));
        boarding.verifyDisplayedPierOrder(List.of(105, 100, 101, 103));
        boarding.openPierOrders(100);
        boarding.chooseFrontOfQueue(100);
        boarding.verifyBoardingState(100, true, true);
        boarding.verifyBoardingState(105, true, false);
        boarding.verifyPierOrder(List.of(100, 101, 103, 105));
        // Opting 101 out keeps it ashore; the front choice stays on 100.
        boarding.openPierOrders(101);
        boarding.chooseBoardingOrder(101);
        boarding.verifyBoardingState(101, false, false);
        boarding.verifyBoardingState(100, true, true);
        // Bring 105 forward again so the single free hold is contested.
        boarding.openPierOrders(105);
        boarding.chooseFrontOfQueue(105);
        boarding.verifyBoardingState(105, true, true);
        boarding.verifyBoardingState(100, true, false);
        boarding.verifyDisplayedPierOrder(List.of(105, 100, 101, 103));
        // The one free hold takes the front choice; 100 and 101 stay ashore.
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        boarding.verifyBoardingState(105, true, true);
        new EuropeEquipmentPage(getContext()).depart(102);
        sentry.verifyPassenger(105, 102, 1, 0);
        boarding.verifyBoardingState(100, true, false);
        boarding.verifyBoardingState(101, false, false);
        sentry.verifyWaitingOnPier(103, false);
        // Disk Continue keeps every choice and never boards anyone again.
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        sentry.verifyPassenger(105, 102, 1, 0);
        boarding.verifyBoardingState(100, true, false);
        boarding.verifyBoardingState(101, false, false);
        sentry.verifyWaitingOnPier(103, false);
    }
}
