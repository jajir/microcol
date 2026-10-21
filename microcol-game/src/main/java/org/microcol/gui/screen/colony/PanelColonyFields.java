package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.dialog.NativeLandDialog;
import org.microcol.gui.dialog.NativeLandGuard;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.FontService;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
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

    private final Logger logger = LoggerFactory
            .getLogger(PanelColonyFields.class);

    private final Canvas canvas;

    private Colony colony;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final ClickableArea clickableArea = new ClickableArea();

    private final ContextMenu contextMenu;

    private final PaintService paintService;

    private final EventBus eventBus;

    private final NativeLandGuard nativeLand;

    private final VBox mainPanel = new VBox();

    private final I18n i18n;

    @Inject
    PanelColonyFields(final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final PaintService paintService, final EventBus eventBus,
            final I18n i18n, final FontService fontService,
            final NativeLandGuard nativeLand) {
        this.nativeLand = Preconditions.checkNotNull(nativeLand);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        final int size = 3 * TILE_WIDTH_IN_PX;

        canvas = new Canvas(size, size);
        canvas.getGraphicsContext2D().setFont(fontService.getDefault(12));
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
                .map(loc -> colony.getColonyFieldInDirection(loc))
                .filter(ColonyField::isNotEmpty).ifPresent(
                        colonyField -> showContextMenuAtColonyField(colonyField,
                                event));
    }

    private void showContextMenuAtColonyField(final ColonyField colonyField,
            final ContextMenuEvent event) {
        final Unit expectedWorker = colonyField.getUnit().orElseThrow();
        contextMenu.getItems().clear();
        colonyField.getTerrain().getProduction().stream()
                .forEach(production -> {
                    final MenuItem item = new MenuItem(
                            production.getGoodsType().name() + "   "
                                    + production.getProduction());
                    item.setOnAction(evt -> {
                        try {
                            colonyField.setProducedGoodsType(expectedWorker,
                                    production.getGoodsType());
                            eventBus.post(new RepaintColonyEvent());
                        } catch (IllegalArgumentException
                                | IllegalStateException rejected) {
                            showFieldProblem(ColonyMsg.fieldChanged);
                        }
                    });
                    contextMenu.getItems().add(item);
                });
        final MenuItem work = new MenuItem(i18n.get(ColonyMsg.jobOpen));
        work.setId("chooseWork");
        work.setOnAction(action -> eventBus
                .post(new ShowColonyJobChoicesEvent(expectedWorker)));
        contextMenu.getItems().add(work);
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
        oLoc.map(loc -> colony.getColonyFieldInDirection(loc))
                .filter(ColonyField::isNotEmpty).ifPresent(colonyField -> {
                    dragStartedAtColonyField(colonyField, oLoc.get());
                    event.consume();
                });
    }

    private void dragStartedAtColonyField(final ColonyField colonyField,
            final Direction direction) {
        final Unit unit = colonyField.getUnit().get();
        final Image image = imageProvider.getUnitImage(unit);
        final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image)
                .addTransferFromColonyField(direction.getVector()).addUnit(unit)
                .build();
    }

    private void onDragOver(final DragEvent event) {
        final Optional<Unit> unit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .getUnit();
        final Optional<ColonyField> field = clickableArea
                .getDirection(Point.of(event.getX(), event.getY()))
                .map(colony::getColonyFieldInDirection);
        if (unit.isPresent() && field.isPresent()
                && field.get().canAccept(unit.get(), GoodsType.CORN)) {
            event.acceptTransferModes(TransferMode.MOVE);
            eventBus.post(new StatusBarMessageEvent(Source.COLONY));
        } else {
            event.acceptTransferModes(TransferMode.NONE);
            if (unit.isPresent() && field.isPresent()) {
                showFieldProblem(
                        field.get().isMissingDock() ? ColonyMsg.fieldMissingDock
                                : field.get().isWorkedByAnotherColony()
                                        ? ColonyMsg.fieldWorked
                                        : ColonyMsg.fieldChanged);
            }
        }
        event.consume();
    }

    private void onDragDropped(final DragEvent event) {
        event.setDropCompleted(false);
        final Optional<Unit> unit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .getUnit();
        clickableArea.getDirection(Point.of(event.getX(), event.getY()))
                .map(colony::getColonyFieldInDirection).ifPresent(field -> {
                    if (unit.isEmpty()) {
                        return;
                    }
                    // The land question comes only for a field the worker may
                    // take; any other drop is refused as before.
                    if (field.canAccept(unit.get(), GoodsType.CORN)
                            && isUnpaidClaim(field)) {
                        // Ask after the drop gesture has finished; canceling
                        // leaves the worker where it was.
                        event.setDropCompleted(true);
                        final javafx.scene.Node source = (javafx.scene.Node) event
                                .getGestureSource();
                        source.addEventHandler(DragEvent.DRAG_DONE,
                                new javafx.event.EventHandler<DragEvent>() {
                                    @Override
                                    public void handle(final DragEvent done) {
                                        source.removeEventHandler(
                                                DragEvent.DRAG_DONE, this);
                                        Platform.runLater(
                                                () -> placeAfterQuestion(
                                                        unit.get(), field));
                                    }
                                });
                        return;
                    }
                    if (place(unit.get(), field,
                            NativeLandDialog.Result.NOT_ASKED)) {
                        event.setDropCompleted(true);
                    }
                });
        event.consume();
    }

    private boolean isUnpaidClaim(final ColonyField field) {
        return gameModelController.getModel()
                .quoteNativeLand(gameModelController.getHumanPlayer(),
                        field.getLocation())
                .filter(quote -> quote.getStatus() == NativeLandStatus.UNPAID)
                .isPresent();
    }

    private void placeAfterQuestion(final Unit unit, final ColonyField field) {
        final NativeLandDialog.Result land = nativeLand.proceed(
                gameModelController.getModel(),
                gameModelController.getHumanPlayer(), field.getLocation());
        if (land != NativeLandDialog.Result.CANCELED) {
            place(unit, field, land);
        }
    }

    private boolean place(final Unit unit, final ColonyField field,
            final NativeLandDialog.Result land) {
        try {
            unit.placeToColonyField(field, GoodsType.CORN);
            eventBus.post(new RepaintColonyEvent());
            return true;
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            final ColonyMsg reason = field.isMissingDock()
                    ? ColonyMsg.fieldMissingDock
                    : field.isWorkedByAnotherColony() ? ColonyMsg.fieldWorked
                            : ColonyMsg.fieldChanged;
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land, i18n.get(reason)),
                    Source.COLONY));
            return false;
        }
    }

    private void showFieldProblem(final ColonyMsg message) {
        eventBus.post(
                new StatusBarMessageEvent(i18n.get(message), Source.COLONY));
    }

    public void setColony(final Colony colony) {
        contextMenu.hide();
        this.colony = Preconditions.checkNotNull(colony);
        paint(canvas.getGraphicsContext2D());
    }

    private void paint(final GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        colony.getColonyFields()
                .forEach(colonyField -> paintColonyField(gc, colonyField));
        final Point colonyPoint = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        paintService.paintTerrainOnTile(
                gc, colonyPoint, colony.getLocation(), gameModelController
                        .getModel().getMap().getTerrainAt(colony.getLocation()),
                false);
        paintService.paintColony(gc, colonyPoint, colony, false);
    }

    private void paintColonyField(final GraphicsContext gc,
            final ColonyField colonyField) {
        if (!colonyField.isValid()) {
            return;
        }
        final Terrain terrain = colonyField.getTerrain();
        final ColonyFieldTile colonyFieldTile = ColonyFieldTile
                .ofDirection(colonyField.getDirection());
        final Point point = colonyFieldTile.getTopLeftCorner();
        paintService.paintTerrainOnTile(gc, point, colonyField.getLocation(),
                terrain, false);
        if (!colonyField.isEmpty()) {
            final Goods production = colonyField.getProduction().get();
            final Unit unit = colonyField.getUnit().get();
            paintService.paintUnitWithoutFlag(gc, point, unit);
            gc.drawImage(imageProvider.getGoodsTypeImage(production.getType()),
                    point.getX(), point.getY(), 25, 25);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.fillText("x " + production.getAmount(), point.getX() + 10,
                    point.getY() + 28);
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
