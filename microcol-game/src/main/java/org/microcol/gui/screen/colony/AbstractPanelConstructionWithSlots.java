package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.HashMap;
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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

abstract class AbstractPanelConstructionWithSlots extends AbstractPanelConstruction {

    private final Logger logger = LoggerFactory.getLogger(AbstractPanelConstructionWithSlots.class);

    private final static int CANVAS_WIDTH = 100;
    private final static int CANVAS_HEIGHT = 135;
    private final static Point CANVAS_SIZE = Point.of(CANVAS_WIDTH, CANVAS_HEIGHT);

    private final static int GOOD_ICON_WIDTH = 30;
    
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

    private final ColonyProductionStats colonyStats;
    private final Map<Rectangle, ConstructionSlot> slots = new HashMap<>();
    private final Model model;

    public AbstractPanelConstructionWithSlots(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider, final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        super(eventBus, i18n, imageProvider, construction, CANVAS_SIZE);
        this.colonyStats = Preconditions.checkNotNull(colonyStats);
        this.model = Preconditions.checkNotNull(model);
        getMainPane().setOnDragOver(this::onDragOver);
        getMainPane().setOnDragDropped(this::onDragDropped);
        getMainPane().setOnDragDetected(this::onDragDetected);
    }

    void paintWorkingSlots(final GraphicsContext gc) {
        final AtomicInteger cx = new AtomicInteger(0);
        getConstruction().getConstructionSlots().forEach(constructionSlot -> {
            final Point topLeftCorner = SLOT_POSITIONS[cx.get()];
            slots.put(Rectangle.ofPointAndSize(topLeftCorner, SLOT_SIZE), constructionSlot);
            if (!constructionSlot.isEmpty()) {
                gc.drawImage(getImageProvider().getUnitImage(constructionSlot.getUnit()),
                        topLeftCorner.getX(), topLeftCorner.getY());
            }
            cx.incrementAndGet();
        });
    }

    void paintProduction(final GraphicsContext gc) {
        if (getConstruction().getType().getProductionPerTurn().isPresent()) {

            final GoodsProductionStats goodsStats = colonyStats
                    .getStatsByType(getConstruction().getProducedGoodsType().get());

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

            getMainPane().getChildren().add(box);
        }
    }

    private void paintProductionIcon(final GraphicsContext gc) {
        final Goods produced = getConstruction().getType().getProductionPerTurn().get();
        gc.drawImage(getImageProvider().getGoodsTypeImage(produced.getType()), GOODS_ICON_X,
                GOODS_ICON_Y, GOOD_ICON_WIDTH, GOOD_ICON_WIDTH);
    }

    private boolean isThereTextToPaint(final GoodsProductionStats goodsStats) {
        return goodsStats.getNetProduction() != 0 || goodsStats.getBlockedProduction() != 0;
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
            final Optional<ConstructionSlot> oSlot = getConstruction().getFirstEmptySlot();
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
            final Image image = getImageProvider().getUnitImage(unit);
            final Dragboard db = getCanvas().startDragAndDrop(TransferMode.MOVE);
            ClipboardWritter.make(db).addImage(image).addTransferFromConstructionSlot()
                    .addUnit(unit).build();
        }
        event.consume();
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

    private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return slots.entrySet().stream().filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

}
