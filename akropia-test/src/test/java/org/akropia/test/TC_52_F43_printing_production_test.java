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
 * Printing upgrades multiply the same town hall, preserving workers and saved
 * owls.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_52_F43_printing_production_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F43-printing-production.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_press_then_newspaper_round_total_once_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        PrintingProductionPage owls = new PrintingProductionPage(getContext());
        // Two ordinary statesmen plus the unattended owl produce seven.
        owls.verifyOwls(7, 0, 0, 1);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.OWLS);
        jobs.verifyDetails(
                "Worker contribution before colony bonuses: 3 Owls per turn",
                true);
        jobs.cancel();
        ConstructionPage building = new ConstructionPage(getContext());
        colony.rememberWorld();
        building.open();
        building.select("PRINTING_PRESS");
        building.verifyOffer(true, "Available.", 80, 0);
        building.cancel();
        colony.verifyWorldUnchanged();
        building.open();
        building.select("PRINTING_PRESS");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.PRINTING_PRESS, 1);
        owls.verifyOwls(10, 7, 0, 1);
        // Complete the replacement newspaper, using this turn's press output.
        owls.prepareNewspaperMaterials();
        building.open();
        building.select("NEWSPAPER");
        building.verifyOffer(true, "Available.", 120, 50);
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.PRINTING_PRESS, 0);
        building.verifyBuilding(ConstructionType.NEWSPAPER, 1);
        owls.verifyOwls(14, 17, 0, 1);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        owls.verifyOwls(14, 31, 0, 1);
        owls.captureDialog("id_town_hall", "r81-newspaper-output.png");
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        owls.verifyOwls(14, 31, 0, 1);
        building.verifyBuilding(ConstructionType.NEWSPAPER, 1);
    }

    @Test
    @Tag("local")
    void TC_02_printing_also_modifies_unattended_owls() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        // Give both statesmen other jobs through the actual work picker.
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.WINE);
        jobs.confirm();
        jobs.openFromIndoor("id_town_hall", 1);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        PrintingProductionPage owls = new PrintingProductionPage(getContext());
        owls.verifyOwls(1, 0);
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("PRINTING_PRESS");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        owls.verifyOwls(1, 1);
        owls.prepareNewspaperMaterials();
        building.open();
        building.select("NEWSPAPER");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        owls.verifyOwls(2, 2);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        owls.verifyOwls(2, 4);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        owls.verifyOwls(2, 4);
    }
}
