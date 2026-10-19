package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

/**
 * Baseline terrain output of the aid card for every legal bare and forested
 * square. The literal cells are transcribed from the aid card and must not be
 * derived from the production tables.
 */
class TerrainProductionMatrixTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private final GameModelPoDao dao = new GameModelPoDao();

    private static Terrain terrain(final TerrainType type,
            final boolean forest) {
        final Terrain terrain = new Terrain(Location.of(1, 1), type);
        terrain.setHasTrees(forest);
        return terrain;
    }

    /**
     * Literal baseline for one legal variant, in the order CORN, SUGAR,
     * TOBACCO, COTTON, FUR, LUMBER, ORE, SILVER.
     */
    @ParameterizedTest(name = "{0} forest={1}")
    @CsvSource({ "PLAINS,false,4,0,1,2,0,0,0,0", "PLAINS,true,3,0,1,1,3,6,0,0",
            "GRASSLAND,false,4,1,3,2,0,0,0,0", "GRASSLAND,true,3,0,1,1,2,6,0,0",
            "PRAIRIE,false,4,0,2,3,0,0,0,0", "PRAIRIE,true,3,0,1,1,2,4,0,0",
            "SAVANNAH,false,4,3,2,3,0,0,0,0", "SAVANNAH,true,3,1,1,1,2,4,0,0",
            "MARSH,false,3,1,3,0,0,0,3,0", "MARSH,true,2,1,2,0,2,4,2,0",
            "SWAMP,false,3,3,1,0,0,0,0,0", "SWAMP,true,2,2,0,0,0,4,0,0",
            "DESERT,false,2,0,1,1,0,0,2,0", "DESERT,true,2,0,0,1,2,2,1,0",
            "TUNDRA,false,3,0,0,0,0,0,2,0", "TUNDRA,true,2,0,0,0,3,4,1,0",
            "ARCTIC,false,0,0,0,0,0,0,0,0", "HILL,false,2,0,0,0,0,0,4,0",
            "MOUNTAIN,false,0,0,0,0,0,0,3,1", "OCEAN,false,3,0,0,0,0,0,0,0",
            "LAKE,false,3,0,0,0,0,0,0,0", "HIGH_SEA,false,3,0,0,0,0,0,0,0" })
    void baselineCellsMatchTheAidCardLiterally(final String name,
            final boolean forest, final int corn, final int sugar,
            final int tobacco, final int cotton, final int fur,
            final int lumber, final int ore, final int silver) {
        final Terrain terrain = terrain(TerrainType.valueOf(name), forest);
        assertAll(
                () -> assertEquals(corn,
                        terrain.canProduceAmmount(GoodsType.CORN), "CORN"),
                () -> assertEquals(sugar,
                        terrain.canProduceAmmount(GoodsType.SUGAR), "SUGAR"),
                () -> assertEquals(tobacco,
                        terrain.canProduceAmmount(GoodsType.TOBACCO),
                        "TOBACCO"),
                () -> assertEquals(cotton,
                        terrain.canProduceAmmount(GoodsType.COTTON), "COTTON"),
                () -> assertEquals(fur,
                        terrain.canProduceAmmount(GoodsType.FUR), "FUR"),
                () -> assertEquals(lumber,
                        terrain.canProduceAmmount(GoodsType.LUMBER), "LUMBER"),
                () -> assertEquals(ore,
                        terrain.canProduceAmmount(GoodsType.ORE), "ORE"),
                () -> assertEquals(silver,
                        terrain.canProduceAmmount(GoodsType.SILVER), "SILVER"));
    }

    @Test
    void thereAreTwentyTwoLegalBareAndForestedVariants() {
        int variants = 0;
        for (final TerrainType type : TerrainType.TERRAINS) {
            variants += type.isCanHaveTree() ? 2 : 1;
        }
        assertEquals(22, variants);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ARCTIC", "HILL", "MOUNTAIN", "OCEAN", "LAKE",
            "HIGH_SEA" })
    void forestOnTreelessTerrainIsRejectedAndYieldsStayBare(final String name) {
        final Terrain terrain = new Terrain(Location.of(1, 1),
                TerrainType.valueOf(name));
        assertThrows(IllegalArgumentException.class,
                () -> terrain.setHasTrees(true));
        assertFalse(terrain.isHasTrees());
        assertEquals(0, terrain.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, terrain.canProduceAmmount(GoodsType.FUR));
    }

    @Test
    void forestedSquareOnlyOffersGoodsItsRowLists() {
        final Terrain tundra = terrain(TerrainType.TUNDRA, true);
        assertTrue(tundra.canProduce(GoodsType.FUR));
        assertFalse(tundra.canProduce(GoodsType.SUGAR));
        assertEquals(
                List.of(GoodsType.CORN, GoodsType.FUR, GoodsType.LUMBER,
                        GoodsType.ORE),
                tundra.getProduction().stream()
                        .map(TerrainProduction::getGoodsType)
                        .collect(Collectors.toList()));
        final Terrain bare = terrain(TerrainType.TUNDRA, false);
        assertFalse(bare.canProduce(GoodsType.FUR));
        assertFalse(bare.canProduce(GoodsType.LUMBER));
    }

    @Test
    void clearedSwampFallsBackToBareRowWithoutDuplicatingOutput() {
        final Terrain swamp = terrain(TerrainType.SWAMP, true);
        assertEquals(2, swamp.canProduceAmmount(GoodsType.CORN));
        assertEquals(4, swamp.canProduceAmmount(GoodsType.LUMBER));
        swamp.setHasTrees(false);
        swamp.setCleared(true);
        assertEquals(3, swamp.canProduceAmmount(GoodsType.CORN));
        assertEquals(3, swamp.canProduceAmmount(GoodsType.SUGAR));
        assertEquals(1, swamp.canProduceAmmount(GoodsType.TOBACCO));
        assertEquals(0, swamp.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, swamp.canProduceAmmount(GoodsType.FUR));
        assertEquals(3, swamp.getProduction().size());
    }

    /**
     * First unoccupied, unshared land square around colony 0 in the stable
     * direction order of the saved map.
     */
    private Location emptyLandFieldOfFirstColony() {
        final Model plain = Model.make(dao.loadFromClassPath(MAP).getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        return plain.getColonyById(0).orElseThrow().getColonyFields().stream()
                .filter(candidate -> candidate.isEmpty() && candidate.isValid()
                        && !candidate.getTerrainType().isSea()
                        && !candidate.isWorkedByAnotherColony()
                        && !plain.getColonyAt(candidate.getLocation())
                                .isPresent())
                .findFirst().orElseThrow().getLocation();
    }

    @Test
    void colonyUpdateMatchesThePreviewOfAForestedField() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        final Location target = emptyLandFieldOfFirstColony();
        types.put(target, TerrainType.SWAMP);
        trees.add(target);
        fields.remove(target);
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setFields(fields);
        final Model model = Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));

        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyField field = colony.getColonyFields().stream()
                .filter(candidate -> candidate.getLocation().equals(target))
                .findFirst().orElseThrow();
        assertEquals(TerrainType.SWAMP, field.getTerrainType());
        assertEquals(2, field.getGoodsTypeProduction(GoodsType.CORN));
        assertFalse(field.canProduce(GoodsType.FUR));
        final Unit colonist = model.addUnitOutSideColony(colony);
        colonist.placeToColonyField(field, GoodsType.LUMBER);

        // The preview and the next update both use the forested row.
        assertEquals(Goods.of(GoodsType.LUMBER, 4),
                field.getProduction().orElseThrow());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.LUMBER, 0));
        // Net accounting includes the new forest worker and any other lumber
        // job of the colony; the update must store exactly that quote.
        final int quoted = colony.getGoodsStats()
                .getStatsByType(GoodsType.LUMBER).getNetProduction();
        assertEquals(4, field.getProduction().orElseThrow().getAmount());
        model.endTurn();
        model.endTurn();
        assertEquals(quoted,
                colony.getWarehouse().getGoods(GoodsType.LUMBER).getAmount());
        assertEquals(Goods.of(GoodsType.LUMBER, 4),
                field.getProduction().orElseThrow());
    }

}
