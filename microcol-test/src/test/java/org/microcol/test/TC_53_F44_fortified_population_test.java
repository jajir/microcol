package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Fortification prevents voluntary departure but preserves work and forced
 * losses.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_53_F44_fortified_population_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F44-fortified-population.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_keep_three_allow_internal_work_and_fourth_departure()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        people.verifyPopulation(3);
        people.verifyFortDoesNotOverlap("id_tobacco", "id_rum", "id_carpenter",
                "id_fur");
        people.captureDialog("id_fort", "r82-fortified-colony.png");
        colony.rememberWorld();
        // A rejected drag leaves the full saved world unchanged and explains
        // why.
        people.dragIndoorOutside("id_carpenter", 0);
        colony.verifyWorldUnchanged();
        colony.verifyThatStatusBarContains(
                "A fortified colony must keep at least 3 residents.");
        people.verifyWorker(200, ConstructionType.CARPENTERS_STAND, false);
        // Internal reassignment keeps all three residents.
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_carpenter", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        people.verifyWorker(200, ConstructionType.CHAPEL, false);
        people.verifyPopulation(3);
        // A fourth resident can leave and board without taking a protected
        // worker.
        new ConstructionPage(getContext()).assignCarpenter(4);
        people.verifyPopulation(4);
        people.dragIndoorOutside("id_carpenter", 1);
        people.verifyOutside(4);
        people.verifyPopulation(3);
        colony.dragUnitFromPierToShipCargoSlot(0, 1);
        people.verifyBoarded(4);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        people.verifyPopulation(3);
        people.verifyWorker(200, ConstructionType.CHAPEL, false);
        people.verifyBoarded(4);
    }

    @Test
    @Tag("local")
    void TC_02_famine_can_reduce_population_without_removing_stockade()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        // Remove food-producing jobs, then expose the colony to an actual
        // update.
        jobs.openFromField(0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        jobs.openFromField(1);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.BELL);
        jobs.confirm();
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        people.exhaustFood();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        people.verifyPopulation(2);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        people.verifyPopulation(2);
    }
}
