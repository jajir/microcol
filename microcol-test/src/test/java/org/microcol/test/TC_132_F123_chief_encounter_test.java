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
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The chief of a native capital, visited by a scout (R-0158). Each scenario
 * starts from a fixture whose saved random state gives the named first draw, so
 * the outcome is explicit. The mood and the exact risk are shown before the
 * draw, the reported result is recorded for the Dutch nation, and information
 * marks the land within two squares of the capital on the Dutch map. A repeat
 * visit by the seasoned scout, or after Save and Continue, repeats the
 * information without a draw. The model rules for each distribution, the gold
 * caps, the death cleanup, the map delta and the stale terms are checked in the
 * model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_132_F123_chief_encounter_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native market tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital; its treasury caps the gold at two hundred. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** The gold cap of a capital, as the model applies it. */
    private static final int CAPITAL_GOLD = 200;

    /**
     * Ordinary scout, land square north-west of the capital. The colony New
     * Amsterdam (24,16), the Dutch frigate (25,16) with sight two and the Aztec
     * braves block the east side, and their sight already explores the land
     * east and south-east of the capital, so both scouts stand on the north
     * side: their sight of two squares ends at row 16, and the squares below
     * stay unexplored until the chief tells about them.
     */
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 14);

    /** Seasoned scout, land square north of the capital. */
    private static final int SEASONED_ID = 303;
    private static final Location SEASONED_AT = Location.of(23, 14);

    /**
     * Land square two from the capital (row 17) that no Dutch unit or colony
     * sees at the start: the scouts see to row 16, the colony and the frigate
     * start at column 23. It is unexplored until the information reveals it.
     */
    private static final Location FAR_LAND = Location.of(22, 17);

    /** Land square three from the capital (row 18): never revealed. */
    private static final Location UNKNOWN_LAND = Location.of(22, 18);

    private static final String CONTENT_EN = "The natives are content. Chances: information 70.0%, gold 25.0%, death 5.0%.";

    private static final String REPEAT_EN = "The natives are content. This nation has met their chief before, so you learn the same information again. Nothing is drawn, and no gold or risk applies.";

    private static final String CONTENT_CS = "Domorodci jsou spokojení. Šance: informace 70.0%, zlato 25.0%, smrt 5.0%.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_information_marks_the_land_and_a_repeat_visit_by_another_scout_draws_nothing_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        fog.centerFrontier();
        visit.verifyLandExplored(FAR_LAND, false);
        visit.verifyLandExplored(UNKNOWN_LAND, false);

        // The ordinary scout reads the mood and the exact risk before any draw.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChiefButtonShown();
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        visit.captureChief("native-chief-en.png");
        final int treasury = visit.treasury(CAPITAL);
        final int gold = visit.gold();

        // The first draw is information: the report names it, and the land
        // within two squares becomes explored, while the land beyond stays
        // unknown.
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.INFORMATION, 0,
                false);
        visit.closeChiefResult();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.INFORMATION);
        visit.verifyChiefGold(CAPITAL, treasury, gold, 0);
        visit.verifyLandExplored(FAR_LAND, true);
        visit.verifyLandExplored(UNKNOWN_LAND, false);
        visit.verifyChiefScoutAlive(SCOUT_ID, SCOUT_AT);
        visit.escape();
        visit.verifyChooserClosed();

        // A repeat visit by the seasoned scout repeats the information only.
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(REPEAT_EN);
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.INFORMATION, 0, true);
        visit.closeChiefResult();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.INFORMATION);
        visit.verifyChiefGold(CAPITAL, treasury, gold, 0);
        visit.verifyLandExplored(FAR_LAND, true);
        visit.verifyLandExplored(UNKNOWN_LAND, false);
        visit.escape();
        visit.verifyChooserClosed();

        // Save and Continue keep the record and the explored land.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.INFORMATION);
        visit.verifyLandExplored(FAR_LAND, true);
        visit.verifyLandExplored(UNKNOWN_LAND, false);
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(REPEAT_EN);
        visit.backFromChief();
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_02_gold_pays_the_capital_cap_once_and_creates_no_money()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.GOLD);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        final int treasury = visit.treasury(CAPITAL);
        final int gold = visit.gold();
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.GOLD, CAPITAL_GOLD,
                false);
        visit.closeChiefResult();

        // The capital pays two hundred once: the treasury loses exactly what
        // the player gains, and the land stays unknown.
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.GOLD);
        visit.verifyChiefGold(CAPITAL, treasury, gold, CAPITAL_GOLD);
        visit.verifyLandExplored(FAR_LAND, false);
        visit.verifyChiefScoutAlive(SCOUT_ID, SCOUT_AT);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_03_death_removes_the_scout_and_keeps_the_record_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.DEATH);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        final int treasury = visit.treasury(CAPITAL);
        final int gold = visit.gold();
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.DEATH, 0, false);
        visit.closeChiefResult();

        // The scout is gone, nothing is paid and the land stays unknown.
        visit.verifyChiefScoutLost(SCOUT_ID);
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.DEATH);
        visit.verifyChiefGold(CAPITAL, treasury, gold, 0);
        visit.verifyLandExplored(FAR_LAND, false);
        visit.verifyChooserClosed();

        // After Save and Continue the scout stays lost, and the seasoned scout
        // repeats the information without a new draw.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.verifyChiefScoutLost(SCOUT_ID);
        visit.verifyChiefRecorded(CAPITAL, NativeChiefOutcome.DEATH);
        visit.enterSettlement(SEASONED_AT, CAPITAL);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(REPEAT_EN);
        visit.confirmChief();
        visit.verifyChiefResultMessage(NativeChiefOutcome.INFORMATION, 0, true);
        visit.closeChiefResult();
        visit.verifyChiefGold(CAPITAL, treasury, gold, 0);
        visit.verifyLandExplored(FAR_LAND, true);

        // The seasoned scout survives, so its chooser opens again with the
        // chief still offered; Escape closes it.
        visit.verifyChiefScoutAlive(SEASONED_ID, SEASONED_AT);
        visit.verifyChiefButtonShown();
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_04_cancelled_visit_changes_nothing() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        fog.centerFrontier();

        visit.rememberMovement(SCOUT_ID);
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        final int treasury = visit.treasury(CAPITAL);
        final int gold = visit.gold();
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_EN);
        visit.backFromChief();

        // Nothing is recorded, paid, explored or moved, and the scout keeps its
        // movement.
        visit.verifyChiefButtonShown();
        visit.verifyChiefNotRecorded(CAPITAL);
        visit.verifyChiefGold(CAPITAL, treasury, gold, 0);
        visit.verifyLandExplored(FAR_LAND, false);
        visit.verifyVisitorUnmoved(SCOUT_ID, SCOUT_AT);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_05_czech_confirmation_and_chooser_fit() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        visit.loadChiefFixture(FIXTURE, SCOUT_ID, SCOUT_AT, SEASONED_ID,
                SEASONED_AT, NativeChiefOutcome.INFORMATION);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        visit.verifyChooserFits(900, 750);
        visit.startChiefChoice();
        visit.verifyChiefConfirmation(CONTENT_CS);
        visit.captureChief("native-chief-cs.png");
        visit.backFromChief();
        visit.verifyChooserFits(900, 750);
        visit.captureChooser("native-chief-cs-chooser.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

}
