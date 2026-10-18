package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.EuropeanRelationsPage;
import org.microcol.page.FortifiedPopulationPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RebelSupportPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-078: colonies keep liberty, the political report shows own national and
 * local rebel support, a completed resident exit applies the population cap and
 * one update credits its bells exactly once.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_75_F67_rebel_support_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F64-la-salle-stockade.microcol");
    private static final Location COLONY = Location.of(24, 16);
    private static final Location DELFT = Location.of(26, 16);
    private static final String COLONY_NAME = "New Amsterdam";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_report_shows_values_and_resident_exit_applies_the_cap()
            throws Exception {
        // Three residents hold 600 liberty, which is full support.
        RebelSupportPage support = new RebelSupportPage(getContext());
        support.prepareFixture(600);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        // Opening the political report only reads the stored values.
        relations.rememberWorld();
        relations.open();
        support.verifyNationalSupport(100, 3);
        support.verifyColonyRow(COLONY_NAME, 3, 600, 100);
        relations.verifyWorldUnchanged();
        game = relations.close();
        // The town hall worker leaves for the outside garrison, so only two
        // residents remain and the cap of 400 removes the excess liberty.
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        people.dragIndoorOutside("id_town_hall", 1);
        support.verifyLiberty(400, 2);
        game = colony.close();
        relations.open();
        support.verifyNationalSupport(100, 2);
        support.verifyColonyRow(COLONY_NAME, 2, 400, 100);
        // Continue restores the same liberty and report.
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        support.verifyLiberty(400, 2);
        relations.open();
        support.verifyNationalSupport(100, 2);
        support.verifyColonyRow(COLONY_NAME, 2, 400, 100);
    }

    @Test
    @Tag("local")
    void TC_02_one_update_credits_bells_once_and_continue_adds_nothing()
            throws Exception {
        // 100 of 600 liberty is 16 percent after rounding down.
        RebelSupportPage support = new RebelSupportPage(getContext());
        support.prepareFixture(100);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        support.verifyNationalSupport(16, 3);
        support.verifyColonyRow(COLONY_NAME, 3, 100, 16);
        game = relations.close();
        // The next update adds the planned town hall bells to liberty once.
        support.rememberLibertyAndPlannedBells();
        game.nextTurnAndCloseDialogs();
        support.verifyLibertyCreditedOnce();
        relations.open();
        support.verifyReportMatchesModel();
        // Reopening the report and Continue do not create more bells.
        support.rememberLibertyAndPlannedBells();
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        support.verifyLibertyUnchanged();
        relations.open();
        support.verifyReportMatchesModel();
    }

    @Test
    @Tag("local")
    void TC_03_resident_moves_between_colonies_and_national_support_is_weighted()
            throws Exception {
        // New Amsterdam: 2 residents, liberty 200. Delft: 8 residents, 400.
        RebelSupportPage support = new RebelSupportPage(getContext());
        support.prepareTransferFixture();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        ConstructionPage work = new ConstructionPage(getContext());
        // The weighted national value is 30 percent, not the 37 percent mean
        // of the 50 and 25 percent colonies.
        relations.open();
        support.verifyNationalSupport(30, 10);
        support.verifyColonyRow(0, COLONY_NAME, 2, 200, 50);
        support.verifyColonyRow(1, "Delft", 8, 400, 25);
        game = relations.close();
        // One New Amsterdam resident leaves work and stays on the map square.
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyTooltip(COLONY_NAME, 2, 200, 50, 1, 1, 8, 0);
        people.dragIndoorOutside("id_town_hall", 1);
        support.verifyResidentOutside(COLONY);
        support.verifyColonyLiberty(0, 200, 1);
        game = colony.close();
        // The resident walks to Delft and takes a carpenter job there.
        support.walkResident(COLONY, DELFT);
        colony = game.openColonyAt(DELFT, "Delft");
        support.verifyColonyTooltip("Delft", 8, 400, 25, 0, 6, 8, 0);
        work.assignCarpenter(17);
        support.verifyResidentWorksAt(1);
        game = colony.close();
        // Liberty stays with each colony; the report shows 1 and 9 residents.
        support.verifyColonyLiberty(0, 200, 1);
        support.verifyColonyLiberty(1, 400, 9);
        relations.open();
        support.verifyNationalSupport(30, 10);
        support.verifyColonyRow(0, COLONY_NAME, 1, 200, 100);
        support.verifyColonyRow(1, "Delft", 9, 400, 22);
        // Continue shows the same values.
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        support.verifyNationalSupport(30, 10);
        support.verifyColonyRow(0, COLONY_NAME, 1, 200, 100);
        support.verifyColonyRow(1, "Delft", 9, 400, 22);
    }
}
