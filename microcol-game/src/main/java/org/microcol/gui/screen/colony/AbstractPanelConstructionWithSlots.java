package org.microcol.gui.screen.colony;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.i18n.I18n;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsProductionStats;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

abstract class AbstractPanelConstructionWithSlots
        extends AbstractPanelConstruction {

    private final Logger logger = LoggerFactory
            .getLogger(AbstractPanelConstructionWithSlots.class);

    private final static int CANVAS_WIDTH = 102;
    private final static int CANVAS_HEIGHT = 32;
    private final static Point CANVAS_SIZE = Point.of(CANVAS_WIDTH,
            CANVAS_HEIGHT);

    private final static int GOOD_ICON_WIDTH = 22;

    private final static int SLOT_POSITION_WIDTH = 35;
    private final static Point SLOT_SIZE = Point.of(32, 32);
    private final static int SLOT_POSITION_X_START = 0;
    private final static int SLOT_POSITION_Y = 0;
    private final static Point[] SLOT_POSITIONS = new Point[] {
            Point.of(SLOT_POSITION_X_START, SLOT_POSITION_Y),
            Point.of(SLOT_POSITION_X_START + SLOT_POSITION_WIDTH,
                    SLOT_POSITION_Y),
            Point.of(SLOT_POSITION_X_START + 2 * SLOT_POSITION_WIDTH,
                    SLOT_POSITION_Y) };

    private final ColonyProductionStats colonyStats;
    private final Map<Rectangle, ConstructionSlot> slots = new LinkedHashMap<>();
    private final Model model;

    private ConstructionSlot offeredDropSlot;

    public AbstractPanelConstructionWithSlots(final EventBus eventBus,
            final I18n i18n, final ImageProvider imageProvider,
            final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        super(eventBus, i18n, imageProvider, construction, CANVAS_SIZE);
        this.colonyStats = Preconditions.checkNotNull(colonyStats);
        this.model = Preconditions.checkNotNull(model);
        getMainPane().setOnDragOver(this::onDragOver);
        getMainPane().setOnDragDropped(this::onDragDropped);
        getMainPane().setOnDragDetected(this::onDragDetected);
        getMainPane().setOnContextMenuRequested(event -> {
            findConstructionSlot(
                    canvasPoint(event.getSceneX(), event.getSceneY()))
                    .filter(slot -> !slot.isEmpty()).ifPresent(slot -> {
                        final Unit worker = slot.getUnit();
                        final MenuItem work = new MenuItem(
                                getI18n().get(ColonyMsg.jobOpen));
                        work.setId("chooseWork");
                        work.setOnAction(action -> getEventBus()
                                .post(new ShowColonyJobChoicesEvent(worker)));
                        new ContextMenu(work).show(getMainPane(),
                                event.getScreenX(), event.getScreenY());
                    });
            event.consume();
        });
    }

    void paintWorkingSlots(final GraphicsContext gc) {
        final AtomicInteger cx = new AtomicInteger(0);
        getConstruction().getConstructionSlots().forEach(constructionSlot -> {
            final Point topLeftCorner = SLOT_POSITIONS[cx.get()];
            slots.put(Rectangle.ofPointAndSize(topLeftCorner, SLOT_SIZE),
                    constructionSlot);
            if (constructionSlot.isEmpty()) {
                gc.setStroke(javafx.scene.paint.Color.web("#75815a"));
                gc.strokeOval(topLeftCorner.getX() + 9,
                        topLeftCorner.getY() + 9, 13, 13);
            } else {
                gc.drawImage(
                        getImageProvider()
                                .getUnitImage(constructionSlot.getUnit()),
                        topLeftCorner.getX(), topLeftCorner.getY(), 32, 32);
            }
            cx.incrementAndGet();
        });
    }

    VBox createProduction() {
        final VBox output = new VBox(1);
        output.setMouseTransparent(true);
        output.getStyleClass().add("production");
        if (getConstruction().getType().getProductionPerTurn().isPresent()) {
            final GoodsProductionStats goodsStats = colonyStats.getStatsByType(
                    getConstruction().getProducedGoodsType().get());
            if (goodsStats.getNetProduction() != 0
                    || goodsStats.getBlockedProduction() != 0) {
                final Goods produced = getConstruction().getType()
                        .getProductionPerTurn().get();
                final ImageView icon = new ImageView(getImageProvider()
                        .getGoodsTypeImage(produced.getType()));
                icon.setFitWidth(GOOD_ICON_WIDTH);
                icon.setFitHeight(GOOD_ICON_WIDTH);
                icon.getStyleClass().add("production-icon");
                final HBox values = new HBox(3);
                values.setAlignment(javafx.geometry.Pos.CENTER);
                if (goodsStats.getNetProduction() != 0) {
                    final Label label = new Label(
                            "+" + goodsStats.getNetProduction());
                    label.getStyleClass().add("diffPositive");
                    values.getChildren().add(label);
                }
                if (goodsStats.getBlockedProduction() != 0) {
                    final Label label = new Label(
                            "-" + goodsStats.getBlockedProduction());
                    label.getStyleClass().add("diffNegative");
                    values.getChildren().add(label);
                }
                output.getChildren().addAll(icon, values);
            }
        }
        return output;
    }

    private Optional<ConstructionSlot> dropTarget(final Point point) {
        return findConstructionSlot(point).filter(ConstructionSlot::isEmpty)
                .or(() -> getConstruction().getFirstEmptySlot());
    }

    private void onDragDropped(final DragEvent event) {
        event.setDropCompleted(false);
        final Optional<ConstructionSlot> target = Optional
                .ofNullable(offeredDropSlot);
        offeredDropSlot = null;
        ClipboardEval.make(model, event.getDragboard())
                .tryReadUnit((unit, transferFrom) -> {
                    try {
                        final ConstructionSlot slot = target.orElseThrow(
                                () -> new IllegalStateException("No free job"));
                        if (!slot.canAccept(unit)) {
                            throw new IllegalStateException(
                                    "Worker cannot take this job");
                        }
                        unit.placeToColonyStructureSlot(slot);
                        event.setDropCompleted(true);
                    } catch (IllegalStateException
                            | IllegalArgumentException rejected) {
                        // A modal dialog must wait for the native drag to end.
                        final Node source = (Node) event.getGestureSource();
                        source.addEventHandler(DragEvent.DRAG_DONE,
                                new EventHandler<DragEvent>() {
                                    @Override
                                    public void handle(final DragEvent done) {
                                        source.removeEventHandler(
                                                DragEvent.DRAG_DONE, this);
                                        javafx.application.Platform.runLater(
                                                () -> getEventBus().post(
                                                        new ShowColonyJobChoicesEvent(
                                                                unit, true)));
                                    }
                                });
                    }
                });
        event.consume();
    }

    private void onDragDetected(final MouseEvent event) {
        logger.debug("Drag detected");
        final Point point = canvasPoint(event.getSceneX(), event.getSceneY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && !loc.get().isEmpty()) {
            final Unit unit = loc.get().getUnit();
            final Image image = getImageProvider().getUnitImage(unit);
            final Dragboard db = getCanvas()
                    .startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image)
                    .addTransferFromConstructionSlot().addUnit(unit).build();
        }
        event.consume();
    }

    private void onDragOver(final DragEvent event) {
        offeredDropSlot = null;
        dropTarget(canvasPoint(event.getSceneX(), event.getSceneY())).ifPresent(
                slot -> ClipboardEval.make(model, event.getDragboard())
                        .tryReadUnit((unit, transferFrom) -> {
                            if (slot.canAccept(unit)) {
                                offeredDropSlot = slot;
                                event.acceptTransferModes(TransferMode.MOVE);
                            }
                        }));
        event.consume();
    }

    private Point canvasPoint(final double sceneX, final double sceneY) {
        final javafx.geometry.Point2D point = getCanvas().sceneToLocal(sceneX,
                sceneY);
        return Point.of((int) point.getX(), (int) point.getY());
    }

    private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return slots.entrySet().stream()
                .filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

}
