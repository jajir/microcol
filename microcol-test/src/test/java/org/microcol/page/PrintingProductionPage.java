package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.microcol.model.*;
import javafx.scene.control.Labeled;

/** Visible town-hall output and saved bell balances. */
public final class PrintingProductionPage extends AbstractScreen {
    public PrintingProductionPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void prepareNewspaperMaterials() {
        getRobot().interact(() -> {
            colony().getWarehouse().removeGoods(
                    colony().getWarehouse().getGoods(GoodsType.HAMMERS));
            colony().getWarehouse().addGoods(Goods.of(GoodsType.HAMMERS, 117));
            colony().getWarehouse().removeGoods(
                    colony().getWarehouse().getGoods(GoodsType.TOOLS));
            colony().getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, 50));
        });
    }

    public void verifyBells(int output, int stock, Integer... workerIds) {
        Labeled label = getNodeFinder()
                .lookup("#id_town_hall .production .diffPositive")
                .queryLabeled();
        assertTrue(label.isVisible());
        assertEquals("+" + output, label.getText());
        assertEquals(output, colony().getGoodsStats()
                .getStatsByType(GoodsType.BELL).getRowProduction());
        assertEquals(stock,
                colony().getWarehouse().getGoods(GoodsType.BELL).getAmount());
        Construction hall = colony().getConstructions().stream()
                .filter(c -> c.getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow();
        assertEquals(Arrays.asList(workerIds),
                hall.getConstructionSlots().stream()
                        .filter(slot -> !slot.isEmpty())
                        .map(slot -> slot.getUnit().getId())
                        .collect(Collectors.toList()));
    }
}
