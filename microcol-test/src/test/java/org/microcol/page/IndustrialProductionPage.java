package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.microcol.model.GoodsType;
import org.microcol.model.PlaceConstructionSlot;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Actual worker assignment and warehouse forecast observations. */
public final class IndustrialProductionPage extends AbstractScreen {
    public IndustrialProductionPage(TestContext context) {
        super(context);
    }

    public void assignWorker(int unitId, String buildingId) throws Exception {
        Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        Canvas canvas = (Canvas) building.getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        getRobot().drag("#colonyUnit-" + unitId, MouseButton.PRIMARY)
                .dropTo(canvas.localToScreen(68, 104));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId)
                        .getPlace() instanceof PlaceConstructionSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyForecast(GoodsType type, int stock, int change) {
        List<VBox> panels = getListOfNodes(".warehouseGoods");
        HBox values = (HBox) panels
                .get(GoodsType.BUYABLE_GOOD_TYPES.indexOf(type)).getChildren()
                .get(1);
        assertEquals(String.valueOf(stock),
                ((Label) values.getChildren().get(0)).getText());
        assertEquals(change == 0 ? "" : (change > 0 ? "+" : "") + change,
                ((Label) values.getChildren().get(1)).getText());
        assertEquals(stock, getModel().getColonyById(0).orElseThrow()
                .getWarehouse().getGoods(type).getAmount());
    }
}
