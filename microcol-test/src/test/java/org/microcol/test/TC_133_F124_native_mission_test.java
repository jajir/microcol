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
import org.microcol.page.NativeMissionPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Establishing a native mission from the chooser (R-0164). An ordinary
 * missionary reads its owner, role and the natives' attitude in the
 * confirmation before anything changes; Back leaves the missionary on the map,
 * while Confirm stations it at the settlement, where the settlement details
 * keep the mission. A second missionary is refused by an occupied settlement,
 * and a Jesuit reads as an expert. Each owner update advances the progress
 * once, and Save and Continue keep it. The model rules, the exact alarm effects
 * and the saved associations are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_133_F124_native_mission_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native chief tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital where the missionaries establish their missions. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Ordinary missionary, land square north-west of the capital. */
    private static final int MISSIONARY_ID = 310;
    private static final Location MISSIONARY_AT = Location.of(22, 14);

    /** Second ordinary missionary, land square north of the capital. */
    private static final int SECOND_ID = 311;
    private static final Location SECOND_AT = Location.of(23, 14);

    private static final String ORDINARY_CONTENT_EN = "Missionary: ordinary. The natives are content. Progress: 0 of 5 updates.";

    private static final String EXPERT_CONTENT_EN = "Missionary: expert. The natives are content. Progress: 0 of 5 updates.";

    private static final String SUMMARY_EN = "ordinary missionary. The natives are content. Progress: 0 of 5 updates.";

    private static final String PROGRESS_ONE_EN = "Progress: 1 of 5 updates.";

    private static final String OCCUPIED_EN = "A mission is already established here.";

    private static final String ORDINARY_CS = "Misionář: běžný.";

    private static final String PROGRESS_CS = "Pokrok: 0 z 5 kol.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_establishment_is_confirmed_and_the_settlement_keeps_the_mission_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        final FogPage fog = new FogPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        fog.centerFrontier();

        // The ordinary missionary reads its owner, role and the natives'
        // attitude
        // before anything is established.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.verifyMissionButtonShown();
        mission.startMissionChoice();
        mission.verifyMissionConfirmation(ORDINARY_CONTENT_EN);
        mission.verifyMissionFits(900, 750);
        mission.captureMission("native-mission-en.png");

        // Back leaves the missionary on the map and the settlement without a
        // mission.
        mission.backFromMission();
        mission.verifyMissionNotEstablished(MISSIONARY_ID, CAPITAL);
        visit.escape();
        visit.verifyChooserClosed();

        // Confirm stations the missionary at the settlement; the chooser
        // closes.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.startMissionChoice();
        mission.confirmMission();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 0);
        visit.verifyChooserClosed();

        // The settlement details keep the mission after the missionary left the
        // map.
        NativeAlarmPage.of(getContext()).openWithNKey();
        mission.verifySettlementMission(SUMMARY_EN);
        NativeAlarmPage.of(getContext()).closeWithEscape();

        // Save and Continue keep the mission and the missionary's identity.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 0);
    }

    @Test
    @Tag("local")
    void TC_02_an_occupied_settlement_refuses_a_second_mission_and_cancel_changes_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        final FogPage fog = new FogPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        fog.centerFrontier();

        // Cancelling the first confirmation changes nothing.
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.startMissionChoice();
        mission.verifyMissionConfirmation(ORDINARY_CONTENT_EN);
        mission.backFromMission();
        mission.verifyMissionNotEstablished(MISSIONARY_ID, CAPITAL);
        visit.escape();
        visit.verifyChooserClosed();

        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.startMissionChoice();
        mission.confirmMission();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 0);
        visit.verifyChooserClosed();

        // The second missionary finds the settlement occupied: no mission
        // button,
        // the reason is named, and the first mission is not replaced.
        visit.enterSettlement(SECOND_AT, CAPITAL);
        mission.verifyMissionButtonHidden();
        mission.verifyMissionChoiceLimit(OCCUPIED_EN);
        visit.escape();
        visit.verifyChooserClosed();
        mission.verifyMissionHeldBy(MISSIONARY_ID, CAPITAL);
        mission.verifyStandsOnMap(SECOND_ID);
    }

    @Test
    @Tag("local")
    void TC_03_a_jesuit_reads_as_an_expert_and_each_owner_update_counts_once_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        final FogPage fog = new FogPage(getContext());
        mission.loadMissionFixture(FIXTURE, "JESUIT_MISSIONARY", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        fog.centerFrontier();

        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.startMissionChoice();
        mission.verifyMissionConfirmation(EXPERT_CONTENT_EN);
        mission.confirmMission();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 0);
        visit.verifyChooserClosed();

        // The Dutch turn comes back once: the progress advances by one.
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 1);
        NativeAlarmPage.of(getContext()).openWithNKey();
        mission.verifySettlementMission(PROGRESS_ONE_EN);
        NativeAlarmPage.of(getContext()).closeWithEscape();

        // Save and Continue do not advance the progress again.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 1);
    }

    @Test
    @Tag("local")
    void TC_04_czech_mission_confirmation_and_chooser_fit() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        final FogPage fog = new FogPage(getContext());
        mission.loadMissionFixture(FIXTURE, "COLONIST", MISSIONARY_ID,
                MISSIONARY_AT, SECOND_ID, SECOND_AT);
        fog.centerFrontier();

        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        visit.verifyChooserFits(900, 750);
        mission.startMissionChoice();
        mission.verifyMissionConfirmation(ORDINARY_CS);
        mission.verifyMissionConfirmation(PROGRESS_CS);
        mission.verifyMissionFits(900, 750);
        mission.captureMission("native-mission-cs.png");
        mission.backFromMission();
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-mission-cs-chooser.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

}
