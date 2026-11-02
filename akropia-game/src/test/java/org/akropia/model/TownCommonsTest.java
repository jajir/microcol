package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ColonyPo;

/**
 * Unattended town commons (R-0146): baseline table, fixed tie order, explicit
 * none, overlays, finite reserves, preview purity and saved identity. The
 * center of the mission fixture is Location (25,14), colony id 0.
 */
class TownCommonsTest {

    private static final Location CENTER = Location.of(25, 14);

    private static Terrain center(final TerrainType type,
            final boolean forest) {
        final Terrain terrain = new Terrain(CENTER, type);
        if (forest) {
            terrain.setHasTrees(true);
        }
        return terrain;
    }

    private static GameModelPo mission() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
    }

    private static ColonyPo colonyPo(final GameModelPo data) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
    }

    private static void setCenter(final GameModelPo data,
            final TerrainType type) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(CENTER, type);
        data.getModel().getMap().setTerrainType(terrain);
    }

    private static void setOverlay(final GameModelPo data,
            final ResourceOverlay overlay) {
        final Map<Location, ResourceOverlay> resources = data.getModel()
                .getMap().getResourceMap();
        resources.put(CENTER, overlay);
        data.getModel().getMap().setResources(resources);
    }

    private static void setFiniteOre(final GameModelPo data,
            final MineralReserve reserve) {
        setOverlay(data, ResourceOverlay.ORE);
        final Map<Location, MineralReserve> reserves = data.getModel().getMap()
                .getMineralReserveMap();
        reserves.put(CENTER, reserve);
        data.getModel().getMap().setMineralReserves(reserves);
    }

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Terrain baseline rows: open and forested, with the saved food. */
    @ParameterizedTest
    @CsvSource({ "GRASSLAND,false,GRAPES,3,4", "GRASSLAND,true,LUMBER,6,3",
            "PRAIRIE,false,WOOL,3,4", "PRAIRIE,true,LUMBER,4,3",
            "SAVANNAH,false,OLIVES,3,4", "SAVANNAH,true,LUMBER,4,3",
            "PLAINS,false,WOOL,2,4", "PLAINS,true,LUMBER,6,3",
            "MARSH,false,GRAPES,3,3", "MARSH,true,LUMBER,4,2",
            "SWAMP,false,OLIVES,3,3", "SWAMP,true,LUMBER,4,2",
            "DESERT,false,ORE,2,2", "DESERT,true,CLAY,2,2",
            "TUNDRA,false,ORE,2,3", "TUNDRA,true,LUMBER,4,2",
            "ARCTIC,false,NONE,0,0", "HILL,false,ORE,4,2" })
    void baselineTableSelectsTheSecondCommodityAndFood(final String terrain,
            final boolean forest, final String second, final int secondAmount,
            final int food) {
        final Terrain square = center(TerrainType.valueOf(terrain), forest);
        final Optional<GoodsType> expected = "NONE".equals(second)
                ? Optional.empty()
                : Optional.of(GoodsType.valueOf(second));
        assertEquals(expected, TownCommons.chooseSecondCommodity(square));
        assertEquals(food, square.getCommonsProduction(GoodsType.GRAIN));
        expected.ifPresent(goods -> assertEquals(secondAmount,
                square.getCommonsProduction(goods)));
    }

    @Test
    void tiesFollowTheFixedCommodityOrder() {
        // Savanna olives and wool 3: olives come earlier in the fixed order.
        assertEquals(Optional.of(GoodsType.OLIVES), TownCommons
                .chooseSecondCommodity(center(TerrainType.SAVANNAH, false)));
        // Marsh grapes and ore 3: grapes is earlier.
        assertEquals(Optional.of(GoodsType.GRAPES), TownCommons
                .chooseSecondCommodity(center(TerrainType.MARSH, false)));
        // Forested desert clay and lumber 2: clay is earlier.
        assertEquals(Optional.of(GoodsType.CLAY), TownCommons
                .chooseSecondCommodity(center(TerrainType.DESERT, true)));
    }

    @Test
    void savedNoneAndCommodityNamesRoundTrip() {
        assertEquals(TownCommons.NONE_CODE, TownCommons.save(Optional.empty()));
        assertEquals(Optional.empty(),
                TownCommons.parse(TownCommons.NONE_CODE));
        assertEquals("ORE", TownCommons.save(Optional.of(GoodsType.ORE)));
        assertEquals(Optional.of(GoodsType.ORE), TownCommons.parse("ORE"));
    }

    @ParameterizedTest
    @CsvSource({ "GRASSLAND,false,PRIME_FOOD,GRAIN,6",
            "GRASSLAND,false,PRIME_GRAPES,GRAPES,5",
            // Matching only: wool overlay leaves the grapes second output.
            "GRASSLAND,false,PRIME_WOOL,GRAPES,3",
            // Prime Timber adds nothing to commons lumber.
            "GRASSLAND,true,PRIME_TIMBER,LUMBER,6",
            // A zero baseline stays zero.
            "ARCTIC,false,GAME,GRAIN,0",
            // Finite ore: baseline 4 plus the bonus 2 from the reserve.
            "HILL,false,ORE,ORE,6" })
    void overlayAddsOnlyToAMatchingPositiveBaseline(final String terrain,
            final boolean forest, final String overlay, final String goods,
            final int expected) {
        final Terrain square = center(TerrainType.valueOf(terrain), forest);
        square.setResourceOverlay(ResourceOverlay.valueOf(overlay));
        assertEquals(expected,
                square.getCommonsProduction(GoodsType.valueOf(goods)));
    }

    @Test
    void exhaustedFiniteDepositGivesOnlyTheBaseline() {
        final Terrain hill = center(TerrainType.HILL, false);
        hill.setResourceOverlay(ResourceOverlay.ORE);
        assertEquals(6, hill.getCommonsProduction(GoodsType.ORE));
        hill.setMineralReserve(MineralReserve.EMPTY);
        assertEquals(4, hill.getCommonsProduction(GoodsType.ORE));
        assertEquals(4, hill.getCommonsProduction(GoodsType.ORE));
    }

    @Test
    void previewsNeverExtractFromTheFiniteReserve() {
        final GameModelPo data = mission();
        setCenter(data, TerrainType.HILL);
        setFiniteOre(data, MineralReserve.initial(ResourceOverlay.ORE));
        colonyPo(data).setTownCommonsGoods("ORE");
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();

        for (int preview = 0; preview < 3; preview++) {
            colony.getGoodsStats();
            assertEquals(
                    List.of(Goods.of(GoodsType.GRAIN, 2),
                            Goods.of(GoodsType.ORE, 6)),
                    colony.getTownCommonsProduction());
        }
        assertEquals(200,
                model.getMap().getMineralRemainingAt(CENTER, GoodsType.ORE));
    }

    @Test
    void oneTurnExtractsOnlyTheReserveExtraOnce() {
        final GameModelPo data = mission();
        setCenter(data, TerrainType.HILL);
        setFiniteOre(data, MineralReserve.initial(ResourceOverlay.ORE));
        colonyPo(data).setTownCommonsGoods("ORE");
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();

        colony.startTurn();

        // Baseline ore 4 is free; only the bonus 2 comes out of the reserve.
        assertEquals(198,
                model.getMap().getMineralRemainingAt(CENTER, GoodsType.ORE));
    }

    @Test
    void renewableOverlayTurnProducesWithoutExtraction() {
        final GameModelPo data = mission();
        setOverlay(data, ResourceOverlay.PRIME_FOOD);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(
                List.of(Goods.of(GoodsType.GRAIN, 6),
                        Goods.of(GoodsType.GRAPES, 3)),
                colony.getTownCommonsProduction());

        // A renewable bonus has no reserve, so the turn must not try to debit
        // one; startTurn would throw if it asked the map to extract grain.
        colony.startTurn();

        assertEquals(
                List.of(Goods.of(GoodsType.GRAIN, 6),
                        Goods.of(GoodsType.GRAPES, 3)),
                colony.getTownCommonsProduction());
    }

    @Test
    void exhaustedReserveTurnKeepsTheBaselineOnly() {
        final GameModelPo data = mission();
        setCenter(data, TerrainType.HILL);
        setFiniteOre(data, MineralReserve.of(0, 0));
        colonyPo(data).setTownCommonsGoods("ORE");
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(Goods.of(GoodsType.ORE, 4),
                colony.getTownCommonsProduction().get(1));

        colony.startTurn();

        assertEquals(0,
                model.getMap().getMineralRemainingAt(CENTER, GoodsType.ORE));
    }

    @Test
    void savedNoneGivesFoodOnly() {
        final GameModelPo data = mission();
        colonyPo(data).setTownCommonsGoods("NONE");
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        assertEquals(List.of(Goods.of(GoodsType.GRAIN, 4)),
                colony.getTownCommonsProduction());
    }

    @Test
    void savedIdentitySurvivesTerrainChangeAndSaveLoad() {
        final GameModelPo data = mission();
        // Saved GRAPES stays chosen; hill gives no grapes, so output is zero.
        setCenter(data, TerrainType.HILL);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(
                List.of(Goods.of(GoodsType.GRAIN, 2),
                        Goods.of(GoodsType.GRAPES, 0)),
                colony.getTownCommonsProduction());

        final ColonyPo saved = model.save().getColonies().stream()
                .filter(colony0 -> colony0.getId() == 0).findFirst()
                .orElseThrow();
        assertEquals("GRAPES", saved.getTownCommonsGoods());
        final Colony restored = Model
                .make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                .getColonyById(0).orElseThrow();
        assertEquals(colony.getTownCommonsProduction(),
                restored.getTownCommonsProduction());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "WEAPONS", "GRAIN", "grapes", "" })
    void missingOrInvalidIdentityIsRejectedOnLoad(final String saved) {
        final GameModelPo data = mission();
        colonyPo(data).setTownCommonsGoods(saved);
        assertThrows(IllegalArgumentException.class, () -> load(data));
    }
}
