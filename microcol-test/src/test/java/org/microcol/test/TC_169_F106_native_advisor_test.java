package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.NativeChiefOutcome;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeAdvisorPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The native advisor (R-0201) on the Aztec capital. Before the chief has told
 * anything, the advisor names the nation and the attitude only; after the chief
 * visit with information it also names the goods and the skill. Reopening after
 * Save and Continue shows the same facts, and the map control centers the
 * settlement. After a gold visit, a repeat visit that gives the information
 * makes the advisor show it, while the first outcome stays gold. The model
 * rules are checked in NativeAdvisorReportTest and NativeChiefTest.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_169_F106_native_advisor_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native chief tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, learned by sight of the ordinary scout. */
    private static final Location CAPITAL = Location.of(23, 15);

    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 14);

    private static final int SEASONED_ID = 303;
    private static final Location SEASONED_AT = Location.of(23, 14);

    private static final String CONTENT_EN = "The natives are content. Chances: information 70.0%, gold 25.0%, death 5.0%.";

    private static final String NATIONS_EN = "Known nations: Aztec.";

    private static final String ATTITUDE_EN = "Attitude: content.";

    private static final String NOT_TOLD_EN = "The chief has not told you about this settlement yet.";

    private static final String FACTS_EN = "Chief information: goods for sale: Corn, Tobacco. Goods wanted: Musket, Tools, Horse. Teachable skill: Expert Farmer.";

    private static final String NOT_TOLD_CS = "Náčelník vám o této osadě zatím nic neřekl.";

    private static final String REPEAT_EN = "The natives are content. This nation has met their chief before, so you learn the same information again. Nothing is drawn, and no gold or risk applies.";

    /** The gold cap of a capital, as the model applies it. */
    private static final int CAPITAL_GOLD = 200;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_chief_facts_appear_after_the_visit_and_survive_continue_and_map_navigation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeAdvisorPage advisor = NativeAdvisorPage.of(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        fog.centerFrontier();

        // Learned by sight, the nation and the attitude are known, but the
        // chief has told nothing about the goods or the skill yet.
        advisor.openWithNKey();
        advisor.verifyNations(NATIONS_EN);
        advisor.verifyAttitude(ATTITUDE_EN);
        advisor.verifyChiefLine(NOT_TOLD_EN);
        advisor.verifyShowOnMapShown();
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();

        // The ordinary scout visits the chief, who gives the information.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.INFORMATION, 0,
                false);
        visit.closeChiefResult();
        visit.escape();
        visit.verifyChooserClosed();

        // The advisor now states the goods and the skill, and cancel changes
        // nothing.
        advisor.openWithNKey();
        advisor.verifyNations(NATIONS_EN);
        advisor.verifyChiefLine(FACTS_EN);
        advisor.captureAdvisor("native-advisor-en.png");
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();

        // The map control centers the known settlement and closes the advisor.
        advisor.openWithNKey();
        advisor.showOnMap();
        advisor.verifyDialogClosed();
        GamePage.of(getContext()).verifyThatSelectedTileIs(CAPITAL);

        // Save and Continue keep the chief facts, so the advisor reads them
        // again unchanged.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        advisor.openWithNKey();
        advisor.verifyNations(NATIONS_EN);
        advisor.verifyChiefLine(FACTS_EN);
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_03_information_after_a_gold_visit_is_learned_by_a_repeat_visit_and_survives_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeAdvisorPage advisor = NativeAdvisorPage.of(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.GOLD);
        fog.centerFrontier();

        // The first visit pays gold and tells nothing, so the advisor still
        // says that the chief has told nothing.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.GOLD, CAPITAL_GOLD,
                false);
        visit.closeChiefResult();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.GOLD);
        visit.escape();
        visit.verifyChooserClosed();

        advisor.openWithNKey();
        advisor.verifyNations(NATIONS_EN);
        advisor.verifyChiefLine(NOT_TOLD_EN);
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();

        // The seasoned scout's repeat visit gives the information again, and
        // the first outcome stays gold.
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(REPEAT_EN);
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.INFORMATION, 0, true);
        visit.closeChiefResult();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.GOLD);
        visit.escape();
        visit.verifyChooserClosed();

        // The advisor now states the goods and the skill it learned on that
        // repeat visit.
        advisor.openWithNKey();
        advisor.verifyNations(NATIONS_EN);
        advisor.verifyChiefLine(FACTS_EN);
        advisor.captureAdvisor("native-advisor-gold-repeat-en.png");
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();

        // Save and Continue keep the record and the information.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.GOLD);
        advisor.openWithNKey();
        advisor.verifyChiefLine(FACTS_EN);
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_02_czech_advisor_fits_and_states_that_the_chief_has_told_nothing()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeAdvisorPage advisor = NativeAdvisorPage.of(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        fog.centerFrontier();

        advisor.openWithNKey();
        advisor.verifyDialogFitsWindow();
        advisor.verifyChiefLine(NOT_TOLD_CS);
        advisor.captureAdvisor("native-advisor-cs.png");
        advisor.closeWithEscape();
        advisor.verifyDialogClosed();
    }

}
