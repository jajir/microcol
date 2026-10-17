package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * UC-108: La Salle gives a stockade to eligible colonies once, without cost,
 * while the existing fortified-population rule keeps three residents.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_72_F64_la_salle_stockade_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String COLONY_NAME = "New Amsterdam";
    private static final String BLOCKED = "A fortified colony must keep at least 3 residents.";

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F64-la-salle-stockade.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_join_with_two_residents_then_third_resident_grants_stockade()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        LaSalleStockadePage defense = new LaSalleStockadePage(getContext());
        ConstructionPage work = new ConstructionPage(getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        defense.verifyPopulation(2);
        defense.verifyStockadeBuilt(0);
        people.verifyOutside(4);
        // Canceling the founder dialog changes no building or resident.
        congress.rememberWorld();
        congress.open();
        congress.select(Founder.SIEUR_DE_LA_SALLE);
        congress.close();
        congress.verifyWorldUnchanged();
        // Choosing him and one ordinary update completes the objective.
        congress.open();
        congress.select(Founder.SIEUR_DE_LA_SALLE);
        congress.choose();
        congress.close();
        defense.verifyStockadeBuilt(0);
        game.nextTurnAndCloseDialogs();
        // Two residents are below the stockade threshold: nothing is granted.
        defense.verifyMembers(List.of(Founder.SIEUR_DE_LA_SALLE));
        defense.verifyPopulation(2);
        defense.verifyStockadeBuilt(0);
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        defense.verifyStockadeShown(false);
        // The third resident arrives through the normal drag and gets the
        // free stockade without hammers, tools or gold.
        work.assignCarpenter(4);
        defense.verifyPopulation(3);
        defense.verifyStockadeBuilt(1);
        defense.verifyStockadeShown(true);
        defense.verifyStock(58, 19, 10000);
        people.verifyWorker(4, ConstructionType.CARPENTERS_STAND, false);
        // The existing fortified rule keeps all three residents inside.
        colony.rememberWorld();
        people.dragIndoorOutside("id_carpenter", 1);
        colony.verifyWorldUnchanged();
        colony.verifyThatStatusBarContains(BLOCKED);
        defense.verifyPopulation(3);
        // Continue keeps the exact defense, residents and resources.
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                COLONY_NAME);
        defense.verifyMembers(List.of(Founder.SIEUR_DE_LA_SALLE));
        defense.verifyStockadeBuilt(1);
        defense.verifyStockadeShown(true);
        defense.verifyPopulation(3);
        defense.verifyStock(58, 19, 10000);
        people.verifyWorker(4, ConstructionType.CARPENTERS_STAND, false);
    }

    @Test
    @Tag("local")
    void TC_02_existing_eligible_colony_is_granted_when_he_joins()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        LaSalleStockadePage defense = new LaSalleStockadePage(getContext());
        ConstructionPage work = new ConstructionPage(getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        // Without the father, three residents get no stockade.
        ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        work.assignCarpenter(4);
        defense.verifyPopulation(3);
        defense.verifyStockadeBuilt(0);
        defense.verifyStockadeShown(false);
        game = colony.close();
        // Choosing him is not yet membership and builds nothing.
        congress.open();
        congress.select(Founder.SIEUR_DE_LA_SALLE);
        congress.choose();
        congress.close();
        defense.verifyStockadeBuilt(0);
        defense.verifyStock(58, 19, 10000);
        // Joining grants the already eligible colony without spending.
        game.nextTurnAndCloseDialogs();
        defense.verifyMembers(List.of(Founder.SIEUR_DE_LA_SALLE));
        defense.verifyStockadeBuilt(1);
        defense.verifyStock(58, 19, 10000);
        colony = game.openColonyAt(COLONY, COLONY_NAME);
        defense.verifyStockadeShown(true);
        colony.rememberWorld();
        people.dragIndoorOutside("id_carpenter", 1);
        colony.verifyWorldUnchanged();
        colony.verifyThatStatusBarContains(BLOCKED);
        defense.verifyPopulation(3);
        // A further turn and Continue never add a second stockade.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                COLONY_NAME);
        defense.verifyStockadeBuilt(1);
        defense.verifyStockadeShown(true);
        defense.verifyPopulation(3);
        defense.verifyStock(58, 19, 10000);
    }
}
