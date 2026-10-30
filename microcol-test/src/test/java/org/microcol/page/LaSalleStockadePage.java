package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.microcol.model.*;
import javafx.scene.Node;

/** La Salle's free stockade in the model and on the colony screen. */
public final class LaSalleStockadePage extends AbstractScreen {
    public LaSalleStockadePage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void verifyMembers(List<Founder> expected) {
        assertEquals(expected,
                getModel().getCurrentPlayer().getCongressMembers());
    }

    /** Counts the stockade and requires that no better defense replaced it. */
    public void verifyStockadeBuilt(long expectedCount) {
        assertEquals(expectedCount, colony().getConstructions().stream().filter(
                building -> building.getType() == ConstructionType.STOCKADE)
                .count());
        assertFalse(colony().wasConstructionBuilded(ConstructionType.FORT));
        assertFalse(colony().wasConstructionBuilded(ConstructionType.FORTRESS));
    }

    /** The colony screen must be open; the stockade is the id_fort picture. */
    public void verifyStockadeShown(boolean expected) {
        Node fort = getNodeFinder().lookup("#id_fort").query();
        assertTrue(fort.isVisible());
        assertEquals(expected,
                !fort.getStyleClass().contains("building-empty"));
    }

    public void verifyPopulation(int expected) {
        assertEquals(expected, colony().getUnitsInsideColony().size());
    }

    public void verifyStock(int hammers, int tools, int gold) {
        assertEquals(hammers, colony().getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(tools,
                colony().getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(gold, getModel().getCurrentPlayer().getGold());
        assertTrue(
                colony().getColonyBuildingQueue().getBuildingQueue().isEmpty());
    }
}
