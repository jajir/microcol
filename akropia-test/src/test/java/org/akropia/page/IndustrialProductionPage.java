package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.akropia.model.GoodsType;
import org.akropia.model.PlaceConstructionSlot;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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
        Node worker = getNodeFinder().lookup("#colonyUnit-" + unitId).query();
        revealGameContent(worker, canvas);
        getRobot().drag(worker, MouseButton.PRIMARY)
                .dropTo(canvas.localToScreen(51, 16));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId)
                        .getPlace() instanceof PlaceConstructionSlot);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyForecast(GoodsType type, int stock, int change) {
        List<VBox> panels = getListOfNodes(".warehouseGoods");
        VBox panel = panels.get(GoodsType.BUYABLE_GOOD_TYPES.indexOf(type));
        revealGameContent(panel);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            Label amount = (Label) panel.lookup(".amount");
            Label difference = (Label) panel.lookup(".diff");
            assertEquals(String.valueOf(stock), amount.getText(), type.name());
            assertEquals("("
                    + (change > 0 ? "+" + change
                            : change < 0 ? "−" + Math.abs(change) : "0")
                    + " /turn)", difference.getText(), type.name());
            assertEquals(stock, getModel().getColonyById(0).orElseThrow()
                    .getWarehouse().getGoods(type).getAmount());
            if (change != 0) {
                assertEquals(Color.web(change < 0 ? "#b13224" : "#316b2f"),
                        difference.getTextFill(), type + " change color");
            }
            Node viewport = panel.getScene().lookup(".screen-scroll .viewport");
            Bounds visible = viewport.localToScene(viewport.getBoundsInLocal());
            for (Label label : List.of(amount, difference)) {
                if (!label.getText().isEmpty()) {
                    Text rendered = (Text) label.lookup(".text");
                    assertTrue(label.isVisible() && rendered.isVisible(),
                            type + " forecast must be visible");
                    assertEquals(label.getText(), rendered.getText(),
                            type + " forecast must not be truncated");
                    Bounds bounds = rendered
                            .localToScene(rendered.getBoundsInLocal());
                    assertTrue(visible.contains(bounds),
                            type + " forecast must fit inside the viewport: "
                                    + bounds + " within " + visible);
                }
            }
        });
    }
}
