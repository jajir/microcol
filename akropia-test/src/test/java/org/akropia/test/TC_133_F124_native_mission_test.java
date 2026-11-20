package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.page.FogPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.NativeMissionPage;
import org.akropia.page.NativeVisitPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * One-shot missionaries: confirmation, cancellation, competition and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_133_F124_native_mission_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.akropia");
    private static final Location CAPITAL = Location.of(23, 15);
    private static final int MISSIONARY_ID = 310;
    private static final Location MISSIONARY_AT = Location.of(22, 14);
    private static final int SECOND_ID = 311;
    private static final Location SECOND_AT = Location.of(23, 14);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_confirmed_influence_and_consumption_survive_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        new FogPage(getContext()).centerFrontier();
        // Visiting opens the one-shot offer and explains the exact score
        // changes.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.verifyMissionConfirmation("The missionary is consumed");
        mission.verifyMissionConfirmation("Corinth: 5 → 6");
        mission.verifyMissionFits(900, 750);
        mission.captureMission("native-mission-en.png");
        // Back preserves the person and the scores.
        mission.backFromMission();
        mission.verifyMissionNotEstablished(MISSIONARY_ID, CAPITAL);
        visit.verifyChooserClosed();
        // Confirm consumes the person once; saving cannot repeat the influence.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.confirmMission();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 6);
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 6);
    }

    @Test
    @Tag("local")
    void TC_02_another_missionary_can_influence_the_same_nation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        new FogPage(getContext()).centerFrontier();
        // The first visit has no permanent resident or occupied-mission lock.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.confirmMission();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 6);
        // A subsequent visitor sees the updated score and can act normally.
        visit.enterSettlement(SECOND_AT, CAPITAL);
        mission.verifyMissionConfirmation("Corinth: 6 → 7");
        mission.confirmMission();
        mission.verifyMissionApplied(SECOND_ID, CAPITAL, 7);
    }

    @Test
    @Tag("local")
    void TC_03_expert_changes_favour_by_two() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadMissionFixture(FIXTURE, "JESUIT_MISSIONARY", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        new FogPage(getContext()).centerFrontier();
        // Expertise affects the same one-shot action.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.verifyMissionConfirmation("Corinth: 5 → 7");
        mission.confirmMission();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 7);
    }

    @Test
    @Tag("local")
    void TC_04_expert_wins_a_rivals_friend_and_continue_keeps_the_new_friend()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadRivalFriendFixture(FIXTURE, MISSIONARY_ID, MISSIONARY_AT,
                SECOND_ID, SECOND_AT);
        new FogPage(getContext()).centerFrontier();
        final org.akropia.page.NativeAdvisorPage advisor = org.akropia.page.NativeAdvisorPage
                .of(getContext());
        // The tribe initially cooperates with the rival.
        advisor.openWithNKey();
        advisor.verifyFriend("Friend: Phocaea");
        advisor.closeWithEscape();
        // A one-shot expert changes national favour and is consumed.
        new NativeVisitPage(getContext()).enterSettlement(MISSIONARY_AT,
                CAPITAL);
        mission.verifyMissionConfirmation("Corinth: 7 → 9");
        mission.verifyMissionConfirmation("Phocaea: 8 → 6");
        mission.confirmMission();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 9);
        advisor.openWithNKey();
        advisor.verifyFriend("Friend: Corinth");
        advisor.closeWithEscape();
        // Save and Continue preserve the newly won friendship.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        advisor.openWithNKey();
        advisor.verifyFriend("Friend: Corinth");
        advisor.closeWithEscape();
        mission.verifyMissionApplied(MISSIONARY_ID, CAPITAL, 9);
    }

    @Test
    @Tag("local")
    void TC_05_czech_confirmation_explains_consumption_and_can_be_canceled()
            throws Exception {
        final org.akropia.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        new FogPage(getContext()).centerFrontier();
        // The Czech dialog states the permanent cost before confirmation.
        new NativeVisitPage(getContext()).enterSettlement(MISSIONARY_AT,
                CAPITAL);
        mission.verifyMissionConfirmation(
                "Misionář se při návštěvě spotřebuje.");
        mission.verifyMissionFits(900, 750);
        mission.captureMission("native-mission-cs.png");
        mission.backFromMission();
        mission.verifyMissionNotEstablished(MISSIONARY_ID, CAPITAL);
        new NativeVisitPage(getContext()).verifyChooserClosed();
    }
}
