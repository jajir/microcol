package org.microcol.gui.colony;

import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.Colony;
import org.microcol.model.GoodType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class PanelUnitWithContextMenu {

    private final Pane pane;

    private final Unit unit;

    private final Colony colony;

    private final ContextMenu contextMenu;

    private final ColonyDialogCallback colonyDialogCallbback;

    private final Canvas canvas;

    public PanelUnitWithContextMenu(final ImageProvider imageProvider, final Unit unit,
            final Colony colony, final ColonyDialogCallback colonyDialog) {
        this.unit = Preconditions.checkNotNull(unit);
        this.colony = Preconditions.checkNotNull(colony);
        this.colonyDialogCallbback = Preconditions.checkNotNull(colonyDialog);

        canvas = new Canvas(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
        repaint();
        pane = new Pane(canvas);
        pane.setOnDragDetected(mouseEvent -> {
            final Image image = imageProvider.getUnitImage(unit);
            final Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image).addTransferFromOutsideColony().addUnit(unit)
                    .build();
            mouseEvent.consume();
        });
        pane.setOnContextMenuRequested(this::onContextMenuRequested);
        pane.setOnMousePressed(this::onMousePressed);

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
                    colonyDialogCallbback.repaint();
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int muskets = colony.getColonyWarehouse().getGoodAmmount(GoodType.MUSKET);
                if (UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT <= muskets) {
                    final MenuItem menuItem = new MenuItem("Equip with muskets");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithMuskets();
                        colonyDialogCallbback.repaint();
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
            if (fc.isMounted()) {
                final MenuItem menuItem = new MenuItem("Dismount horses");
                menuItem.setOnAction(evt -> {
                    fc.unequipWithHorses();
                    colonyDialogCallbback.repaint();
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int horses = colony.getColonyWarehouse().getGoodAmmount(GoodType.HORSE);
                if (UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT <= horses) {
                    final MenuItem menuItem = new MenuItem("Mount horses");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithHorses();
                        colonyDialogCallbback.repaint();
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
            if (fc.isHoldingTools()) {
                final MenuItem menuItem = new MenuItem("Unload tools");
                menuItem.setOnAction(evt -> {
                    fc.unequipWithTools();
                    colonyDialogCallbback.repaint();
                });
                contextMenu.getItems().add(menuItem);
            } else {
                final int tools = colony.getColonyWarehouse().getGoodAmmount(GoodType.TOOLS);
                if (tools > 0) {
                    final MenuItem menuItem = new MenuItem("Equip with tools");
                    menuItem.setOnAction(evt -> {
                        fc.equipWithTools();
                        colonyDialogCallbback.repaint();
                    });
                    contextMenu.getItems().add(menuItem);
                }
            }
        }
        contextMenu.show(pane, event.getScreenX(), event.getScreenY());
        repaint();
    }

    public void repaint() {
        colonyDialogCallbback.paintUnit(canvas, unit);
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
    public Pane getPane() {
        return pane;
    }
}
