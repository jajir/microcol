package org.akropia.gui.screen.colony;

import static org.akropia.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.Optional;

import org.akropia.gui.Point;
import org.akropia.gui.dialog.NativeLandDialog;
import org.akropia.gui.dialog.DialogMessage;
import org.akropia.gui.dialog.NativeLandGuard;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;
import org.akropia.gui.util.ClipboardEval;
import org.akropia.gui.util.ClipboardWritter;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.FontService;
import org.akropia.gui.util.PaintService;
import org.akropia.model.Colony;
import org.akropia.model.ColonyField;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.Direction;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.NativeLandStatus;
import org.akropia.model.Terrain;
import org.akropia.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.LocalizationHelper;
import org.akropia.model.Location;
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
    private final DialogMessage dropMessage;

    private final HBox mainPanel = new HBox(10);
    private final VBox overview = new VBox(4);
    private Runnable onSelect = () -> {
    };
    private final Label mapTitle = new Label();
    private final Label detailTitle = new Label();
    private final Canvas preview = new Canvas(TILE_WIDTH_IN_PX,
            TILE_WIDTH_IN_PX);
    private final Label tileName = new Label();
    private final Label workerName = new Label();
    private final Label yield = new Label();
    private final VBox output = new VBox(3);
    private final Label productHeader = new Label();
    private final Label yieldHeader = new Label();
    private final Label tileInfo = new Label();
    private final VBox production;
    private int selectedX = 1;
    private int selectedY = 1;

    private final I18n i18n;

    @Inject
    PanelColonyFields(final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final PaintService paintService, final EventBus eventBus,
            final I18n i18n, final FontService fontService,
            final NativeLandGuard nativeLand, final DialogMessage dropMessage) {
        this.dropMessage = Preconditions.checkNotNull(dropMessage);
        this.nativeLand = Preconditions.checkNotNull(nativeLand);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        eventBus.register(this);
        final int size = 3 * TILE_WIDTH_IN_PX;

        canvas = new Canvas(size, size);
        canvas.setId("colony-field-map");
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> {
            final KeyCode code = event.getCode();
            if (code == KeyCode.LEFT)
                selectedX = Math.max(0, selectedX - 1);
            else if (code == KeyCode.RIGHT)
                selectedX = Math.min(2, selectedX + 1);
            else if (code == KeyCode.UP)
                selectedY = Math.max(0, selectedY - 1);
            else if (code == KeyCode.DOWN)
                selectedY = Math.min(2, selectedY + 1);
            else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                output.getChildren().stream()
                        .filter(node -> ((RadioButton) node).isSelected())
                        .findFirst().ifPresent(javafx.scene.Node::requestFocus);
                event.consume();
                return;
            } else
                return;
            paint(canvas.getGraphicsContext2D());
            refreshDetail();
            event.consume();
        });
        canvas.getGraphicsContext2D().setFont(fontService.getDefault(12));
        canvas.setOnDragOver(this::onDragOver);
        canvas.setOnDragDropped(this::onDragDropped);
        canvas.setOnDragExited(event -> clearMovementDropHint());
        canvas.setOnDragDone(event -> clearMovementDropHint());
        canvas.setOnDragDetected(this::onDragDetected);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnContextMenuRequested(this::onContextMenuRequested);

        contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("popup");
        contextMenu.setAutoHide(true);

        overview.getChildren().setAll(canvas);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            onSelect.run();
            canvas.requestFocus();
        });
        canvas.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    onSelect.run();
                    Platform.runLater(this::revealProduction);
                });
        final HBox tile = new HBox(8, preview,
                new VBox(4, tileName, workerName));
        tile.setAlignment(Pos.CENTER_LEFT);
        final Region headerSpace = new Region();
        HBox.setHgrow(headerSpace, Priority.ALWAYS);
        final HBox columns = new HBox(8, productHeader, headerSpace,
                yieldHeader);
        columns.getStyleClass().add("field-output-header");
        production = new VBox(4, columns, output);
        production.setMaxWidth(480);
        production.setOnMouseEntered(
                event -> eventBus.post(new StatusBarMessageEvent(
                        i18n.get(ColonyMsg.outputHelp), Source.COLONY)));
        production.setOnMouseMoved(production.getOnMouseEntered());
        production.setOnMouseExited(event -> eventBus
                .post(new StatusBarMessageEvent(Source.COLONY)));
        final VBox detail = new VBox(4, detailTitle, tile, tileInfo, yield,
                production);
        detail.getStyleClass().add("tile-detail");
        detail.setMinWidth(200);
        detail.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detail, Priority.ALWAYS);
        tileName.setWrapText(true);
        workerName.setWrapText(true);
        yield.setWrapText(true);
        tileInfo.setWrapText(true);
        tileInfo.setId("colony-field-info");
        output.setId("colony-field-output");
        preview.setId("colony-field-preview");
        tileName.setId("colony-field-name");
        workerName.setId("colony-field-worker");
        yield.setId("colony-field-yield");
        mainPanel.getChildren().add(detail);
        mainPanel.getStyleClass().add("colony-fields");
    }

    Region getOverview() {
        return overview;
    }

    void onSelect(final Runnable action) {
        onSelect = action;
    }

    private Optional<Unit> fieldWorker(final ColonyField field) {
        if (field.isWorkedByAnotherColony()) {
            return Optional.empty();
        }
        return field.getUnit().or(() -> gameModelController.getModel()
                .getUnitsAt(field.getLocation()).stream()
                .filter(unit -> unit.getFishingColony().orElse(null) == colony)
                .findFirst());
    }

    private void onContextMenuRequested(final ContextMenuEvent event) {
        clickableArea.getDirection(fieldPoint(event.getX(), event.getY()))
                .map(loc -> colony.getColonyFieldInDirection(loc))
                .filter(field -> fieldWorker(field).isPresent()).ifPresent(
                        colonyField -> showContextMenuAtColonyField(colonyField,
                                event));
    }

    private void showContextMenuAtColonyField(final ColonyField colonyField,
            final ContextMenuEvent event) {
        final Unit expectedWorker = fieldWorker(colonyField).orElseThrow();
        contextMenu.getItems().clear();
        colonyField.getTerrain().getProduction().stream()
                .filter(production -> expectedWorker.isAtPlaceColonyField())
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
        final MenuItem cancel = new MenuItem(
                i18n.get(org.akropia.gui.Loc.cancelWork));
        cancel.setId("cancelColonyWork");
        cancel.setDisable(!expectedWorker.canCancelColonyWork());
        cancel.setOnAction(action -> {
            try {
                expectedWorker.cancelColonyWork();
                eventBus.post(new RepaintColonyEvent());
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                showFieldProblem(ColonyMsg.fieldChanged);
            }
        });
        final MenuItem returnHome = new MenuItem(
                i18n.get(org.akropia.gui.Loc.returnToColony));
        returnHome.setId("returnToColony");
        returnHome.setDisable(!expectedWorker.canReturnToWorkingColony());
        returnHome.setOnAction(action -> {
            try {
                expectedWorker.returnToWorkingColony();
                eventBus.post(new RepaintColonyEvent());
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                showFieldProblem(ColonyMsg.fieldChanged);
            }
        });
        contextMenu.getItems().addAll(cancel, returnHome);
        contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
    }

    @SuppressWarnings("unused")
    private void onMousePressed(final MouseEvent event) {
        canvas.requestFocus();
        selectedX = Math.min(2, Math.max(0, (int) (event.getX() / tileSize())));
        selectedY = Math.min(2, Math.max(0, (int) (event.getY() / tileSize())));
        paint(canvas.getGraphicsContext2D());
        refreshDetail();
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
        // Keep the enclosing ScrollPane from taking keyboard focus.
        event.consume();
    }

    private void onDragDetected(final MouseEvent event) {
        logger.debug("Drag detected");
        final Optional<Direction> oLoc = clickableArea
                .getDirection(fieldPoint(event.getX(), event.getY()));
        oLoc.map(loc -> colony.getColonyFieldInDirection(loc))
                .filter(field -> fieldWorker(field).isPresent())
                .ifPresent(colonyField -> {
                    dragStartedAtColonyField(colonyField, oLoc.get());
                    event.consume();
                });
    }

    private void dragStartedAtColonyField(final ColonyField colonyField,
            final Direction direction) {
        final Unit unit = fieldWorker(colonyField).orElseThrow();
        final Image image = imageProvider.getUnitImage(unit);
        final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image)
                .addTransferFromColonyField(direction.getVector()).addUnit(unit)
                .build();
    }

    private boolean movementDropHint;

    private void clearMovementDropHint() {
        if (movementDropHint) {
            movementDropHint = false;
            eventBus.post(new StatusBarMessageEvent(Source.COLONY));
        }
    }

    private void onDragOver(final DragEvent event) {
        clearMovementDropHint();
        final Optional<Unit> unit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .getUnit();
        final Optional<ColonyField> field = clickableArea
                .getDirection(fieldPoint(event.getX(), event.getY()))
                .map(colony::getColonyFieldInDirection);
        if (unit.isPresent() && field.isPresent()
                && field.get().canAccept(unit.get(), GoodsType.GRAIN)) {
            event.acceptTransferModes(TransferMode.MOVE);
            eventBus.post(new StatusBarMessageEvent(Source.COLONY));
        } else {
            // Accept the gesture so a refused release can explain the
            // rejection.
            if (unit.isPresent() && field.isPresent()) {
                event.acceptTransferModes(TransferMode.MOVE);
                movementDropHint = field.get().isWaitingForMovement(unit.get(),
                        GoodsType.GRAIN);
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(ColonyMsg.fieldDropRejected) + ". "
                                + i18n.get(
                                        dropProblem(field.get(), unit.get())),
                        Source.COLONY));
            }
        }
        event.consume();
    }

    private void onDragDropped(final DragEvent event) {
        clearMovementDropHint();
        event.setDropCompleted(false);
        final Optional<Unit> unit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .getUnit();
        clickableArea.getDirection(fieldPoint(event.getX(), event.getY()))
                .map(colony::getColonyFieldInDirection).ifPresent(field -> {
                    if (unit.isEmpty()) {
                        return;
                    }
                    if (!field.canAccept(unit.get(), GoodsType.GRAIN)) {
                        final ColonyMsg reason = dropProblem(field, unit.get());
                        afterDrag(event, () -> showDropMessage(reason));
                        return;
                    }
                    // The land question comes only for a field the worker may
                    // take; any other drop is refused as before.
                    if (isUnpaidClaim(field)) {
                        // Ask after the drop gesture has finished; canceling
                        // leaves the worker where it was.
                        event.setDropCompleted(true);
                        afterDrag(event,
                                () -> placeAfterQuestion(unit.get(), field));
                        return;
                    }
                    if (place(unit.get(), field,
                            NativeLandDialog.Result.NOT_ASKED)) {
                        event.setDropCompleted(true);
                    } else {
                        final ColonyMsg reason = dropProblem(field, unit.get());
                        afterDrag(event, () -> showDropMessage(reason));
                    }
                });
        event.consume();
    }

    private ColonyMsg dropProblem(final ColonyField field, final Unit unit) {
        if (field.isWaitingForMovement(unit, GoodsType.GRAIN)) {
            return ColonyMsg.fieldInsufficientMovement;
        }
        if (!field.isValid()) {
            return ColonyMsg.fieldChanged;
        }
        if (field.isMissingDock()) {
            return ColonyMsg.fieldMissingDock;
        }
        if (field.isWorkedByAnotherColony()) {
            return ColonyMsg.fieldWorked;
        }
        return !field.canProduce(GoodsType.GRAIN) ? ColonyMsg.fieldCannotProduce
                : ColonyMsg.fieldChanged;
    }

    private void afterDrag(final DragEvent event, final Runnable action) {
        final javafx.scene.Node source = (javafx.scene.Node) event
                .getGestureSource();
        source.addEventHandler(DragEvent.DRAG_DONE,
                new javafx.event.EventHandler<DragEvent>() {
                    @Override
                    public void handle(final DragEvent done) {
                        source.removeEventHandler(DragEvent.DRAG_DONE, this);
                        Platform.runLater(action);
                    }
                });
    }

    private void showDropMessage(final ColonyMsg reason) {
        dropMessage.setText(i18n.get(ColonyMsg.fieldDropRejected),
                i18n.get(reason));
        dropMessage.showAndWait();
        if (reason != ColonyMsg.fieldInsufficientMovement) {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(ColonyMsg.fieldDropRejected) + ". "
                            + i18n.get(reason),
                    Source.COLONY));
        }
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
            if (!place(unit, field, land)) {
                showDropMessage(dropProblem(field, unit));
            }
        }
    }

    private boolean place(final Unit unit, final ColonyField field,
            final NativeLandDialog.Result land) {
        try {
            unit.placeToColonyField(field, GoodsType.GRAIN);
            eventBus.post(new RepaintColonyEvent());
            return true;
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            final ColonyMsg reason = dropProblem(field, unit);
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land, i18n.get(reason)),
                    Source.COLONY));
            return false;
        }
    }

    private void showFieldProblem(final ColonyMsg message) {
        eventBus.post(StatusBarMessageEvent.outcome(i18n.get(message),
                Source.COLONY));
    }

    @com.google.common.eventbus.Subscribe
    public void selectWorker(final SelectColonyWorkerEvent event) {
        final Unit worker = event.getWorker();
        if (worker.getFishingColony().orElse(null) == colony
                && worker.isAtPlaceLocation()) {
            selectedX = worker.getLocation().getX()
                    - colony.getLocation().getX() + 1;
            selectedY = worker.getLocation().getY()
                    - colony.getLocation().getY() + 1;
            paint(canvas.getGraphicsContext2D());
            refreshDetail();
        }
    }

    public void setColony(final Colony colony) {
        contextMenu.hide();
        if (this.colony != colony) {
            selectedX = 1;
            selectedY = 1;
        }
        this.colony = Preconditions.checkNotNull(colony);
        paint(canvas.getGraphicsContext2D());
        refreshDetail();
    }

    private void paint(final GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        colony.getColonyFields()
                .forEach(colonyField -> paintColonyField(gc, colonyField));
        final Point colonyPoint = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        paintService.paintTerrainOnTile(
                gc, colonyPoint, colony.getLocation(), gameModelController
                        .getModel().getMap().getTerrainAt(colony.getLocation()),
                false, 0);
        paintService.paintColony(gc, colonyPoint, colony, false);
        gc.restore();
        gc.setStroke(Color.web("#75815a", 0.65));
        gc.setLineWidth(1);
        gc.strokeRect(.5, .5, canvas.getWidth() - 1, canvas.getHeight() - 1);
        for (int index = 1; index < 3; index++) {
            gc.strokeLine(index * tileSize(), 0, index * tileSize(),
                    canvas.getHeight());
            gc.strokeLine(0, index * tileSize(), canvas.getWidth(),
                    index * tileSize());
        }
        gc.setStroke(Color.web("#c99638"));
        gc.setLineWidth(2);
        gc.strokeRect(selectedX * tileSize() + 2, selectedY * tileSize() + 2,
                tileSize() - 4, tileSize() - 4);
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
        if (colonyField.isWorkedByAnotherColony()) {
            gc.setFill(Color.GRAY);
            gc.fillRect(point.getX(), point.getY(), TILE_WIDTH_IN_PX,
                    TILE_WIDTH_IN_PX);
            return;
        }
        paintService.paintTerrainOnTile(gc, point, colonyField.getLocation(),
                terrain, false, 0);
        if (colonyField.isEmpty()) {
            fieldWorker(colonyField).ifPresent(
                    unit -> paintService.paintUnitWithoutFlag(gc, point, unit));
        }
        if (!colonyField.isEmpty()) {
            final Goods production = colonyField.getProduction().get();
            final Unit unit = fieldWorker(colonyField).orElseThrow();
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

    private double tileSize() {
        return canvas.getWidth() / 3;
    }

    private Point fieldPoint(final double x, final double y) {
        return Point.of(x * TILE_WIDTH_IN_PX / tileSize(),
                y * TILE_WIDTH_IN_PX / tileSize());
    }

    private String describeTerrain(Terrain terrain) {
        java.util.List<String> facts = new java.util.ArrayList<>();
        facts.add(i18n.get(ColonyMsg.tileMovement, terrain.getMoveCost()));
        facts.add(i18n.get(ColonyMsg.tileDefense,
                terrain.isHasTrees()
                        ? terrain.getTerrainType().getDefenseBonusWithTree()
                        : terrain.getTerrainType().getDefenseBonus()));
        if (terrain.isHasTrees())
            facts.add(i18n.get(ColonyMsg.tileForest));
        if (terrain.isHasField())
            facts.add(i18n.get(ColonyMsg.tilePlowed));
        if (terrain.isHasRoad())
            facts.add(i18n.get(ColonyMsg.tileRoad));
        if (terrain.getRiverSize() != org.akropia.model.RiverSize.NONE)
            facts.add(i18n.get(
                    terrain.getRiverSize() == org.akropia.model.RiverSize.SMALL
                            ? ColonyMsg.tileSmallRiver
                            : ColonyMsg.tileLargeRiver));
        if (terrain
                .getResourceOverlay() != org.akropia.model.ResourceOverlay.NONE)
            facts.add(i18n.get(ColonyMsg.tileResource,
                    new LocalizationHelper(i18n).getResourceOverlayName(
                            terrain.getResourceOverlay())));
        return String.join(" · ", facts);
    }

    private void refreshDetail() {
        mapTitle.setText(i18n.get(ColonyMsg.surroundingSection));
        detailTitle.setText(i18n.get(ColonyMsg.selectedSection));
        productHeader.setText(i18n.get(ColonyMsg.outputLabel));
        yieldHeader.setText(i18n.get(ColonyMsg.outputYield));
        tileInfo.setText("");
        output.setAccessibleText(i18n.get(ColonyMsg.outputLabel));
        canvas.setAccessibleText(i18n.get(ColonyMsg.surroundingSection));
        final boolean center = selectedX == 1 && selectedY == 1;
        final ColonyField field = center ? null
                : colony.getColonyFieldInDirection(Direction
                        .valueOf(Location.of(selectedX - 1, selectedY - 1)));
        final Location location = center ? colony.getLocation()
                : field.getLocation();
        final GraphicsContext gc = preview.getGraphicsContext2D();
        gc.clearRect(0, 0, preview.getWidth(), preview.getHeight());
        output.getChildren().clear();
        production.setVisible(false);
        production.setManaged(false);
        workerName.setText(i18n.get(ColonyMsg.noWorker));
        yield.setVisible(center);
        yield.setManaged(center);
        if (!center && !field.isValid()) {
            tileName.setText(i18n.get(ColonyMsg.fieldChanged));
            return;
        }
        if (!center && field.isWorkedByAnotherColony()) {
            tileName.setText("");
            workerName.setText("");
            gc.setFill(Color.GRAY);
            gc.fillRect(0, 0, preview.getWidth(), preview.getHeight());
            return;
        }
        final Terrain terrain = gameModelController.getModel().getMap()
                .getTerrainAt(location);
        tileInfo.setText(describeTerrain(terrain));
        gc.save();
        paintService.paintTerrainOnTile(gc, Point.of(0, 0), location, terrain,
                false, 0);
        if (center) {
            paintService.paintColony(gc, Point.of(0, 0), colony, false);
            tileName.setText(colony.getName());
            workerName.setText(i18n.get(ColonyMsg.automaticProduction));
            yield.setText(colony.getTownCommonsProduction().stream()
                    .map(goods -> i18n.get(
                            GoodsTypeName.getNameForGoodsType(goods.getType()))
                            + ": " + goods.getAmount())
                    .collect(java.util.stream.Collectors.joining(", ")));
        } else {
            tileName.setText(new LocalizationHelper(i18n)
                    .getTerrainName(field.getTerrainType()));
            fieldWorker(field).ifPresent(unit -> {
                paintService.paintUnitWithoutFlag(gc, Point.of(0, 0), unit);
                workerName.setText(
                        i18n.get(UnitTypes.getUnitName(unit.getType())));
                final ToggleGroup group = new ToggleGroup();
                final GoodsType current = unit.isAtPlaceColonyField()
                        ? unit.getPlaceColonyField().getProducedGoodsType()
                        : null;
                // Only work on this field; a best-field offer for the same
                // field would repeat the product.
                colony.getJobChoices(unit).stream()
                        .filter(choice -> choice
                                .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD
                                && choice.getTargetLocation()
                                        .filter(field.getLocation()::equals)
                                        .isPresent()
                                && choice.getUnavailableReason().isEmpty())
                        .forEach(choice -> addOutputChoice(choice, current,
                                group));
                production.setVisible(true);
                production.setManaged(true);
            });
        }
        gc.restore();
    }

    private void addOutputChoice(final ColonyJobChoice choice,
            final GoodsType current, final ToggleGroup group) {
        final Goods goods = choice.getPotential().orElseThrow();
        final String name = i18n
                .get(GoodsTypeName.getNameForGoodsType(goods.getType()));
        final ImageView icon = new ImageView(
                imageProvider.getGoodsTypeImage(goods.getType()));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        final Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        final Label amount = new Label(Integer.toString(goods.getAmount()));
        amount.setId("colony-field-yield-" + goods.getType().name());
        final HBox content = new HBox(8, icon, new Label(name), space, amount);
        content.setAlignment(Pos.CENTER_LEFT);
        content.prefWidthProperty().bind(output.widthProperty().subtract(40));
        final RadioButton row = new RadioButton();
        row.setGraphic(content);
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setId("colony-field-output-" + goods.getType().name());
        row.setUserData(goods.getType());
        row.getStyleClass().add("field-output-choice");
        row.setToggleGroup(group);
        row.setSelected(goods.getType() == current);
        row.setAccessibleText(name + ", "
                + i18n.get(ColonyMsg.yieldSummary, goods.getAmount()));
        row.setAccessibleHelp(i18n.get(ColonyMsg.outputHelp));
        // Runs after the scroll pane's own jump to the focused row.
        row.focusedProperty().addListener((observable, wasFocused, focused) -> {
            if (focused) {
                Platform.runLater(this::revealProduction);
            }
        });
        row.selectedProperty()
                .addListener((observable, wasSelected, selected) -> {
                    if (!selected) {
                        return;
                    }
                    try {
                        choice.execute();
                        eventBus.post(new RepaintColonyEvent());
                    } catch (IllegalArgumentException
                            | IllegalStateException rejected) {
                        showFieldProblem(ColonyMsg.fieldChanged);
                        paint(canvas.getGraphicsContext2D());
                        refreshDetail();
                    }
                    output.getChildren().stream().filter(
                            node -> node.getUserData() == goods.getType())
                            .findFirst()
                            .ifPresent(javafx.scene.Node::requestFocus);
                });
        output.getChildren().add(row);
    }

    /**
     * Scrolls the colony page just enough to show the whole product list, so
     * keyboard focus never moves to a choice the player cannot read.
     */
    private void revealProduction() {
        Parent parent = production.getParent();
        while (parent != null && !(parent instanceof ScrollPane)) {
            parent = parent.getParent();
        }
        if (parent == null || production.getScene() == null) {
            return;
        }
        final ScrollPane scroll = (ScrollPane) parent;
        scroll.applyCss();
        scroll.layout();
        final Node content = scroll.getContent();
        final Bounds target = content.sceneToLocal(
                production.localToScene(production.getLayoutBounds()));
        final Bounds view = scroll.getViewportBounds();
        final Bounds all = content.getLayoutBounds();
        final double width = all.getWidth() - view.getWidth();
        if (width > 0) {
            scroll.setHvalue(scrollValue(scroll.getHvalue(), scroll.getHmin(),
                    scroll.getHmax(), width, target.getMinX(), target.getMaxX(),
                    view.getWidth()));
        }
        final double height = all.getHeight() - view.getHeight();
        if (height > 0) {
            scroll.setVvalue(scrollValue(scroll.getVvalue(), scroll.getVmin(),
                    scroll.getVmax(), height, target.getMinY(),
                    target.getMaxY(), view.getHeight()));
        }
        scroll.layout();
    }

    private static double scrollValue(final double value, final double min,
            final double max, final double range, final double start,
            final double end, final double visible) {
        double offset = (value - min) / (max - min) * range;
        if (end > offset + visible) {
            offset = end - visible;
        }
        if (start < offset) {
            offset = start;
        }
        offset = Math.max(0, Math.min(range, offset));
        return min + offset / range * (max - min);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
