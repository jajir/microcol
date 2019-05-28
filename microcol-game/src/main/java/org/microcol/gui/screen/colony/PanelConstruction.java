package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.MicroColException;
import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
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

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Panel with construction.
 */
class PanelConstruction implements JavaFxComponent {

    private final Logger logger = LoggerFactory.getLogger(PanelConstruction.class);

    private final static int CANVAS_WIDTH = 100;
    private final static int CANVAS_HEIGHT = 135;

    private final static int GOOD_ICON_WIDTH = 30;

    /*
     * Constructions
     */
    private final static int CONSTRUCTION_X = 15;
    private final static int CONSTRUCTION_Y = 0;

    /*
     * Goods icon.
     */
    private final static int GOODS_ICON_X = 0;
    private final static int GOODS_ICON_Y = 100;

    /*
     * Slot size and position definition.
     */
    private final static int SLOT_POSITION_SLOT_GAP = -14;
    private final static int SLOT_POSITION_WIDTH = TILE_WIDTH_IN_PX + SLOT_POSITION_SLOT_GAP;
    private final static Point SLOT_SIZE = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
    private final static int SLOT_POSITION_X_START = -10;
    private final static int SLOT_POSITION_Y = 60;
    private final static Point[] SLOT_POSITIONS = new Point[] {
            Point.of(SLOT_POSITION_X_START, SLOT_POSITION_Y),
            Point.of(SLOT_POSITION_X_START + SLOT_POSITION_WIDTH, SLOT_POSITION_Y),
            Point.of(SLOT_POSITION_X_START + 2 * SLOT_POSITION_WIDTH, SLOT_POSITION_Y) };

    private final EventBus eventBus;
    private final I18n i18n;
    private final Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    private final StackPane mainPane = new StackPane();
    private final Construction construction;
    private final ColonyProductionStats colonyStats;
    private final ImageProvider imageProvider;
    private final Map<Rectangle, ConstructionSlot> slots = new HashMap<>();
    private final Model model;

    PanelConstruction(final ImageProvider imageProvider, final EventBus eventBus, final I18n i18n,
            final Construction construction, final ColonyProductionStats colonyStats,
            final Model model) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.construction = Preconditions.checkNotNull(construction);
        this.colonyStats = Preconditions.checkNotNull(colonyStats);
        this.model = Preconditions.checkNotNull(model);
        mainPane.getStyleClass().add("constructionPane");
        mainPane.setOnMouseEntered(this::onMouseEntered);
        mainPane.setOnMouseExited(this::onMouseExited);
        mainPane.getChildren().add(canvas);
        mainPane.setOnDragOver(this::onDragOver);
        mainPane.setOnDragDropped(this::onDragDropped);
        mainPane.setOnDragDetected(this::onDragDetected);
    }

    void setCssId(final String id) {
        mainPane.setId(id);
    }

    private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return slots.entrySet().stream().filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

    void paint() {
        paintConstruction(canvas.getGraphicsContext2D(), colonyStats, construction);
    }

    private void paintConstruction(final GraphicsContext gc,
            final ColonyProductionStats colonyStats, final Construction construction) {
        // gc.setFill(Color.BLANCHEDALMOND);
        // gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        final Image image = imageProvider.getConstructionImage(construction.getType())
                .orElseThrow(() -> new MicroColException(
                        String.format("Unable to find image for %s", construction)));
        gc.drawImage(image, CONSTRUCTION_X, +CONSTRUCTION_Y);

        final AtomicInteger cx = new AtomicInteger(0);
        construction.getConstructionSlots().forEach(constructionSlot -> {
            final Point topLeftCorner = SLOT_POSITIONS[cx.get()];
            slots.put(Rectangle.ofPointAndSize(topLeftCorner, SLOT_SIZE), constructionSlot);
            if (!constructionSlot.isEmpty()) {
                gc.drawImage(imageProvider.getUnitImage(constructionSlot.getUnit()),
                        topLeftCorner.getX(), topLeftCorner.getY());
            }
            cx.incrementAndGet();
        });
        paintProduction(gc, colonyStats, construction);
    }

    private void paintProduction(final GraphicsContext gc, final ColonyProductionStats colonyStats,
            final Construction construction) {
        if (construction.getType().getProductionPerTurn().isPresent()) {

            final GoodsProductionStats goodsStats = colonyStats
                    .getStatsByType(construction.getProducedGoodsType().get());

            if (isThereTextToPaint(goodsStats)) {
                paintProductionIcon(gc);
            }

            final HBox box = new HBox();
            box.getStyleClass().add("production");
            // TODO make box separate component
            // TODO add mouse over status bar description

            if (goodsStats.getNetProduction() != 0) {
                final Label label = new Label("+" + goodsStats.getNetProduction());
                label.getStyleClass().add("diffPositive");
                box.getChildren().add(label);
            }

            if (goodsStats.getBlockedProduction() != 0) {
                final Label label = new Label("-" + goodsStats.getBlockedProduction());
                label.getStyleClass().add("diffNegative");
                box.getChildren().add(label);
            }

            mainPane.getChildren().add(box);
        }
    }

    private void paintProductionIcon(final GraphicsContext gc) {
        final Goods produced = construction.getType().getProductionPerTurn().get();
        gc.drawImage(imageProvider.getGoodsTypeImage(produced.getType()), GOODS_ICON_X,
                GOODS_ICON_Y, GOOD_ICON_WIDTH, GOOD_ICON_WIDTH);
    }

    private boolean isThereTextToPaint(final GoodsProductionStats goodsStats) {
        return goodsStats.getNetProduction() != 0 || goodsStats.getBlockedProduction() != 0;
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(
                i18n.get(ConstructionTypeName.getNameForType(construction.getType())),
                Source.COLONY));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.COLONY));
    }

    private void onDragOver(final DragEvent event) {
        logger.debug("Drag Over");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && loc.get().isEmpty()) {
            event.acceptTransferModes(TransferMode.MOVE);
            logger.debug("was clicked at: " + loc.get());
        } else {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Drag dropped");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && loc.get().isEmpty()) {
            final ConstructionSlot slot = loc.get();
            ClipboardEval.make(model, event.getDragboard()).tryReadUnit((unit, transferFrom) -> {
                unit.placeToColonyStructureSlot(slot);
                event.setDropCompleted(true);
            });
        } else {
            final Optional<ConstructionSlot> oSlot = construction.getFirstEmptySlot();
            if (oSlot.isPresent()) {
                ClipboardEval.make(model, event.getDragboard())
                        .tryReadUnit((unit, transferFrom) -> {
                            unit.placeToColonyStructureSlot(oSlot.get());
                            event.setDropCompleted(true);
                        });
            }
        }
        event.consume();
    }

    private void onDragDetected(final MouseEvent event) {
        logger.debug("Drag detected");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && !loc.get().isEmpty()) {
            final Unit unit = loc.get().getUnit();
            final Image image = imageProvider.getUnitImage(unit);
            final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image).addTransferFromConstructionSlot()
                    .addUnit(unit).build();
        }
        event.consume();
    }

    @Override
    public Region getContent() {
        return mainPane;
    }
}
