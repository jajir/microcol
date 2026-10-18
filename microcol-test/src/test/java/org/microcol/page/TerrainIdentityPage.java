package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.model.Colony;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;

/** Actual model terrain identity, forest state and ship positions. */
public final class TerrainIdentityPage extends AbstractScreen {

    public TerrainIdentityPage(final TestContext context) {
        super(context);
    }

    private int foodBefore;

    private int netFoodBefore;

    /**
     * Remember the stored food and the colony's own net food quote, which
     * includes fishing, consumption and centre production.
     */
    public void rememberFoodAndNetProduction(final int colonyId) {
        final Colony colony = getModel().getColonyById(colonyId).orElseThrow();
        foodBefore = colony.getWarehouse().getGoods(GoodsType.CORN).getAmount();
        netFoodBefore = colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
    }

    public void verifyFoodAfterTurn(final int colonyId) {
        assertEquals(foodBefore + netFoodBefore,
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(GoodsType.CORN).getAmount(),
                "Stored food must change by the quoted net production");
    }

    public void verifyTerrain(final Location location,
            final TerrainType expectedType, final boolean expectedForest) {
        final Terrain terrain = getModel().getMap().getTerrainAt(location);
        assertEquals(expectedType, terrain.getTerrainType(),
                "Unexpected terrain at " + location);
        assertEquals(expectedForest, terrain.isHasTrees(),
                "Unexpected forest at " + location);
    }

    public void verifyShipAt(final int shipId,
            final Location expectedLocation) {
        assertEquals(expectedLocation,
                getModel().getUnitById(shipId).getLocation(),
                "Unexpected location of ship " + shipId);
    }
}
