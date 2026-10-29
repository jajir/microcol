package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.FortifiedPopulationPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.PoliticalProductionPage;
import org.microcol.page.RebelSupportPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-078: exact rebel support gives staffed jobs a production bonus, a growing
 * population can take it away, the blacksmith's house stays limited by the ore
 * actually in the warehouse, and Continue restores the same values.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_77_F69_political_production_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F64-la-salle-stockade.microcol");
    private static final Location COLONY = Location.of(24, 16);
    private static final String COLONY_NAME = "New Amsterdam";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_growing_population_removes_the_bonus_and_ore_still_limits_tools()
            throws Exception {
        // Three residents hold 300 liberty, exactly half of full support, and
        // the blacksmith's house has only 3 ore for a worker who could use 4.
        PoliticalProductionPage production = new PoliticalProductionPage(
                getContext());
        production.prepareFixture(300, 3);
        RebelSupportPage support = new RebelSupportPage(getContext());
        ConstructionPage work = new ConstructionPage(getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        // Exactly 50 percent gives +1: 2 Tories are below the threshold of 8.
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 3, 300, 50, 1, 2, 8, 0);
        // The boosted job would make 4 tools, but only 3 ore are available.
        production.verifyPlannedTools(3, 3, 1);
        // A fourth resident takes a carpenter job and dilutes the support.
        work.assignCarpenter(production.getWaitingColonistId());
        production.verifyPoliticalProduction(4, 0, 3, 0);
        game = colony.close();
        // 300 of 800 points is 37 percent: no bonus, no ore shortage.
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 4, 300, 37, 0, 3, 8, 0);
        production.verifyPlannedTools(3, 3, 0);
        game = colony.close();
        // The update uses the support captured at its start.
        game.nextTurnAndCloseDialogs();
        production.verifyWarehouse(GoodsType.TOOLS, 22);
        production.verifyWarehouse(GoodsType.ORE, 0);
        // The empty town hall adds its single unattended bell, without bonus.
        support.verifyLiberty(301, 4);
        // Continue restores the same colony and adds no modifier again.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouse(GoodsType.TOOLS, 22);
        production.verifyPoliticalProduction(4, 0, 3, 0);
        support.verifyLiberty(301, 4);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 4, 301, 37, 0, 3, 8, 0);
    }

    @Test
    @Tag("local")
    void TC_02_tory_penalty_is_removed_when_the_count_drops_below_the_line()
            throws Exception {
        // Viceroy draws the Tory line at 6; six residents and no liberty give
        // 6 Tories, so every staffed job loses one unit of output.
        PoliticalProductionPage production = new PoliticalProductionPage(
                getContext());
        production.prepareToryFixture();
        RebelSupportPage support = new RebelSupportPage(getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 6, 0, 0, 0, 6, 6, -1);
        // The blacksmith would make 3 tools at the base rate, now only 2.
        production.verifyPlannedTools(2, 2, 0);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        production.verifyWarehouse(GoodsType.TOOLS, 21);
        production.verifyWarehouse(GoodsType.ORE, 88);
        // A carpenter leaves work: 5 Tories are below the line of 6.
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        people.dragIndoorOutside("id_carpenter", 2);
        production.verifyPoliticalProduction(5, 0, 5, 0);
        game = colony.close();
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 5, 1, 0, 0, 5, 6, 0);
        production.verifyPlannedTools(3, 3, 0);
        game = colony.close();
        // The next update makes the full 3 tools again.
        game.nextTurnAndCloseDialogs();
        production.verifyWarehouse(GoodsType.TOOLS, 24);
        production.verifyWarehouse(GoodsType.ORE, 85);
        // Continue restores the same values without changing them.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        production.verifyWarehouse(GoodsType.TOOLS, 24);
        production.verifyPoliticalProduction(5, 0, 5, 0);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        support.verifyColonyDetails(COLONY_NAME, 5, 2, 0, 0, 5, 6, 0);
    }
}
