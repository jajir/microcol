package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.akropia.model.ColonyField;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.Terrain;
import org.akropia.model.Unit;

/** Terrain baseline and colony stock checks for field production. */
public final class TerrainProductionPage extends AbstractScreen {

    public TerrainProductionPage(final TestContext context) {
        super(context);
    }

    /** Check the worker, square and exact output of one colony field job. */
    public void verifyFieldJob(final int colonyId, final int workerId,
            final Location expectedLocation, final Goods expectedGoods) {
        final ColonyField field = getModel()
                .getColonyById(colonyId).orElseThrow().getColonyFields()
                .stream().filter(candidate -> candidate.getUnit()
                        .map(Unit::getId).orElse(-1) == workerId)
                .findFirst().orElseThrow();
        assertEquals(expectedLocation, field.getLocation(),
                "Unexpected field of worker " + workerId);
        assertEquals(expectedGoods, field.getProduction().orElseThrow(),
                "Unexpected production of worker " + workerId);
    }

    /** Check the aid-card baseline of a forested grassland square. */
    public void verifyBaseline(final Location location, final int expectedGrain,
            final int expectedClay, final int expectedLumber) {
        final Terrain terrain = getModel().getMap().getTerrainAt(location);
        assertEquals(expectedGrain, terrain.canProduceAmmount(GoodsType.GRAIN),
                "Unexpected grain output at " + location);
        assertEquals(expectedClay, terrain.canProduceAmmount(GoodsType.CLAY),
                "Unexpected clay output at " + location);
        assertEquals(expectedLumber,
                terrain.canProduceAmmount(GoodsType.LUMBER),
                "Unexpected lumber output at " + location);
    }

    public void verifyWarehouseGoods(final int colonyId,
            final Goods expectedGoods) {
        assertEquals(expectedGoods.getAmount(),
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(expectedGoods.getType()).getAmount(),
                "Unexpected " + expectedGoods.getType() + " in colony "
                        + colonyId);
    }
}
