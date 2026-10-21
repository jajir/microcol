package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeCombatPage;
import org.microcol.page.NativeTeachingPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A colonist learns the offered skill from the natives (R-0148). The Dutch
 * ordinary scout beside the Aztec capital chooses the teaching action, reads
 * the confirmation with the skill and the four owner updates, and starts the
 * course, which takes the colonist off the map. Four European turns later the
 * skill is granted to the same colonist, which keeps its mounted equipment, and
 * it is back on the map. Backing out changes nothing, a second visitor finds
 * the settlement occupied, and a stopped course grants nothing. The role rules,
 * the stale offer, the hostility cancel and the saved stages are checked in the
 * model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_128_F119_native_teaching_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, learned by the Dutch, offering the expert farmer. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Unarmed mounted colonist, the ordinary scout, west of the capital. */
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 15);

    /** Petty criminal south-west of the capital. */
    private static final int CRIMINAL_ID = 301;
    private static final Location CRIMINAL_AT = Location.of(22, 16);

    /** Armed soldier north of the capital. */
    private static final int SOLDIER_ID = 302;
    private static final Location SOLDIER_AT = Location.of(23, 14);

    private static final String CONFIRMATION_EN = "A Free Colonist learns Expert Farmer after 4 owner updates. Meanwhile the colonist stays with the natives and cannot move, work or fight.";
    private static final String CONFIRMATION_CS = "Jednotka svobodný kolonista se naučí: zkušený farmář (po 4 aktualizacích vlastníka). Kolonista mezitím zůstává u domorodců a nemůže se hýbat, pracovat ani bojovat.";
    private static final String TEACHING_AVAILABLE_EN = "Learn the offered skill: Available now.";
    private static final String OCCUPIED_EN = "Learn the offered skill: Another colonist is already learning here.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    private static String progressEn(final int updates) {
        return "Learning Expert Farmer here: " + updates
                + " of 4 owner updates.";
    }

    @Test
    @Tag("local")
    void TC_01_scout_learns_over_four_turns_and_the_skill_is_granted_once()
            throws Exception {
        // Load the native position with the scout beside the capital.
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();

        // The chooser offers teaching now; the scout accepts it after reading
        // the confirmation with the skill and the four owner updates.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChooser("Offers to teach: Expert Farmer.",
                "Visit the chief: Not available yet.", TEACHING_AVAILABLE_EN,
                "Trade: Your unit's role cannot do this.",
                "Demand tribute: Not available yet.",
                "Establish a mission: Your unit's role cannot do this.",
                "Attack the settlement: Your unit's role cannot do this.");
        visit.verifyTeachButtonShown();
        visit.startTeachingChoice();
        visit.verifyTeachingConfirmation(CONFIRMATION_EN);
        visit.captureTeaching("native-teaching-confirm-en.png");
        visit.confirmTeaching();
        visit.verifyTeachingClosed();
        visit.verifyChooserClosed();
        visit.verifyCourseStarted(SCOUT_ID, CAPITAL);

        // The details name the progress before any owner turn has passed.
        teaching.openSettlements();
        teaching.verifyProgress(progressEn(0));
        teaching.captureSettlements("native-teaching-progress-en.png");
        teaching.closeSettlements();

        // Three turns count the updates; the fourth grants the skill and the
        // colonist is released with its identity and equipment.
        for (int updates = 1; updates < 4; updates++) {
            teaching.nextTurn();
            teaching.verifyLearning(SCOUT_ID, CAPITAL, updates);
        }
        // The report of that turn announces the new skill exactly once.
        final ScreenTurnReport report = teaching.nextTurnWithReport();
        teaching.verifyReportAnnounces(report,
                "A colonist learned Expert Farmer from the natives.");
        teaching.captureReport("native-teaching-outcome-en.png");
        teaching.closeReport(report);
        teaching.verifyLearnedAndReleased(SCOUT_ID, CAPITAL, true);
        fog.saveMapEvidence("native-teaching-map-en.png");
    }

    @Test
    @Tag("local")
    void TC_02_backing_out_of_the_confirmation_changes_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final AttackPage world = new AttackPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();
        world.rememberWorld();
        visit.rememberMovement(SCOUT_ID);

        // Back returns to the chooser, which Escape then closes.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startTeachingChoice();
        visit.verifyTeachingConfirmation(CONFIRMATION_EN);
        visit.backFromTeaching();
        visit.verifyTeachingClosed();
        visit.verifyNoCourse(SCOUT_ID, CAPITAL);
        visit.verifyVisitorUnmoved(SCOUT_ID, SCOUT_AT);
        visit.escape();
        visit.verifyChooserClosed();
        world.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_03_second_visitor_sees_the_settlement_is_occupied()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();

        // The scout starts the course; the soldier then finds the one pupil
        // place taken and cannot start a second course.
        visit.rememberMovement(SCOUT_ID);
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startTeachingChoice();
        visit.confirmTeaching();
        visit.verifyTeachingClosed();
        visit.verifyCourseStarted(SCOUT_ID, CAPITAL);

        visit.rememberMovement(SOLDIER_ID);
        visit.enterSettlement(SOLDIER_AT, CAPITAL);
        visit.verifyChooser("Offers to teach: Expert Farmer.",
                "Visit the chief: Your unit's role cannot do this.",
                OCCUPIED_EN, "Trade: Your unit's role cannot do this.",
                "Demand tribute: Not available yet.",
                "Establish a mission: Your unit's role cannot do this.",
                "Attack the settlement: Not available yet.");
        visit.verifyTeachButtonHidden();
        visit.escape();
        visit.verifyChooserClosed();
        visit.verifyVisitorUnmoved(SOLDIER_ID, SOLDIER_AT);
    }

    @Test
    @Tag("local")
    void TC_04_continue_mid_course_does_not_advance_and_stop_grants_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        final NativeCombatPage combat = new NativeCombatPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startTeachingChoice();
        visit.confirmTeaching();
        visit.verifyCourseStarted(SCOUT_ID, CAPITAL);

        // Save and Continue mid-course: the same course, no owner update
        // counted.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        teaching.verifyLearning(SCOUT_ID, CAPITAL, 0);

        // The stop control ends the course at once and grants no skill, not
        // even after a later European turn.
        teaching.openSettlements();
        teaching.verifyProgress(progressEn(0));
        teaching.stopLearning();
        teaching.verifyStoppedWithoutSkill(SCOUT_ID, CAPITAL);
        teaching.nextTurn();
        teaching.verifyStoppedWithoutSkill(SCOUT_ID, CAPITAL);
        combat.verifyRelationOutcomeCount(0);
    }

    @Test
    @Tag("local")
    void TC_05_czech_confirmation_and_progress_of_the_learning_course()
            throws Exception {
        // Switch to Czech, load the native position and the initial fixture.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadFixture(FIXTURE, SCOUT_ID, SCOUT_AT, CRIMINAL_ID, CRIMINAL_AT,
                SOLDIER_ID, SOLDIER_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startTeachingChoice();
        visit.verifyTeachingConfirmation(CONFIRMATION_CS);
        visit.captureTeaching("native-teaching-confirm-cs.png");
        visit.confirmTeaching();
        visit.verifyCourseStarted(SCOUT_ID, CAPITAL);

        teaching.openSettlements();
        teaching.verifyProgress(
                "Učení: zkušený farmář zde, 0 z 4 aktualizací vlastníka.");
        teaching.captureSettlements("native-teaching-progress-cs.png");
    }

}
