package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.Founder;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.CongressPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeConvertPage;
import org.microcol.page.NativeMissionPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Las Casas's one-time join (R-0182) on the real Congress dialog. The Dutch
 * already hold one native convert on the map. Canceling the founder dialog
 * changes nothing; choosing him is not yet the join. The next turn brings the
 * earned join: the convert becomes a free colonist at the same square, its
 * convert label disappears, and Save and Continue neither replay the notice nor
 * restore the convert. A convert on a colony field keeps its worker and job
 * through the join and Continue, at ordinary output, with the manufacturing
 * refusal lifted. A convert acquired afterwards by a mission stays a convert.
 * English and Czech notices are checked.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_143_F134_las_casas_converts_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native mission tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /**
     * Other events may take draws of the saved stream before the conversion;
     * several successes in a row keep the acquisition deterministic whichever
     * of them come first.
     */
    private static final long CONVERT_STATE = NativeConvertPage
            .stateWithSuccessfulDraws(0.10, 7);

    /** The Aztec capital where the missionary establishes its mission. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Ordinary missionary, land square north-west of the capital. */
    private static final int MISSIONARY_ID = 310;
    private static final Location MISSIONARY_AT = Location.of(22, 14);

    /** The Dutch convert on the map, one square north of the capital. */
    private static final int CONVERT_ID = 320;
    private static final Location CONVERT_AT = Location.of(23, 14);

    /** The colony nearest to the capital by legal land route. */
    private static final Location NEAREST_COLONY = Location.of(24, 16);

    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
            Founder.BARTOLOME_DE_LAS_CASAS, Founder.FERDINAND_MAGELLAN);

    private static final String LABEL_0_EN = "Native convert of the Aztec. Unassigned turns: 0 of 8.";

    private static final String FREED_EN = "Las Casas freed your existing native converts";

    private static final String FREED_CS = "Las Casas osvobodil vaše dosavadní domorodé konvertity";

    private static final String ACQUIRED_EN = "A native convert joined your colony";

    /** Colony scenario of the job choices, with its colony New Amsterdam. */
    private static final File COLONY_FIXTURE = new File(
            "src/test/scenarios/F32-colony-job-choices.microcol");

    private static final Location COLONY = Location.of(24, 16);

    private static final String COLONY_NAME = "New Amsterdam";

    /** The convert of the colony, working the best lumber field. */
    private static final int WORKER_ID = 500;

    private static final Location FIELD_AT = Location.of(23, 15);

    private static final String BEST_LUMBER_EN = "Best eligible field: 7 Lumber per turn";

    private static final String REFUSED_EN = "Unavailable: A native convert cannot work in a manufacturing building.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_las_casas_frees_the_existing_convert_and_a_later_convert_stays_a_convert()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        final CongressPage congress = new CongressPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeMissionPage mission = new NativeMissionPage(getContext());
        convert.loadLasCasasFixture(FIXTURE, CONVERT_STATE, CONVERT_AT,
                CONVERT_ID, MISSIONARY_AT, MISSIONARY_ID);
        new FogPage(getContext()).centerFrontier();
        convert.verifyUnassignedUpdates(0);
        convert.verifyVisibleConvertLabel(LABEL_0_EN);

        // Canceling the founder dialog changes no person or convert.
        congress.rememberWorld();
        congress.open();
        congress.verifyOffer(OFFER);
        congress.select(Founder.BARTOLOME_DE_LAS_CASAS);
        congress.close();
        congress.verifyWorldUnchanged();
        convert.verifyUnassignedUpdates(0);

        // Choosing him is not yet the join; the convert is still a convert.
        congress.open();
        congress.select(Founder.BARTOLOME_DE_LAS_CASAS);
        congress.choose();
        congress.close();
        convert.verifyUnassignedUpdates(0);

        // The next turn brings the earned join and its notice.
        final GamePage game = GamePage.of(getContext());
        final ScreenTurnReport report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(FREED_EN);
        convert.captureReport("las-casas-freed-en.png");
        report.close();
        convert.verifyFreedColonist(CONVERT_ID, CONVERT_AT);
        convert.verifyNoConvertLabel(CONVERT_ID);
        convert.verifyNoConvertHeld();

        // Save and Continue keep the freed person and do not replay the join.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        convert.verifyFreedColonist(CONVERT_ID, CONVERT_AT);
        convert.verifyNoConvertHeld();

        // A convert acquired later by a mission is the only convert held.
        new FogPage(getContext()).centerFrontier();
        visit.enterSettlement(MISSIONARY_AT, CAPITAL);
        mission.startMissionChoice();
        mission.confirmMission();
        mission.verifyMissionEstablished(MISSIONARY_ID, CAPITAL, 0);
        visit.verifyChooserClosed();
        final GamePage afterContinue = GamePage.of(getContext());
        for (int turn = 1; turn <= 4; turn++) {
            afterContinue.nextTurnAndCloseDialogs();
        }
        final ScreenTurnReport acquired = afterContinue.nextTurn()
                .orElseThrow();
        acquired.verifyThatAtLeastOneEventMessageContains(ACQUIRED_EN);
        acquired.close();
        convert.verifyConvertAcquired("AZTEC", 0, NEAREST_COLONY);
        convert.verifyFreedColonist(CONVERT_ID, CONVERT_AT);
    }

    @Test
    @Tag("local")
    void TC_02_czech_arrival_report_names_the_freed_converts()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        final CongressPage congress = new CongressPage(getContext());
        convert.loadLasCasasFixture(FIXTURE, CONVERT_STATE, CONVERT_AT,
                CONVERT_ID, MISSIONARY_AT, MISSIONARY_ID);
        new FogPage(getContext()).centerFrontier();
        congress.open();
        congress.select(Founder.BARTOLOME_DE_LAS_CASAS);
        congress.choose();
        congress.close();

        final ScreenTurnReport report = GamePage.of(getContext()).nextTurn()
                .orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(FREED_CS);
        convert.captureReport("las-casas-freed-cs.png");
        report.close();
        convert.verifyFreedColonist(CONVERT_ID, CONVERT_AT);
        convert.verifyNoConvertLabel(CONVERT_ID);
    }

    @Test
    @Tag("local")
    void TC_03_las_casas_frees_a_field_convert_and_keeps_its_job_and_ordinary_output_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeConvertPage convert = new NativeConvertPage(getContext());
        final CongressPage congress = new CongressPage(getContext());
        convert.loadConvertFounderFixture(COLONY_FIXTURE, COLONY, WORKER_ID);
        final GamePage game = GamePage.of(getContext());
        game.openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // The convert works the best lumber field at one more than an ordinary
        // colonist, and manufacturing stays refused before the join.
        jobs.openFromCenter(WORKER_ID);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails(BEST_LUMBER_EN, true);
        jobs.confirm();
        jobs.verifyFieldJob(WORKER_ID, FIELD_AT, Goods.of(GoodsType.LUMBER, 7));
        jobs.openFromField(WORKER_ID);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails(REFUSED_EN, false);
        jobs.cancel();
        jobs.verifyClosed();
        final GamePage afterColony = ColonyScreen.of(getContext(), COLONY_NAME)
                .close();

        // Choosing Las Casas is not yet the join; the field job is unchanged.
        congress.open();
        congress.select(Founder.BARTOLOME_DE_LAS_CASAS);
        congress.choose();
        congress.close();
        jobs.verifyFieldJob(WORKER_ID, FIELD_AT, Goods.of(GoodsType.LUMBER, 7));

        // The earned join at the next turn frees the convert; its field job
        // stays the same worker, now at ordinary output. The scenario colony
        // starts with no liberty and the join adds no resident, so the update
        // leaves the support bonus and the Tory penalty at zero and the
        // ordinary figure is the six of TC_134.
        final ScreenTurnReport report = afterColony.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(FREED_EN);
        report.close();
        final ColonyScreen colony = afterColony.openColonyAt(COLONY,
                COLONY_NAME);
        jobs.verifyFieldJob(WORKER_ID, FIELD_AT, Goods.of(GoodsType.LUMBER, 6));
        convert.verifyNoConvertHeld();
        jobs.openFromField(WORKER_ID);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyManufacturingOffered(REFUSED_EN);
        jobs.cancel();
        jobs.verifyClosed();

        // Save and Continue keep the same person, field job and ordinary
        // output.
        final GamePage closed = colony.close();
        closed.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                COLONY_NAME);
        jobs.verifyFieldJob(WORKER_ID, FIELD_AT, Goods.of(GoodsType.LUMBER, 6));
        convert.verifyNoConvertHeld();
    }
}
