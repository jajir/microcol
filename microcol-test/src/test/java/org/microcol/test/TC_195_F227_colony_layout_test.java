package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_195_F227_colony_layout_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F227-colony-layout.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_sections_and_explicit_tile_production() throws Exception {
        // Read the model-backed summary and all sixteen stable building
        // positions.
        final ColonyScreen town = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        layout.verifyLayout("New Amsterdam", 7);
        layout.verifyBuildingLevel("id_warehouse",
                ConstructionType.WAREHOUSE_BASIC);
        layout.verifyCenter();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0352-colony-english.png");

        // Selecting a worked tile by keyboard is read-only.
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        layout.selectTile(1, 1);
        layout.verifyMapFocused();
        layout.verifySelectedTileColor(1, 1, javafx.scene.paint.Color.GREY);
        layout.moveSelection(KeyCode.LEFT);
        layout.verifySelectedTileColor(0, 1, javafx.scene.paint.Color.GREY);
        layout.verifyOutput(GoodsType.LUMBER, 2);
        jobs.verifyWorldUnchanged();

        // An explicit crop choice updates this worker, the inspector and
        // forecast.
        layout.chooseOutput(GoodsType.CORN);
        layout.verifyOutput(GoodsType.CORN, 2);
        jobs.verifyFieldJob(0, Location.of(23, 16),
                org.microcol.model.Goods.of(GoodsType.CORN, 2));
        layout.verifyLayout("New Amsterdam", 7);
        layout.selectTile(1, 1);
        layout.verifyCenter();

        // The warehouse remains reachable at the original window width.
        new GameBarWidthPage(getContext()).resizeContent(1000, 925);
        layout.showStock();
        town.verifyWarehouseGoodsAndTownCommonsLayout(16, 45);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0352-colony-small.png");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_02_czech_upgraded_town() throws Exception {
        // Use the normal language settings and open a town with every family
        // upgraded.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final ColonyScreen town = settings.goBack().loadGame()
                .openColonyAt(Location.of(26, 16), "Delft");
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        layout.verifyLayout("Delft", 0);
        layout.verifyBuildingLevel("id_fort", ConstructionType.FORTRESS);
        layout.verifyBuildingLevel("id_warehouse",
                ConstructionType.WAREHOUSE_EXPANSION);
        layout.verifyBuildingLevel("id_dock", ConstructionType.SHIPYARD);
        layout.verifyCenter();
        layout.showStock();
        town.verifyWarehouseGoodsAndTownCommonsLayout(16, 45);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0352-colony-czech-upgrades.png");
        town.close();
    }
}
