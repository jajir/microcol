package org.microcol.gui.screen.colony;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.MicroColException;
import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.image.ImageLoaderExtra;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsProductionStats;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * It's responsible for painting construction.
 */
class ConstructionPainting {

    private final static int GOOD_ICON_WIDTH = 30;

    /*
     * Constructions
     */
    private final static int CONSTRUCTION_X = 15;
    private final static int CONSTRUCTION_Y = 0;

    /*
     * Production text position.
     */
    private final static int PRODUCTION_TEXT_X = 50;
    private final static int PRODUCTION_TEXT_Y = 120;
    private final static Point PRODUCTION_TEXT = Point.of(PRODUCTION_TEXT_X, PRODUCTION_TEXT_Y);
    private final static int GOODS_ICON_SHIFT_Y = -20;

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

    private final Construction construction;
    private final ColonyProductionStats colonyStats;
    private final ImageProvider imageProvider;
    private final Rectangle positionAndSize;
    private final Map<Rectangle, ConstructionSlot> slots = new HashMap<>();
    private final ColonyStructure colonyStructure;

    ConstructionPainting(final ImageProvider imageProvider, final EventBus eventBus, final I18n i18n,
            final Construction construction, final ColonyProductionStats colonyStats,
            final Rectangle positionAndSize) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.construction = Preconditions.checkNotNull(construction);
        this.colonyStats = Preconditions.checkNotNull(colonyStats);
        this.positionAndSize = Preconditions.checkNotNull(positionAndSize);
        colonyStructure = new ColonyStructure(getPosition(), positionAndSize.getSize());
        colonyStructure.setOnMouseEntered(event -> {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(ConstructionTypeName.getNameForType(construction.getType())),
                    Source.COLONY));
        });
        colonyStructure.setOnMouseExited(event -> {
            eventBus.post(new StatusBarMessageEvent(Source.COLONY));
        });
    }

    void evaluateMouseMove(final MouseEvent event) {
        colonyStructure.evaluateMouseMove(event);
    }

    Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return slots.entrySet().stream().filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

    Optional<Construction> findConstruction(final Point point) {
        return positionAndSize.isIn(point) ? Optional.of(construction) : Optional.empty();
    }

    void paint(final GraphicsContext gc) {
        paintConstruction(gc, colonyStats, construction);
    }

    private void paintConstruction(final GraphicsContext gc,
            final ColonyProductionStats colonyStats, final Construction construction) {
        Preconditions.checkNotNull(getPosition(), String
                .format("There is no defined position for construction type '%s'", getPosition()));
        final Image imageFrame = imageProvider.getImage(ImageLoaderExtra.COLONY_FRAME);
        gc.drawImage(imageFrame, getPosition().getX(), getPosition().getY());
        final Image image = imageProvider.getConstructionImage(construction.getType())
                .orElseThrow(() -> new MicroColException(
                        String.format("Unable to find image for %s", construction)));
        gc.drawImage(image, getPosition().getX() + CONSTRUCTION_X,
                getPosition().getY() + CONSTRUCTION_Y);

        final AtomicInteger cx = new AtomicInteger(0);
        construction.getConstructionSlots().forEach(constructionSlot -> {
            final Point topLeftCorner = getPosition().add(SLOT_POSITIONS[cx.get()]);
            slots.put(Rectangle.ofPointAndSize(topLeftCorner, SLOT_SIZE), constructionSlot);
            if (!constructionSlot.isEmpty()) {
                gc.drawImage(imageProvider.getUnitImage(constructionSlot.getUnit()),
                        topLeftCorner.getX(), topLeftCorner.getY());
            }
            cx.incrementAndGet();
        });
        paintProduction(gc, getPosition(), colonyStats, construction);
    }

    private void paintProduction(final GraphicsContext gc, final Point point,
            final ColonyProductionStats colonyStats, final Construction construction) {
        if (construction.getType().getProductionPerTurn().isPresent()) {

            final GoodsProductionStats goodsStats = colonyStats
                    .getStatsByType(construction.getProducedGoodsType().get());

            String toWrite = "";

            if (goodsStats.getNetProduction() != 0) {
                toWrite += "x " + goodsStats.getNetProduction() + " ";
            }
            if (goodsStats.getBlockedProduction() != 0) {
                toWrite += "lost " + goodsStats.getBlockedProduction();
            }
            if (!Strings.isNullOrEmpty(toWrite)) {
                final Point prod = point.add(PRODUCTION_TEXT);
                final Goods produced = construction.getType().getProductionPerTurn().get();
                gc.fillText(toWrite, prod.getX(), prod.getY());
                final double width = getTextWidth(gc, toWrite);
                gc.drawImage(imageProvider.getGoodsTypeImage(produced.getType()),
                        prod.getX() - width / 2 - GOOD_ICON_WIDTH, prod.getY() + GOODS_ICON_SHIFT_Y,
                        GOOD_ICON_WIDTH, GOOD_ICON_WIDTH);
            }
        }
    }

    /**
     * Get length of given text. Use default text font.
     * 
     * @param gc
     *            required graphics context
     * @param text
     *            required text which size is looking for
     * @return text length
     */
    private double getTextWidth(final GraphicsContext gc, final String text) {
        final Text theText = new Text(text);
        theText.setFont(gc.getFont());
        return theText.getBoundsInLocal().getWidth();
    }

    private Point getPosition() {
        return positionAndSize.getTopLeftCorner();
    }
}
