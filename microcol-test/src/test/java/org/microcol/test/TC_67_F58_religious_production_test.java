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
 * Build religious upgrades, assign preachers and retain actual cross credit.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_67_F58_religious_production_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F58-religious-production.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_upgrade_then_expert_and_continue() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ReligiousProductionPage crosses = new ReligiousProductionPage(
                getContext());
        ConstructionPage building = new ConstructionPage(getContext());
        crosses.verifyCrosses(colony, 1, 0);
        // Completing the church keeps this turn's chapel production.
        building.open();
        building.select("CHURCH");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.CHURCH, 1);
        crosses.verifyCrosses(colony, 2, 1);
        // One ordinary preacher gives five; the cathedral immediately previews
        // nine.
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        crosses.verifyCrosses(colony, 5, 1);
        crosses.prepareCathedralMaterials();
        building.open();
        building.select("CATHEDRAL");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.CHURCH, 0);
        building.verifyBuilding(ConstructionType.CATHEDRAL, 1);
        crosses.verifyCrosses(colony, 9, 6);
        // Removing the worker retains the tier; a firebrand doubles only
        // staffed output.
        jobs.openFromIndoor("id_church", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.BELL);
        jobs.confirm();
        crosses.verifyCrosses(colony, 3, 6);
        jobs.openFromIndoor("id_blacksmith", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        crosses.verifyCrosses(colony, 15, 6);
        crosses.captureDialog("id_church", "religion-english.png");
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 15, 21);
        game = colony.close();
        game.exitToMenu();
        colony = new FreePlayAuditPage(getContext()).continueGame()
                .openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 15, 21);
    }

    @Test
    @Tag("local")
    void TC_02_czech_penn_rounds_total_and_persists() throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        ReligiousProductionPage crosses = new ReligiousProductionPage(
                getContext());
        crosses.loadPennFixture();
        GamePage game = GamePage.of(getContext());
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 1, 0);
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("CHURCH");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 3, 1);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        crosses.verifyCrosses(colony, 7, 1);
        crosses.prepareCathedralMaterials();
        building.open();
        building.select("CATHEDRAL");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 13, 8);
        crosses.captureDialog("id_church", "religion-czech.png");
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 13, 21);
        game = colony.close();
        game.exitToMenu();
        colony = new FreePlayAuditPage(getContext()).continueGame()
                .openColonyAt(COLONY, "New Amsterdam");
        crosses.verifyCrosses(colony, 13, 21);
    }
}
