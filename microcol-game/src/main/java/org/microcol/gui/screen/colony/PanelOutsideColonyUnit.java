package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Colony;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class PanelOutsideColonyUnit implements JavaFxComponent {

    public final static String UNIT_AT_PIER_STYLE = "unitInPort";

    private final Pane mainPane;

    private final Unit unit;

    private final Colony colony;

    private final ContextMenu contextMenu;

    private final PaintService paintService;

    private final EventBus eventBus;

    private final Canvas canvas;

    PanelOutsideColonyUnit(final ImageProvider imageProvider, final EventBus eventBus,
            final PaintService paintService, final Colony colony, final Unit unit) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.colony = Preconditions.checkNotNull(colony);
        this.unit = Preconditions.checkNotNull(unit);

        canvas = new Canvas(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        repaint();
        mainPane = new Pane(canvas);
        mainPane.getStyleClass().add(UNIT_AT_PIER_STYLE);
        mainPane.setOnDragDetected(mouseEvent -> {
            final Image image = imageProvider.getUnitImage(unit);
            final Dragboard db = mainPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image).addTransferFromOutsideColony().addUnit(unit)
                    .build();
            mouseEvent.consume();
        });
        mainPane.setOnContextMenuRequested(this::onContextMenuRequested);
        mainPane.setOnMousePressed(this::onMousePressed);

        contextMenu = new ContextMenu();
    }

    private void onContextMenuRequested(final ContextMenuEvent event) {
        contextMenu.getItems().clear();
        if (unit.canHoldGuns()) {
            final UnitFreeColonist fc = (UnitFreeColonist) unit;
            if (fc.isHoldingGuns()) {
                final MenuItem menuItem = new MenuItem("Unload muskets");
                menuItem.setOnAction(evt -> {
                    fc.unequipWithMuskets();
                    eventBus.post(new RepaintColonyEvent());
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int muskets = colony.getWarehouse().getGoods(GoodsType.MUSKET).getAmount();
                if (UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT <= muskets) {
                    final MenuItem menuItem = new MenuItem("Equip with muskets");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithMuskets();
                        eventBus.post(new RepaintColonyEvent());
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
            if (fc.isMounted()) {
                final MenuItem menuItem = new MenuItem("Dismount horses");
                menuItem.setOnAction(evt -> {
                    fc.unequipWithHorses();
                    eventBus.post(new RepaintColonyEvent());
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int horses = colony.getWarehouse().getGoods(GoodsType.HORSE).getAmount();
                if (UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT <= horses) {
                    final MenuItem menuItem = new MenuItem("Mount horses");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithHorses();
                        eventBus.post(new RepaintColonyEvent());
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
            if (fc.isHoldingTools()) {
                final MenuItem menuItem = new MenuItem("Unload tools");
                menuItem.setOnAction(evt -> {
                    fc.unequipWithTools();
                    eventBus.post(new RepaintColonyEvent());
                    eventBus.post(new RepaintColonyEvent());
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int tools = colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount();
                if (tools > 0) {
                    final MenuItem menuItem = new MenuItem("Equip with tools");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithTools();
                        eventBus.post(new RepaintColonyEvent());
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
        }
        contextMenu.show(mainPane, event.getScreenX(), event.getScreenY());
        repaint();
    }

    private void repaint() {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        paintService.paintUnitWithoutFlag(graphics, Point.ZERO, unit);
    }

    @SuppressWarnings("unused")
    private void onMousePressed(final MouseEvent event) {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    /**
     * @return the pane
     */
    @Override
    public Region getContent() {
        return mainPane;
    }
}
