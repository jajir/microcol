package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.UnitType;

/** Controlled construction funding and actual completed-ship observations. */
public final class CoastalFacilitiesPage extends AbstractScreen {
    public CoastalFacilitiesPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void prepareProjectStocks(int hammers, int tools) {
        getRobot().interact(() -> {
            colony().getWarehouse().removeGoods(
                    colony().getWarehouse().getGoods(GoodsType.HAMMERS));
            colony().getWarehouse()
                    .addGoods(Goods.of(GoodsType.HAMMERS, hammers));
            colony().getWarehouse().removeGoods(
                    colony().getWarehouse().getGoods(GoodsType.TOOLS));
            colony().getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, tools));
        });
    }

    public void verifyShips(UnitType type, int count) {
        assertEquals(count,
                getModel().getUnitsAt(colony().getLocation()).stream()
                        .filter(unit -> unit.getType() == type
                                && unit.getOwner() == colony().getOwner())
                        .count());
    }

    public void verifyStocks(int hammers, int tools) {
        assertEquals(hammers, colony().getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(tools,
                colony().getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
    }
}
