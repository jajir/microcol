package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyAdvisorPage;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The colony advisor (R-0200) of the Dutch nation on the F58 fixture. New
 * Amsterdam keeps its eight residents in the building slots, so the advisor
 * shows eight colonists. The fixture is changed only in its stock and queue: 70
 * hammers, 25 tools and a blacksmith's shop, which needs 64 hammers and 20
 * tools. The shop therefore completes at the next update whatever the
 * production, and the report shows the 64 hammers and 20 tools it takes. The
 * town-hall worker then takes the cross job, as in TC167, so the chapel gives
 * two crosses in the report. The stock the report projects before the update is
 * the stock the next turn shows. Save and Continue keep the same figures, and
 * Open leads to the colony screen.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_168_F200_colony_advisor_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F58-religious-production.microcol");

    /** New Amsterdam, the colony of the fixture. */
    private static final Location COLONY = Location.of(24, 16);

    private static final int COLONY_ID = 0;

    private static final String COLONY_NAME = "New Amsterdam";

    /** Residents in the building slots of the fixture. */
    private static final int POPULATION = 8;

    /** Good, stock, change, next, output, used, spent or sold. */
    private static final int TABLE_COLUMNS = 7;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_colony_advisor_projection_assignment_continue_and_next_turn()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyAdvisorPage advisor = new ColonyAdvisorPage(getContext());
        advisor.loadProjectFixture();
        game = GamePage.of(getContext());

        // The report shows the residents, the shop that completes at the next
        // update with the cost it takes, and one cross from the chapel.
        advisor.open();
        advisor.verifyTabSelected();
        advisor.verifyColonyHeader(COLONY_ID, COLONY_NAME, POPULATION);
        advisor.verifyProjectCompletes(COLONY_ID);
        advisor.verifyNoUncertainty(COLONY_ID);
        advisor.verifySpent(COLONY_ID, GoodsType.HAMMERS, "-64");
        advisor.verifySpent(COLONY_ID, GoodsType.TOOLS, "-20");
        advisor.verifyOutput(COLONY_ID, GoodsType.CROSS, 1);
        // The report fits the window: sentences wrap in view, headings keep
        // their width, and Open stands beside the colony name.
        advisor.verifyReportInsideWindow();
        advisor.verifySentencesInsideViewport(COLONY_ID);
        advisor.verifyTableHeadingsNotSqueezed(COLONY_ID, TABLE_COLUMNS);
        advisor.verifyOpenVisible(COLONY_ID);
        advisor.captureDialog("coloniesTab", "colony-advisor-english.png");
        game = advisor.closeWithEscape();

        // Keyboard reaches the same tab, and Escape closes it without a change.
        advisor.openWithKeyboard();
        advisor.verifyTabSelectedAndFocused();
        game = advisor.closeWithEscape();

        // The town-hall worker takes the cross job: the chapel is staffed and
        // the report shows two crosses at once.
        final ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        game = colony.close();
        advisor.open();
        advisor.verifyOutput(COLONY_ID, GoodsType.CROSS, 2);
        advisor.verifyProjectCompletes(COLONY_ID);
        // The next stock shown now is what the next update is expected to
        // leave.
        advisor.rememberNext(COLONY_ID, GoodsType.HAMMERS);
        advisor.rememberNext(COLONY_ID, GoodsType.TOOLS);
        advisor.rememberNext(COLONY_ID, GoodsType.CORN);
        game = advisor.closeWithEscape();

        // Save and Continue keep the queued shop and the same projected stock.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        advisor.open();
        advisor.verifyProjectCompletes(COLONY_ID);
        advisor.verifyNextIsRemembered(COLONY_ID, GoodsType.HAMMERS);
        advisor.verifyNextIsRemembered(COLONY_ID, GoodsType.TOOLS);
        advisor.verifyNextIsRemembered(COLONY_ID, GoodsType.CORN);
        game = advisor.closeWithEscape();

        // Open leaves the report for the colony screen, and Escape there
        // returns
        // to the map.
        advisor.open();
        advisor.openColony(COLONY_ID);
        advisor.verifyColonyOpened(COLONY_NAME);
        game = advisor.closeOpenedColony(COLONY_NAME);

        // The next update completes the shop; the stock shown afterwards is the
        // stock the report projected, and no project remains queued.
        game.nextTurnAndCloseDialogs();
        advisor.open();
        advisor.verifyStockIsRememberedNext(COLONY_ID, GoodsType.HAMMERS);
        advisor.verifyStockIsRememberedNext(COLONY_ID, GoodsType.TOOLS);
        advisor.verifyStockIsRememberedNext(COLONY_ID, GoodsType.CORN);
        advisor.verifyNoProject(COLONY_ID);
        game = advisor.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_02_czech_labels_show_the_same_projection_and_capture_the_panel()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonyAdvisorPage advisor = new ColonyAdvisorPage(getContext());
        advisor.loadProjectFixture();
        GamePage game = GamePage.of(getContext());

        // The Czech labels name the same colony, residents and project state.
        advisor.open();
        advisor.verifyCzechColonyHeader(COLONY_ID, COLONY_NAME, POPULATION);
        advisor.verifyCzechProjectCompletes(COLONY_ID);
        // The longer Czech words fit the same way.
        advisor.verifyReportInsideWindow();
        advisor.verifySentencesInsideViewport(COLONY_ID);
        advisor.verifyTableHeadingsNotSqueezed(COLONY_ID, TABLE_COLUMNS);
        advisor.verifyOpenVisible(COLONY_ID);
        advisor.captureDialog("coloniesTab", "colony-advisor-czech.png");
        game = advisor.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_03_famine_shows_unknown_projection_and_exact_stock_until_the_update()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyAdvisorPage advisor = new ColonyAdvisorPage(getContext());
        advisor.loadFamineFixture();
        game = GamePage.of(getContext());

        // The eight indoor residents eat 16 and no corn is stored: the report
        // warns of the shortage and shows question marks where the amounts
        // depend on which colonist is lost.
        advisor.open();
        advisor.verifyColonyHeader(COLONY_ID, COLONY_NAME, POPULATION);
        advisor.verifyFamineWarning(COLONY_ID);
        advisor.verifyUncertaintyExplained(COLONY_ID);
        advisor.verifyUnknownProjection(COLONY_ID, GoodsType.CORN);
        advisor.verifyUnknownProjection(COLONY_ID, GoodsType.HAMMERS);
        advisor.verifyProjectUncertain(COLONY_ID);
        // The stock now stays an exact number.
        advisor.verifyStock(COLONY_ID, GoodsType.CORN, 0);
        advisor.verifyStock(COLONY_ID, GoodsType.HAMMERS, 70);
        advisor.verifyStock(COLONY_ID, GoodsType.TOOLS, 25);
        // The long uncertainty sentence wraps inside the visible report.
        advisor.verifyReportInsideWindow();
        advisor.verifySentencesInsideViewport(COLONY_ID);
        advisor.captureDialog("coloniesTab",
                "colony-advisor-famine-english.png");
        game = advisor.closeWithEscape();

        // The update really loses one colonist, whoever it is, and the shop
        // still completes because its hammers and tools are stored.
        game.nextTurnAndCloseDialogs();
        advisor.open();
        advisor.verifyColonyHeader(COLONY_ID, COLONY_NAME, POPULATION - 1);
        advisor.verifyFamineWarning(COLONY_ID);
        advisor.verifyNoProject(COLONY_ID);
        advisor.verifyStock(COLONY_ID, GoodsType.CORN, 0);
        game = advisor.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_04_czech_famine_shows_the_same_unknown_projection_and_exact_stock()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ColonyAdvisorPage advisor = new ColonyAdvisorPage(getContext());
        advisor.loadFamineFixture();
        GamePage game = GamePage.of(getContext());

        // The Czech labels give the same warning, uncertainty and figures.
        advisor.open();
        advisor.verifyCzechColonyHeader(COLONY_ID, COLONY_NAME, POPULATION);
        advisor.verifyCzechFamineWarning(COLONY_ID);
        advisor.verifyCzechUncertaintyExplained(COLONY_ID);
        advisor.verifyUnknownProjection(COLONY_ID, GoodsType.CORN);
        advisor.verifyUnknownProjection(COLONY_ID, GoodsType.HAMMERS);
        advisor.verifyCzechProjectUncertain(COLONY_ID);
        advisor.verifyStock(COLONY_ID, GoodsType.CORN, 0);
        advisor.verifyStock(COLONY_ID, GoodsType.HAMMERS, 70);
        advisor.verifyStock(COLONY_ID, GoodsType.TOOLS, 25);
        advisor.verifyReportInsideWindow();
        advisor.verifySentencesInsideViewport(COLONY_ID);
        advisor.captureDialog("coloniesTab", "colony-advisor-famine-czech.png");
        game = advisor.closeWithEscape();
    }
}
