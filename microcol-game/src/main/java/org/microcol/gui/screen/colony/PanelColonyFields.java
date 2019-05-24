package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
@Singleton
public final class PanelColonyFields implements JavaFxComponent {

    private final Logger logger = LoggerFactory.getLogger(PanelColonyFields.class);

    private final Canvas canvas;

    private Colony colony;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final ClickableArea clickableArea = new ClickableArea();

    private final ContextMenu contextMenu;

    private final PaintService paintService;

    private final EventBus eventBus;

    private final VBox mainPanel = new VBox();

    @Inject
    PanelColonyFields(final ImageProvider imageProvider,
            final GameModelController gameModelController, final PaintService paintService,
            final EventBus eventBus) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        final int size = 3 * TILE_WIDTH_IN_PX;

        canvas = new Canvas(size, size);
        canvas.setOnDragOver(this::onDragOver);
        canvas.setOnDragDropped(this::onDragDropped);
        canvas.setOnDragDetected(this::onDragDetected);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnContextMenuRequested(this::onContextMenuRequested);

        contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("popup");
        contextMenu.setAutoHide(true);

        mainPanel.getChildren().add(canvas);
        mainPanel.getStyleClass().add("colony-fields");
    }

    private void onContextMenuRequested(final ContextMenuEvent event) {
        clickableArea.getDirection(Point.of(event.getX(), event.getY()))
                .map(loc -> colony.getColonyFieldInDirection(loc)).filter(ColonyField::isNotEmpty)
                .ifPresent(colonyField -> showContextMenuAtColonyField(colonyField, event));
    }

    private void showContextMenuAtColonyField(final ColonyField colonyField,
            final ContextMenuEvent event) {
        contextMenu.getItems().clear();
        colonyField.getTerrain().getProduction().stream().forEach(production -> {
            final MenuItem item = new MenuItem(
                    production.getGoodsType().name() + "   " + production.getProduction());
            item.setOnAction(evt -> {
                colonyField.setProducedGoodsType(production.getGoodsType());
                eventBus.post(new RepaintColonyEvent());
            });
            contextMenu.getItems().add(item);
        });
        contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
    }

    @SuppressWarnings("unused")
    private void onMousePressed(final MouseEvent event) {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    private void onDragDetected(final MouseEvent event) {
        logger.debug("Drag detected");
        final Optional<Direction> oLoc = clickableArea
                .getDirection(Point.of(event.getX(), event.getY()));
        oLoc.map(loc -> colony.getColonyFieldInDirection(loc)).filter(ColonyField::isNotEmpty)
                .ifPresent(colonyField -> {
                    dragStartedAtColonyField(colonyField, oLoc.get());
                    event.consume();
                });
    }

    private void dragStartedAtColonyField(final ColonyField colonyField,
            final Direction direction) {
        final Unit unit = colonyField.getUnit().get();
        final Image image = imageProvider.getUnitImage(unit);
        final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image).addTransferFromColonyField(direction.getVector())
                .addUnit(unit).build();
    }

    private void onDragOver(final DragEvent event) {
        logger.debug("Drag Over");
        if (isItUnit(event.getDragboard())) {
            final Optional<Direction> oLoc = clickableArea
                    .getDirection(Point.of(event.getX(), event.getY()));
            if (canFieldAcceptDraggedUnit(oLoc)) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
                return;
            }

        }
        event.acceptTransferModes(TransferMode.NONE);
        event.consume();
    }

    private boolean canFieldAcceptDraggedUnit(final Optional<Direction> oLoc) {
        return oLoc.map(loc -> colony.getColonyFieldInDirection(loc).isEmpty()).orElse(false);
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Drag dropped");
        clickableArea.getDirection(Point.of(event.getX(), event.getY()))
                .map(loc -> colony.getColonyFieldInDirection(loc)).filter(ColonyField::isEmpty)
                .ifPresent(colonyField -> dragDropped(colonyField, event));
        event.consume();
    }

    private void dragDropped(final ColonyField colonyField, final DragEvent event) {
        ClipboardEval.make(gameModelController.getModel(), event.getDragboard()).getUnit()
                .ifPresent(unit -> {
                    unit.placeToColonyField(colonyField, GoodsType.CORN);
                    event.setDropCompleted(true);
                    eventBus.post(new RepaintColonyEvent());
                });
    }

    private boolean isItUnit(final Dragboard db) {
        logger.debug("Drag over unit id '" + db.getString() + "'.");
        return ClipboardEval.make(gameModelController.getModel(), db).getUnit().isPresent();
    }

    public void setColony(final Colony colony) {
        this.colony = Preconditions.checkNotNull(colony);
        paint(canvas.getGraphicsContext2D());
    }

    private void paint(final GraphicsContext gc) {
        colony.getColonyFields().forEach(colonyField -> paintColonyField(gc, colonyField));
        final Point colonyPoint = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        paintService.paintTerrainOnTile(gc, colonyPoint, colony.getLocation(),
                gameModelController.getModel().getMap().getTerrainAt(colony.getLocation()), false);
        paintService.paintColony(gc, colonyPoint, colony, false);
    }

    private void paintColonyField(final GraphicsContext gc, final ColonyField colonyField) {
        final Terrain terrain = colonyField.getTerrain();
        final ColonyFieldTile colonyFieldTile = ColonyFieldTile
                .ofDirection(colonyField.getDirection());
        final Point point = colonyFieldTile.getTopLeftCorner();
        paintService.paintTerrainOnTile(gc, point, colonyField.getLocation(), terrain, false);
        if (!colonyField.isEmpty()) {
            final Goods production = colonyField.getProduction().get();
            final Unit unit = colonyField.getUnit().get();
            paintService.paintUnitWithoutFlag(gc, point, unit);
            gc.drawImage(imageProvider.getGoodsTypeImage(production.getType()), point.getX(),
                    point.getY(), 25, 25);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.fillText("x " + production.getAmount(), point.getX() + 10, point.getY() + 28);
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
