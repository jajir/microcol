package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** National favour is visible without a chief visit and survives Continue. */
@ExtendWith(ApplicationExtension.class)
public class TC_169_F106_native_advisor_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.akropia");
    private static final Location CAPITAL = Location.of(23, 15);
    private static final Location VISITOR = Location.of(22, 14);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_favour_changes_after_a_missionary_and_survives_continue_and_map_navigation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", 310, VISITOR, 311,
                Location.of(23, 14));
        new FogPage(getContext()).centerFrontier();
        final NativeAdvisorPage advisor = NativeAdvisorPage.of(getContext());
        // Known settlements disclose national favour immediately.
        advisor.openWithNKey();
        advisor.verifyFavourLine("Favour towards Corinth: 5/10");
        advisor.verifyFriend("No friend");
        advisor.verifyDialogFitsWindow();
        advisor.closeWithEscape();
        // A confirmed missionary changes the same report.
        new NativeVisitPage(getContext()).enterSettlement(VISITOR, CAPITAL);
        mission.confirmMission();
        mission.verifyMissionApplied(310, CAPITAL, 6);
        advisor.openWithNKey();
        advisor.verifyFavourLine("Favour towards Corinth: 6/10");
        advisor.verifyShowOnMapShown();
        advisor.showOnMap();
        advisor.verifyDialogClosed();
        // Continue retains the national relationship.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        advisor.openWithNKey();
        advisor.verifyFavourLine("Favour towards Corinth: 6/10");
        advisor.verifyFriend("No friend");
        advisor.captureAdvisor("native-advisor-en.png");
        advisor.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_02_czech_advisor_displays_favour_and_fits_the_window()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        new FogPage(getContext()).centerFrontier();
        final NativeAdvisorPage advisor = NativeAdvisorPage.of(getContext());
        // No visit or payment is needed to inspect known settlements.
        advisor.openWithNKey();
        advisor.verifyFavourLine("5/10");
        advisor.verifyFriend("Bez přítele");
        advisor.verifyDialogFitsWindow();
        advisor.captureAdvisor("native-advisor-cs.png");
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();
    }
}
