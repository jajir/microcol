package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.model.ColonyField;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

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
    public void verifyBaseline(final Location location, final int expectedCorn,
            final int expectedFur, final int expectedLumber) {
        final Terrain terrain = getModel().getMap().getTerrainAt(location);
        assertEquals(expectedCorn, terrain.canProduceAmmount(GoodsType.CORN),
                "Unexpected corn output at " + location);
        assertEquals(expectedFur, terrain.canProduceAmmount(GoodsType.FUR),
                "Unexpected fur output at " + location);
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
