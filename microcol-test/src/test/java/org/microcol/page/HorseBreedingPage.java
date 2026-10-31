package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Warehouse forecasts and food-limited breeding observations. */
public final class HorseBreedingPage extends AbstractScreen {
    public HorseBreedingPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public int forecastFood() {
        return colony().getGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter();
    }

    public int population() {
        return colony().getUnitsInsideColony().size();
    }

    public void prepareStocks(int horses, int foodAfterResidents) {
        getRobot().interact(() -> {
            Colony colony = colony();
            int produced = colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                    .getRowProduction();
            int required = colony.getRequiredFoodPerTurn();
            colony.getWarehouse().removeGoods(
                    colony.getWarehouse().getGoods(GoodsType.HORSE));
            colony.getWarehouse().addGoods(Goods.of(GoodsType.HORSE, horses));
            colony.getWarehouse().removeGoods(
                    colony.getWarehouse().getGoods(GoodsType.CORN));
            colony.getWarehouse().addGoods(Goods.of(GoodsType.CORN,
                    foodAfterResidents - produced + required));
        });
    }

    public void verifyPreview(int horseStock, int horseIncrease) {
        final VBox panel = getNodeFinder().lookup("#colony-good-HORSE")
                .queryAs(VBox.class);
        final Label amount = (Label) panel.lookup(".amount");
        final HBox values = (HBox) amount.getParent();
        assertEquals(String.valueOf(horseStock), amount.getText());
        assertEquals(horseIncrease == 0 ? "" : "+" + horseIncrease,
                ((Label) values.getChildren().get(1)).getText());
        assertEquals(horseIncrease, colony().getGoodsStats()
                .getStatsByType(GoodsType.HORSE).getRowProduction());
    }

    public void verifyStocks(int horses, int food) {
        assertEquals(horses,
                colony().getWarehouse().getGoods(GoodsType.HORSE).getAmount());
        assertEquals(food,
                colony().getWarehouse().getGoods(GoodsType.CORN).getAmount());
    }

    public void verifyBornOutside(int beforeInside, int expectedOutside) {
        assertEquals(beforeInside, population());
        assertEquals(expectedOutside,
                getModel().getUnitsAt(colony().getLocation()).stream()
                        .filter(unit -> !unit.isShip()).count());
    }
}
