package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.ColonyJobChoice.Kind;
import org.akropia.model.ConstructionType;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.ColonyJobPage;
import org.akropia.page.ColonyScreen;
import org.akropia.page.FogPage;
import org.akropia.page.FortifiedPopulationPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.NativeConvertPage;
import org.akropia.page.NativeMissionPage;
import org.akropia.page.NativeVisitPage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Native convert acquisition, work and expiry (R-0149). A mission's fifth
 * nonhostile update, under a saved randomness whose coming draws all succeed,
 * delivers one convert to the owned colony, and Continue keeps it. A convert
 * works a field at one more than an ordinary colonist; a brief building job
 * pauses its unassigned count without resetting it, and leaving the job resumes
 * it across Continue. A convert that stays unassigned counts each owner update
 * until the eighth, when it returns to its tribe once. A manufacturing building
 * and the build key refuse a convert without any change. Labels and reports are
 * checked in English and Czech.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_134_F125_native_convert_test extends AbstractAkropiaTest {

    /** Current-schema scenario shared with the native mission tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.akropia");

    /** Colony scenario of the job choices, with its colony New Amsterdam. */
    private static final File COLONY_FIXTURE = new File(
            "src/test/scenarios/F32-colony-job-choices.akropia");

    /** Founding scenario with two colonists on the map at the same square. */
    private static final File FOUNDING_FIXTURE = new File(
            "src/test/scenarios/F02-founding-colony.akropia");

    private static final Location FOUNDING_AT = Location.of(19, 11);

    /** The Aztec capital where the missionary establishes its mission. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Ordinary missionary, land square north-west of the capital. */
    private static final int MISSIONARY_ID = 310;
    private static final Location MISSIONARY_AT = Location.of(22, 14);

    /**
     * Other events may take draws of the saved stream before the conversion;
     * several successes in a row keep the acquisition deterministic whichever
     * of them come first.
     */
    private static final long CONVERT_STATE = NativeConvertPage
            .stateWithSuccessfulDraws(0.10, 7);

    /** The colony nearest to the capital by legal land route. */
    private static final Location NEAREST_COLONY = Location.of(24, 16);

    /** Existing convert standing on the map beside the capital. */
    private static final int CONVERT_ID = 320;

    /**
     * Convert standing at the centre of New Amsterdam in the colony scenario.
     */
    private static final int WORKER_ID = 500;

    private static final Location COLONY = Location.of(24, 16);

    private static final String COLONY_NAME = "New Amsterdam";

    private static final String ACQUIRED_EN = "A native convert joined your colony";

    private static final String RETURNED_EN = "A native convert returned to the local people after eight turns";

    private static final String LABEL_0_EN = "Local convert of the Egyptians.";

    private static final String LABEL_7_EN = "Local convert of the Egyptians.";

    private static final String LABEL_1_CS = "Místní konvertita: Egypťané.";

    private static final String ACQUIRED_CS = "Místní konvertita se připojil ke kolonii";

    private static final String RETURNED_CS = "Místní konvertita se po osmi tazích bez práce vrátil ke svému národu";

    private static final String BEST_LUMBER_EN = "Best eligible field: 8 Lumber per turn";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_saved_friendship_arrival_delivers_a_convert_that_keeps_identity_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadFriendshipConvertFixture(FIXTURE);
        new FogPage(getContext()).centerFrontier();

        // Four nonhostile updates advance the mission; the fifth one converts.
        final GamePage game = GamePage.of(getContext());
        for (int turn = 1; turn <= 4; turn++) {
            game.nextTurnAndCloseDialogs();
        }
        final ScreenTurnReport report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(ACQUIRED_EN);
        convert.captureReport("native-convert-acquired-en.png");
        report.close();
        convert.verifyConvertAcquired("AZTEC", 0, NEAREST_COLONY);

        // Save and Continue keep the convert and its native identity.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        convert.verifyConvertAcquired("AZTEC", 0, NEAREST_COLONY);
    }

    @Test
    @Tag("local")
    void TC_02_an_unassigned_convert_remains_after_eight_turns_and_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertFixture(FIXTURE, MISSIONARY_AT, CONVERT_ID, 0);
        new FogPage(getContext()).centerFrontier();
        // The unit remains affiliated without a departure countdown.
        convert.verifyVisibleConvertLabel(LABEL_0_EN);
        final GamePage game = GamePage.of(getContext());
        for (int turn = 0; turn < 8; turn++) {
            game.nextTurnAndCloseDialogs();
        }
        convert.verifyUnassignedUpdates(0);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        convert.verifyUnassignedUpdates(0);
        convert.verifyVisibleConvertLabel(LABEL_0_EN);
    }

    @Test
    @Tag("local")
    void TC_03_czech_convert_label_names_the_nation() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertFixture(FIXTURE, MISSIONARY_AT, CONVERT_ID, 0);
        new FogPage(getContext()).centerFrontier();
        GamePage.of(getContext()).nextTurnAndCloseDialogs();
        convert.verifyUnassignedUpdates(0);
        convert.verifyVisibleConvertLabel(LABEL_1_CS);
    }

    @Test
    @Tag("local")
    void TC_04_a_convert_keeps_field_bonus_and_building_job_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertFixture(COLONY_FIXTURE, COLONY, WORKER_ID, 3);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // The road gives seven lumber to an ordinary colonist; the convert
        // produces one more on the same field.
        jobs.assignField(WORKER_ID, Location.of(23, 15), GoodsType.LUMBER);
        jobs.verifyFieldJob(WORKER_ID, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 8));

        // A brief building job keeps the work non-manufacturing: the town hall.
        jobs.assignIndoorFromField(WORKER_ID, "id_town_hall");
        jobs.verifyIndoorJob(WORKER_ID, ConstructionType.TOWN_HALL, 1);
        final GamePage game = ColonyScreen.of(getContext(), COLONY_NAME)
                .close();

        // Two owner updates while assigned pause the count at three.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        convert.verifyUnassignedUpdates(3);

        // Leaving the job resumes the same count: the next update makes four.
        game.openColonyAt(COLONY, COLONY_NAME);
        final FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        people.dragIndoorOutside("id_town_hall", 1);
        people.verifyOutside(WORKER_ID);
        ColonyScreen.of(getContext(), COLONY_NAME).close();
        game.nextTurnAndCloseDialogs();
        convert.verifyUnassignedUpdates(3);

        // Save and Continue keep the same person, affiliation and count.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        convert.verifyUnassignedUpdates(3);
        convert.verifyUnassignedAtColony(WORKER_ID, "AZTEC", COLONY);
    }

    @Test
    @Tag("local")
    void TC_05_a_manufacturing_building_refuses_a_convert_without_any_change()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertFixture(COLONY_FIXTURE, COLONY, WORKER_ID, 0);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();

        // Manufacturing is absent from the current-state work menu.
        jobs.openFromCenter(WORKER_ID);
        jobs.verifyJobNotOffered(Kind.INDOOR, GoodsType.TOOLS);
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();

        // Dragging the convert onto the blacksmith is not accepted either.
        convert.dragCenterToBuildingExpectingRefusal(WORKER_ID,
                "id_blacksmith");
        convert.verifyUnassignedAtColony(WORKER_ID, "AZTEC", COLONY);
        jobs.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_06_the_build_key_does_not_found_a_colony_with_a_convert()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertColonistsFixture(FOUNDING_FIXTURE);
        final GamePage game = GamePage.of(getContext());
        game.panToTile(FOUNDING_AT);
        game.selectTile(FOUNDING_AT);
        game.verifyNumberOfUnitInRightPanel(2);

        // The same square lets an ordinary colonist found Leiden in the
        // founding scenario; a convert leaves no build option and no colony.
        game.getRightPanelUnit(0).selectUnit();
        game.verifyThatBuildColonyButtonIsHidden();
        convert.pressBuildColony();
        game.verifyThatThereIsNoColonyAt(FOUNDING_AT);
        game.verifyNumberOfUnitInRightPanel(2);
    }

    @Test
    @Tag("local")
    void TC_07_czech_acquisition_report_names_the_convert() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadFriendshipConvertFixture(FIXTURE);
        new FogPage(getContext()).centerFrontier();

        final GamePage game = GamePage.of(getContext());
        for (int turn = 1; turn <= 4; turn++) {
            game.nextTurnAndCloseDialogs();
        }
        final ScreenTurnReport report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(ACQUIRED_CS);
        convert.captureReport("native-convert-acquired-cs.png");
        report.close();
        convert.verifyConvertAcquired("AZTEC", 0, NEAREST_COLONY);
    }

    @Test
    @Tag("local")
    void TC_09_czech_menu_preserves_the_manufacturing_ban() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        convert.loadConvertFixture(COLONY_FIXTURE, COLONY, WORKER_ID, 0);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();

        jobs.openFromCenter(WORKER_ID);
        jobs.verifyJobNotOffered(Kind.INDOOR, GoodsType.TOOLS);
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        convert.dragCenterToBuildingExpectingRefusal(WORKER_ID,
                "id_blacksmith");
        convert.verifyUnassignedAtColony(WORKER_ID, "AZTEC", COLONY);
        jobs.verifyWorldUnchanged();
    }

}
