package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import org.microcol.gui.Point;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.gui.dialog.DialogOrdainMissionary;
import org.microcol.gui.dialog.OrdinationMsg;
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
import javafx.scene.input.KeyCode;
import javafx.geometry.Side;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class PanelOutsideColonyUnit implements JavaFxComponent {

    public final static String UNIT_AT_PIER_STYLE = "unitInPort";

    private final Pane mainPane;

    private final Unit unit;

    private final Colony colony;

    private final DialogEquipment equipmentDialog;

    private final DialogOrdainMissionary ordinationDialog;

    private final I18n i18n;

    private final ContextMenu contextMenu;

    private final PaintService paintService;

    private final EventBus eventBus;

    private final Canvas canvas;

    PanelOutsideColonyUnit(final ImageProvider imageProvider,
            final EventBus eventBus, final PaintService paintService,
            final Colony colony, final Unit unit,
            final DialogEquipment equipmentDialog, final I18n i18n,
            final DialogOrdainMissionary ordinationDialog) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.colony = Preconditions.checkNotNull(colony);
        this.equipmentDialog = Preconditions.checkNotNull(equipmentDialog);
        this.ordinationDialog = Preconditions.checkNotNull(ordinationDialog);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.unit = Preconditions.checkNotNull(unit);

        canvas = new Canvas(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        repaint();
        mainPane = new Pane(canvas);
        mainPane.getStyleClass().add(UNIT_AT_PIER_STYLE);
        mainPane.setId("colonyUnit-" + unit.getId());
        mainPane.setFocusTraversable(unit instanceof UnitFreeColonist);
        if (unit instanceof UnitFreeColonist) {
            mainPane.setAccessibleText(i18n.get(ColonyMsg.jobContext));
        }
        mainPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.CONTEXT_MENU || (event.isShiftDown()
                    && event.getCode() == KeyCode.F10)) {
                showEquipmentMenu();
                event.consume();
            }
        });
        mainPane.setOnDragDetected(mouseEvent -> {
            final Image image = imageProvider.getUnitImage(unit);
            final Dragboard db = mainPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image)
                    .addTransferFromOutsideColony().addUnit(unit).build();
            mouseEvent.consume();
        });
        mainPane.setOnContextMenuRequested(this::onContextMenuRequested);
        mainPane.setOnMousePressed(this::onMousePressed);

        contextMenu = new ContextMenu();
    }

    private void onContextMenuRequested(final ContextMenuEvent event) {
        showEquipmentMenu();
        event.consume();
    }

    private void showEquipmentMenu() {
        if (!(unit instanceof UnitFreeColonist)) {
            return;
        }
        mainPane.requestFocus();
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        contextMenu.getItems().clear();
        addEquipmentChoice(colonist, GoodsType.MUSKET,
                !colonist.isHoldingGuns());
        addEquipmentChoice(colonist, GoodsType.HORSE, !colonist.isMounted());
        addEquipmentChoice(colonist, GoodsType.TOOLS,
                !colonist.isHoldingTools());
        final MenuItem work = new MenuItem(i18n.get(ColonyMsg.jobOpen));
        work.setId("chooseWork");
        work.setOnAction(
                event -> eventBus.post(new ShowColonyJobChoicesEvent(unit)));
        contextMenu.getItems().add(work);
        final MenuItem ordain = new MenuItem(
                i18n.get(OrdinationMsg.ordainMenu));
        ordain.setId("colony-ordain-" + unit.getId());
        ordain.setOnAction(event -> {
            ordinationDialog.show(unit);
            if (colony.isValid()) {
                eventBus.post(new RepaintColonyEvent());
            } else {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
            }
        });
        contextMenu.getItems().add(ordain);
        contextMenu.show(mainPane, Side.BOTTOM, 0, 0);
    }

    private void addEquipmentChoice(final UnitFreeColonist colonist,
            final GoodsType type, final boolean equip) {
        final MenuItem item = new MenuItem(
                i18n.get(DialogEquipment.actionKey(type, equip)));
        item.setId("equipment-" + type.name());
        item.setOnAction(event -> {
            equipmentDialog.show(colonist, type, equip);
            if (colony.isValid()) {
                eventBus.post(new RepaintColonyEvent());
            } else {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
            }
        });
        contextMenu.getItems().add(item);
    }

    private void repaint() {
        final GraphicsContext graphics = canvas.getGraphicsContext2D();
        paintService.paintUnitWithoutFlag(graphics, Point.ZERO, unit);
    }

    @SuppressWarnings("unused")
    private void onMousePressed(final MouseEvent event) {
        if (mainPane.isFocusTraversable()) {
            // Keep the scroll viewport from taking the colonist's keyboard
            // focus.
            mainPane.requestFocus();
            event.consume();
        }
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
