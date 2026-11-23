package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.*;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Population gates construction, not departure from completed fortifications.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_53_F44_fortified_population_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F44-fortified-population.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_allow_departure_below_three_and_preserve_work_and_boarding()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        people.verifyPopulation(3);
        people.verifyFortDoesNotOverlap("id_winery", "id_oil_press",
                "id_carpenter", "id_pottery");
        people.captureDialog("id_fort", "r82-fortified-colony.png");
        // Internal reassignment keeps all three residents.
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_carpenter", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.TORCHES);
        jobs.confirm();
        people.verifyWorker(200, ConstructionType.CHAPEL);
        people.verifyPopulation(3);
        // The third resident may leave completed defenses, then return.
        people.dragIndoorOutside("id_church", 0);
        people.verifyOutside(200);
        people.verifyPopulation(2);
        jobs.dragCenterToIndoor(200, "id_church", 0);
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
        people.verifyWorker(200, ConstructionType.CHAPEL);
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
        jobs.assignIndoorFromField(0, "id_church");
        jobs.assignIndoorFromField(1, "id_town_hall");
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

    @Test
    @Tag("local")
    void TC_03_empty_city_survives_reassignment_and_requires_turn_end_confirmation()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());

        // Every resident may leave; the city remains available during the turn.
        jobs.assignIndoorFromField(0, "id_church");
        jobs.assignIndoorFromField(1, "id_church");
        people.dragIndoorOutside("id_church", 0);
        people.dragIndoorOutside("id_church", 1);
        people.dragIndoorOutside("id_carpenter", 0);
        people.verifyPopulation(0);
        colony.verifyColonyPresent("New Amsterdam", true);

        // Returning a worker in the same turn rescues the city.
        jobs.dragCenterToIndoor(200, "id_carpenter", 0);
        people.verifyPopulation(1);
        people.dragIndoorOutside("id_carpenter", 0);
        people.verifyPopulation(0);
        game = colony.close();
        colony.rememberWorld();

        // Canceling the named turn-end warning changes no gameplay.
        people.requestAbandonment();
        people.verifyEmptyColonyQuestion("New Amsterdam");
        people.cancelEmptyColony();
        colony.verifyWorldUnchanged();
        colony.verifyColonyPresent("New Amsterdam", true);

        // Save/Continue preserves zero residents and the pending decision.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        people.verifyPopulation(0);
        colony.close();
        people.requestAbandonment();
        people.verifyEmptyColonyQuestion("New Amsterdam");
        people.confirmEmptyColony();
        people.verifyColonyAbsent(0);
        new org.akropia.page.ScoreBoundaryPage(getContext())
                .verifyZeroColonyEnd();
    }

    @Test
    @Tag("local")
    void TC_04_disbanding_the_last_field_worker_defers_abandonment()
            throws Exception {
        // Leave one resident working outside; the others stand in town.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        FortifiedPopulationPage people = new FortifiedPopulationPage(
                getContext());
        jobs.assignIndoorFromField(1, "id_church");
        people.dragIndoorOutside("id_church", 0);
        people.dragIndoorOutside("id_carpenter", 0);
        people.verifyPopulation(1);
        game = colony.close();
        game.selectTile(Location.of(23, 15));
        org.akropia.page.DisbandPage disband = new org.akropia.page.DisbandPage(
                getContext());
        disband.verifySelectedUnit(java.util.Optional.of(0));
        disband.verifyControlEnabled(true);
        // Cancel the first disband confirmation without losing the worker.
        disband.useControl();
        disband.cancel();
        disband.verifyUnitPresent(0, true);
        people.verifyPopulation(1);
        // Confirmation removes the unit but preserves its now empty city.
        disband.useControl();
        disband.confirm();
        disband.verifyUnitPresent(0, false);
        people.verifyPopulation(0);
        colony.verifyColonyPresent("New Amsterdam", true);
        people.requestAbandonment();
        people.verifyEmptyColonyQuestion("New Amsterdam");
        people.cancelEmptyColony();
        people.verifyPopulation(0);
        colony.verifyColonyPresent("New Amsterdam", true);
    }

}
