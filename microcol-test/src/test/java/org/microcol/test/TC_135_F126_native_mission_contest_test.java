package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeAlarmPage;
import org.microcol.page.NativeContestPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Challenging a rival mission from the chooser (R-0165). A Dutch ordinary
 * missionary sees the French mission it may contest, with the rival named and
 * the exact chance, before anything is drawn. Back leaves both missionaries and
 * the mission as they were. A won contest puts the Dutch missionary into the
 * mission with its clock at zero and removes only the French one; a lost
 * contest removes only the Dutch missionary and keeps the French mission with
 * its clock. Save and Continue keep each outcome. The model rules, the
 * strengths, the expertise, the stale refusals and the single draw are checked
 * in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_135_F126_native_mission_contest_test
        extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native mission tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital where the rival mission is held. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Dutch ordinary missionary, land square north-west of the capital. */
    private static final int VISITOR_ID = 310;
    private static final Location VISITOR_AT = Location.of(22, 14);

    /** French missionary that holds the capital's mission. */
    private static final int RESIDENT_ID = 311;

    /** Nonhostile updates the French mission has counted before the contest. */
    private static final int RESIDENT_UPDATES = 3;

    /**
     * Both sides have angry natives, so each strength is 1.5 and the chance
     * 50%.
     */
    private static final double CHANCE = 0.5;

    private static final String LIMIT_EN = "A rival mission holds this settlement; you may challenge it.";

    private static final String CONFIRMATION_EN = "Rival mission of France: its missionary is ordinary, and the natives are angry toward them. Your missionary is ordinary, and the natives are angry toward you. Your chance: 50%.";

    private static final String WON_EN = "Your missionary won. The rival missionary died, and yours now holds the mission.";

    private static final String LOST_EN = "Your missionary lost and died. The rival mission stays.";

    private static final String WINNER_SUMMARY_EN = "Mission of Dutch: ordinary missionary. The natives are angry. Progress: 0 of 5 updates.";

    private static final String HELD_SUMMARY_EN = "Mission of France: ordinary missionary. The natives are angry. Progress: 3 of 5 updates.";

    private static final String LIMIT_CS = "Na tomto místě působí misie rivala; můžete ji vyzvat.";

    private static final String CHANCE_CS = "Vaše šance: 50 %";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_a_won_challenge_puts_the_visitor_into_the_mission_and_keeps_it_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeContestPage contest = new NativeContestPage(getContext());
        final FogPage fog = new FogPage(getContext());
        contest.loadContestFixture(FIXTURE,
                NativeContestPage.stateWithOutcome(true, CHANCE),
                RESIDENT_UPDATES);
        fog.centerFrontier();

        // The chooser names the rival mission as a challenge; no establishment
        // is offered beside it.
        visit.enterSettlement(VISITOR_AT, CAPITAL);
        contest.verifyContestButtonShown();
        contest.verifyMissionButtonHidden();
        contest.verifyContestChoiceLimit(LIMIT_EN);
        visit.verifyChooserFits(900, 750);

        // The confirmation names the rival and the exact chance before anything
        // is drawn.
        contest.startContestChoice();
        visit.verifyChooserClosed();
        contest.verifyContestConfirmation(CONFIRMATION_EN);
        contest.verifyContestFits(900, 750);
        contest.captureContest("native-contest-en.png");

        // Confirm: the model draws once, the visitor takes the mission at zero
        // and the French missionary is removed.
        contest.confirmContest();
        contest.verifyContestResultMessage(WON_EN);
        contest.closeContestResult();
        contest.verifyMissionaryDied(RESIDENT_ID);
        contest.verifyMissionHeldBy(VISITOR_ID, CAPITAL, 0);

        // The settlement details name the winner, not the rival.
        NativeAlarmPage.of(getContext()).openWithNKey();
        contest.verifySettlementMission(WINNER_SUMMARY_EN);
        NativeAlarmPage.of(getContext()).closeWithEscape();

        // Save and Continue keep the winner and do not bring the rival back.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        contest.verifyMissionHeldBy(VISITOR_ID, CAPITAL, 0);
        contest.verifyMissionaryDied(RESIDENT_ID);
    }

    @Test
    @Tag("local")
    void TC_02_a_lost_challenge_removes_only_the_visitor_and_keeps_the_rival_mission_and_clock()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeContestPage contest = new NativeContestPage(getContext());
        final FogPage fog = new FogPage(getContext());
        contest.loadContestFixture(FIXTURE,
                NativeContestPage.stateWithOutcome(false, CHANCE),
                RESIDENT_UPDATES);
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        contest.startContestChoice();
        visit.verifyChooserClosed();
        contest.confirmContest();
        contest.verifyContestResultMessage(LOST_EN);
        contest.closeContestResult();

        // The Dutch missionary died; the French mission stays with its clock.
        contest.verifyMissionaryDied(VISITOR_ID);
        contest.verifyMissionHeldBy(RESIDENT_ID, CAPITAL, RESIDENT_UPDATES);

        // Save and Continue keep the loss; the mission keeps its count.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        contest.verifyMissionaryDied(VISITOR_ID);
        contest.verifyMissionHeldBy(RESIDENT_ID, CAPITAL, RESIDENT_UPDATES);
        NativeAlarmPage.of(getContext()).openWithNKey();
        contest.verifySettlementMission(HELD_SUMMARY_EN);
        NativeAlarmPage.of(getContext()).closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_03_back_from_the_challenge_changes_nothing_and_draws_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeContestPage contest = new NativeContestPage(getContext());
        final FogPage fog = new FogPage(getContext());
        final long state = NativeContestPage.stateWithOutcome(true, CHANCE);
        contest.loadContestFixture(FIXTURE, state, RESIDENT_UPDATES);
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        contest.startContestChoice();
        visit.verifyChooserClosed();
        contest.verifyContestConfirmation(CONFIRMATION_EN);
        contest.backFromContest();

        // Back leaves both missionaries, the mission and the random state as
        // they were; the chooser returns with the same challenge.
        contest.verifyContestNotStarted(VISITOR_ID, RESIDENT_ID, CAPITAL,
                RESIDENT_UPDATES);
        contest.verifyCombatRandomState(state);
        contest.verifyContestButtonShown();
        visit.escape();
        visit.verifyChooserClosed();
        contest.verifyContestNotStarted(VISITOR_ID, RESIDENT_ID, CAPITAL,
                RESIDENT_UPDATES);
        contest.verifyCombatRandomState(state);
    }

    @Test
    @Tag("local")
    void TC_04_czech_challenge_chooser_and_confirmation_fit() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeContestPage contest = new NativeContestPage(getContext());
        final FogPage fog = new FogPage(getContext());
        contest.loadContestFixture(FIXTURE,
                NativeContestPage.stateWithOutcome(true, CHANCE),
                RESIDENT_UPDATES);
        fog.centerFrontier();

        visit.enterSettlement(VISITOR_AT, CAPITAL);
        visit.verifyChooserFits(900, 750);
        contest.verifyContestChoiceLimit(LIMIT_CS);
        contest.startContestChoice();
        contest.verifyContestConfirmation(CHANCE_CS);
        contest.verifyContestFits(900, 750);
        contest.captureContest("native-contest-cs.png");
        contest.backFromContest();
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-contest-cs-chooser.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

}
