package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.model.NativeChiefOutcome;
import org.akropia.page.FogPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.NativeVisitPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Retired chief encounters remain unavailable after the native redesign. */
@ExtendWith(ApplicationExtension.class)
public class TC_132_F123_chief_encounter_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.akropia");
    private static final Location CAPITAL = Location.of(23, 15);
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 14);
    private static final int SEASONED_ID = 303;
    private static final Location SEASONED_AT = Location.of(23, 14);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_information_is_not_offered_and_exploration_survives_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        new FogPage(getContext()).centerFrontier();
        // The former chief gesture offers no chief action and cannot draw a
        // reward or risk.
        visit.rememberRetiredVisit();
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChiefUnavailable(SCOUT_ID, CAPITAL);
        visit.verifyChooserClosed();
        // Saving and continuing cannot restore the retired interaction.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.rememberRetiredVisit();
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.verifyChiefUnavailable(SEASONED_ID, CAPITAL);
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_02_gold_is_not_offered_and_treasuries_stay_unchanged()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.GOLD);
        new FogPage(getContext()).centerFrontier();
        // The former chief gesture offers no chief action and cannot draw a
        // reward or risk.
        visit.rememberRetiredVisit();
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChiefUnavailable(SCOUT_ID, CAPITAL);
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_03_retired_death_draw_cannot_kill_a_scout() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.DEATH);
        new FogPage(getContext()).centerFrontier();
        // The former chief gesture offers no chief action and cannot draw a
        // reward or risk.
        visit.rememberRetiredVisit();
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChiefUnavailable(SCOUT_ID, CAPITAL);
        visit.verifyChooserClosed();
        // Saving and continuing cannot restore the retired interaction.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.rememberRetiredVisit();
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.verifyChiefUnavailable(SEASONED_ID, CAPITAL);
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_04_seasoned_scout_also_cannot_visit_a_chief() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        new FogPage(getContext()).centerFrontier();
        // The former chief gesture offers no chief action and cannot draw a
        // reward or risk.
        visit.rememberRetiredVisit();
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.verifyChiefUnavailable(SEASONED_ID, CAPITAL);
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_05_retired_chief_action_is_absent_in_czech() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        new FogPage(getContext()).centerFrontier();
        // The former chief gesture offers no chief action and cannot draw a
        // reward or risk.
        visit.rememberRetiredVisit();
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChiefUnavailable(SCOUT_ID, CAPITAL);
        visit.verifyChooserClosed();
    }
}
