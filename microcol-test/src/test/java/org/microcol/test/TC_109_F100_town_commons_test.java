package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.screen.colony.PanelColonyGoods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Town commons (R-0146, UC-031): the unattended center output is shown as a
 * labeled forecast without any worker, settles once per owner update and
 * survives Continue. The fixture is derived in memory from the founding save: a
 * prime-food center, a hill center and a forested center with prime timber.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_109_F100_town_commons_test extends AbstractMicroColTest {

    private static final File SOURCE_FILE = new File(
            "src/test/scenarios/F02-founding-colony.microcol");

    private static final File DERIVED_FILE = new File(
            "target/town-commons/F100-town-commons.microcol");

    private static final Location NEW_AMSTERDAM = Location.of(25, 14);

    private static final Location DELFT = Location.of(22, 12);

    private static final Location HAARLEM = Location.of(23, 17);

    /** A goods panel narrower than this is squeezed out of its row. */
    private static final double MINIMUM_GOODS_WIDTH = 30;

    private static final String EN_NEW_AMSTERDAM = "Town commons, no worker: food 6, Tobacco 3.";

    private static final String EN_DELFT = "Town commons, no worker: food 2, Ore 4.";

    private static final String EN_HAARLEM = "Town commons, no worker: food 3, Lumber 6.";

    private static final String CS_NEW_AMSTERDAM = "Obecní zdroje města, bez pracovníka: jídlo 6, Tabák 3.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), derivedFixture());
    }

    /** Derive the scenario from the founding save; no bundled file changes. */
    private static File derivedFixture() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(SOURCE_FILE);
        final java.util.Map<Location, ResourceOverlay> resources = data
                .getModel().getMap().getResourceMap();
        resources.put(NEW_AMSTERDAM, ResourceOverlay.PRIME_FOOD);
        resources.put(HAARLEM, ResourceOverlay.PRIME_TIMBER);
        data.getModel().getMap().setResources(resources);
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(DELFT, TerrainType.HILL);
        data.getModel().getMap().setTerrainType(terrain);
        addForest(data, HAARLEM);
        // Identity follows the derived center: hill ore, forested lumber, and
        // the grassland tobacco of New Amsterdam unchanged.
        colonyNamed(data, "Delft").setTownCommonsGoods("ORE");
        final ColonyPo haarlem = colonyNamed(data, "Haarlem");
        haarlem.setTownCommonsGoods("LUMBER");
        // Haarlem keeps only its three tobacconists indoors: its surrounding
        // fields have no worker, so the center is the only food and lumber.
        final java.util.Set<Integer> fieldWorkers = new java.util.HashSet<>();
        haarlem.getColonyFields().forEach(field -> {
            if (field.getWorkerId() != null) {
                fieldWorkers.add(field.getWorkerId());
                field.setWorkerId(null);
                field.setProducedGoodsType(null);
            }
        });
        data.getModel()
                .setUnits(data.getModel().getUnits().stream()
                        .filter(unit -> !fieldWorkers.contains(unit.getId()))
                        .collect(java.util.stream.Collectors.toList()));
        DERIVED_FILE.getParentFile().mkdirs();
        dao.saveToFile(DERIVED_FILE.getPath(), data);
        return DERIVED_FILE;
    }

    private static ColonyPo colonyNamed(final GameModelPo data,
            final String name) {
        return data.getModel().getColonies().stream()
                .filter(colony -> name.equals(colony.getName())).findFirst()
                .orElseThrow();
    }

    private static void addForest(final GameModelPo data, final Location at) {
        final String[] trees = data.getModel().getMap().getTrees().clone();
        final String prefix = String.format("row-%04d:", at.getY());
        for (int index = 0; index < trees.length; index++) {
            if (trees[index].startsWith(prefix)) {
                final String[] cells = trees[index].substring(prefix.length())
                        .split(",");
                cells[at.getX() - 1] = "t";
                trees[index] = prefix + String.join(",", cells);
            }
        }
        data.getModel().getMap().setTrees(trees);
    }

    @Test
    @Tag("local")
    void TC_01_forecast_settles_once_and_survives_continue_in_english()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Each center shows its own commons: overlay, hill ore, forest and
        // prime timber, with no worker and no Prime Timber bonus on lumber.
        ColonyScreen newAmsterdam = game.openColonyAt(NEW_AMSTERDAM,
                "New Amsterdam");
        newAmsterdam.verifyTownCommons(EN_NEW_AMSTERDAM);
        // The commons row is whole and readable and the goods row is intact.
        newAmsterdam.verifyTownCommonsNotEllipsized(EN_NEW_AMSTERDAM);
        newAmsterdam.verifyWarehouseGoodsAndTownCommonsLayout(
                GoodsType.BUYABLE_GOOD_TYPES.size(), MINIMUM_GOODS_WIDTH);
        newAmsterdam.captureDialog(PanelColonyGoods.TOWN_COMMONS_ID,
                "town-commons-en-new-amsterdam.png");
        game = newAmsterdam.close();

        ColonyScreen delft = game.openColonyAt(DELFT, "Delft");
        delft.verifyTownCommons(EN_DELFT);
        final int oreBefore = delft.stockOf(GoodsType.ORE);
        game = delft.close();

        ColonyScreen haarlem = game.openColonyAt(HAARLEM, "Haarlem");
        haarlem.verifyTownCommons(EN_HAARLEM);
        game = haarlem.close();

        // One owner update settles the hill ore once: four ore enter the
        // warehouse, and the label still reads the same forecast.
        game.nextTurnAndCloseDialogs();
        delft = game.openColonyAt(DELFT, "Delft");
        delft.verifyWarehouseStock(GoodsType.ORE, oreBefore + 4);
        delft.verifyTownCommons(EN_DELFT);
        game = delft.close();

        // Save and Continue from disk do not repeat the update.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        delft = game.openColonyAt(DELFT, "Delft");
        delft.verifyWarehouseStock(GoodsType.ORE, oreBefore + 4);
        delft.verifyTownCommons(EN_DELFT);
        delft.close();
    }

    @Test
    @Tag("local")
    void TC_02_forecast_label_is_czech_and_captured() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        final ColonyScreen newAmsterdam = game.openColonyAt(NEW_AMSTERDAM,
                "New Amsterdam");
        newAmsterdam.verifyTownCommons(CS_NEW_AMSTERDAM);
        newAmsterdam.verifyTownCommonsNotEllipsized(CS_NEW_AMSTERDAM);
        newAmsterdam.verifyWarehouseGoodsAndTownCommonsLayout(
                GoodsType.BUYABLE_GOOD_TYPES.size(), MINIMUM_GOODS_WIDTH);
        newAmsterdam.captureDialog(PanelColonyGoods.TOWN_COMMONS_ID,
                "town-commons-cs-new-amsterdam.png");
        newAmsterdam.close();
    }

    @Test
    @Tag("local")
    void TC_03_keyboard_opens_and_closes_colony_with_unchanged_commons()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Press O for the colonies menu, arrow down to New Amsterdam, Enter.
        ColonyScreen newAmsterdam = game.openColonyWithKeyboard(0,
                "New Amsterdam");
        newAmsterdam.verifyTownCommons(EN_NEW_AMSTERDAM);
        newAmsterdam.verifyTownCommonsNotEllipsized(EN_NEW_AMSTERDAM);
        final int tobaccoBefore = newAmsterdam.stockOf(GoodsType.TOBACCO);

        // Escape leaves the colony; opening it again by keys changes nothing.
        game = newAmsterdam.closeWithEscape();
        newAmsterdam = game.openColonyWithKeyboard(0, "New Amsterdam");
        newAmsterdam.verifyTownCommons(EN_NEW_AMSTERDAM);
        newAmsterdam.verifyWarehouseStock(GoodsType.TOBACCO, tobaccoBefore);
        newAmsterdam.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_04_unworked_surroundings_forecast_matches_settlement_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Haarlem has no worker on any surrounding field; its three residents
        // work indoors and eat 6 food, so the forecast holds only the commons.
        ColonyScreen haarlem = game.openColonyAt(HAARLEM, "Haarlem");
        haarlem.verifyNumberOfUnitsOnFields(0);
        haarlem.verifyTownCommons(EN_HAARLEM);
        haarlem.verifyFoodAccounting(3, 6);
        haarlem.verifyThatProductionIs(GoodsType.CORN, -3);
        haarlem.verifyThatProductionIs(GoodsType.LUMBER, 6);
        haarlem.verifyWarehouseStock(GoodsType.CORN, 134);
        haarlem.verifyWarehouseStock(GoodsType.LUMBER, 0);
        game = haarlem.close();

        // One owner update settles exactly the forecast: 134 + 3 - 6 food and
        // 6 lumber, with no Prime Timber bonus.
        game.nextTurnAndCloseDialogs();
        haarlem = game.openColonyAt(HAARLEM, "Haarlem");
        haarlem.verifyWarehouseStock(GoodsType.CORN, 131);
        haarlem.verifyWarehouseStock(GoodsType.LUMBER, 6);
        haarlem.verifyNumberOfUnitsOnFields(0);
        game = haarlem.close();

        // Save and Continue from disk do not settle the update again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        haarlem = game.openColonyAt(HAARLEM, "Haarlem");
        haarlem.verifyWarehouseStock(GoodsType.CORN, 131);
        haarlem.verifyWarehouseStock(GoodsType.LUMBER, 6);
        haarlem.verifyTownCommons(EN_HAARLEM);
        haarlem.close();
    }
}
