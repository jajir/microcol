package org.microcol.gui.screen.colony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsProductionStats;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Show building factories and other structures build in colony.
 */
public final class PanelColonyStructures implements JavaFxComponent {

    private final Logger logger = LoggerFactory.getLogger(PanelColonyStructures.class);

    private final static int COLUMN_WIDTH = 100;

    private final static int COLUMN_GAP = 30;

    private final static int COLUMN_1 = 25;
    private final static int COLUMN_2 = COLUMN_1 + COLUMN_WIDTH + COLUMN_GAP;
    private final static int COLUMN_3 = COLUMN_2 + COLUMN_WIDTH + COLUMN_GAP;
    private final static int COLUMN_4 = COLUMN_3 + COLUMN_WIDTH + COLUMN_GAP;

    private final static int ROW_HEIGHT = 64;

    private final static int ROW_GAP = 10;

    private final static int ROW_1 = 20;
    private final static int ROW_2 = ROW_1 + ROW_HEIGHT + ROW_GAP;
    private final static int ROW_3 = ROW_2 + ROW_HEIGHT + ROW_GAP;
    private final static int ROW_4 = ROW_3 + ROW_HEIGHT + ROW_GAP;

    private final static int PRODUCTION_TEXT_X = 30;
    private final static int PRODUCTION_TEXT_Y = 0;

    private final static Point PRODUCTION_TEXT = Point.of(PRODUCTION_TEXT_X, PRODUCTION_TEXT_Y);

    private final static int SLOT_POSITION_SLOT_GAP = -10;

    private final static int SLOT_POSITION_WIDTH = GamePanelView.TILE_WIDTH_IN_PX
            + SLOT_POSITION_SLOT_GAP;

    private final static Point SLOT_SIZE = Point.of(GamePanelView.TILE_WIDTH_IN_PX,
            GamePanelView.TILE_WIDTH_IN_PX);

    private final static int SLOT_POSITION_TOTAL_WIDTH = 3 * GamePanelView.TILE_WIDTH_IN_PX
            + 2 * SLOT_POSITION_SLOT_GAP;

    private final static int SLOT_POSITION_START = -SLOT_POSITION_TOTAL_WIDTH / 2 + 50;

    private final static Point[] SLOT_POSITIONS = new Point[] { Point.of(SLOT_POSITION_START, 10),
            Point.of(SLOT_POSITION_START + SLOT_POSITION_WIDTH, 10),
            Point.of(SLOT_POSITION_START + 2 * SLOT_POSITION_WIDTH, 10) };

    /**
     * Following structure define position of constructions images on colony map.
     */
    private final static Map<ConstructionType, Point> constructionPlaces = ImmutableMap
            .<ConstructionType, Point>builder()
            .put(ConstructionType.TOWN_HALL, Point.of(COLUMN_2, ROW_1))
            .put(ConstructionType.LUMBER_MILL, Point.of(COLUMN_4, ROW_3))
            .put(ConstructionType.CARPENTERS_SHOP, Point.of(COLUMN_4, ROW_3))
            .put(ConstructionType.CARPENTERS_STAND, Point.of(COLUMN_4, ROW_3))
            .put(ConstructionType.IRON_WORKS, Point.of(COLUMN_2, ROW_4))
            .put(ConstructionType.BLACKSMITHS_SHOP, Point.of(COLUMN_2, ROW_4))
            .put(ConstructionType.BLACKSMITHS_HOUSE, Point.of(COLUMN_2, ROW_4))
            .put(ConstructionType.FORTRESS, Point.of(COLUMN_4, ROW_4))
            .put(ConstructionType.FORT, Point.of(COLUMN_4, ROW_4))
            .put(ConstructionType.STOCKADE, Point.of(COLUMN_4, ROW_4))
            .put(ConstructionType.CIGAR_FACTORY, Point.of(COLUMN_2, ROW_2))
            .put(ConstructionType.TOBACCONISTS_SHOP, Point.of(COLUMN_2, ROW_2))
            .put(ConstructionType.TOBACCONISTS_HOUSE, Point.of(COLUMN_2, ROW_2))
            .put(ConstructionType.TEXTILE_MILL, Point.of(COLUMN_1, ROW_2))
            .put(ConstructionType.WEAVERS_SHOP, Point.of(COLUMN_1, ROW_2))
            .put(ConstructionType.WEAVERS_HOUSE, Point.of(COLUMN_1, ROW_2))
            .put(ConstructionType.RUM_FACTORY, Point.of(COLUMN_3, ROW_2))
            .put(ConstructionType.RUM_DISTILLERY, Point.of(COLUMN_3, ROW_2))
            .put(ConstructionType.RUM_DISTILLERS_HOUSE, Point.of(COLUMN_3, ROW_2))
            .put(ConstructionType.FUR_FACTORY, Point.of(COLUMN_3, ROW_3))
            .put(ConstructionType.FUR_TRADING_POST, Point.of(COLUMN_3, ROW_3))
            .put(ConstructionType.FUR_TRADERS_HOUSE, Point.of(COLUMN_3, ROW_3))
            .put(ConstructionType.ARSENAL, Point.of(10, 10))
            .put(ConstructionType.MAGAZINE, Point.of(10, 10))
            .put(ConstructionType.ARMORY, Point.of(10, 10))
            .put(ConstructionType.SHIPYARD, Point.of(10, 10))
            .put(ConstructionType.DRYDOCK, Point.of(10, 10))
            .put(ConstructionType.DOCK, Point.of(10, 10))
            .put(ConstructionType.UNIVERSITY, Point.of(10, 10))
            .put(ConstructionType.COLLEGE, Point.of(10, 10))
            .put(ConstructionType.SCHOOLHOUSE, Point.of(10, 10))
            .put(ConstructionType.WAREHOUSE_EXPANSION, Point.of(COLUMN_1, ROW_1))
            .put(ConstructionType.WAREHOUSE, Point.of(COLUMN_1, ROW_1))
            .put(ConstructionType.WAREHOUSE_BASIC, Point.of(COLUMN_1, ROW_1))
            .put(ConstructionType.LARGE_STABLES, Point.of(10, 10))
            .put(ConstructionType.STABLES, Point.of(10, 10))
            .put(ConstructionType.CHAPEL, Point.of(COLUMN_3, ROW_1))
            .put(ConstructionType.CATHEDRAL, Point.of(COLUMN_3, ROW_1))
            .put(ConstructionType.CHURCH, Point.of(COLUMN_3, ROW_1))
            .put(ConstructionType.NEWSPAPER, Point.of(10, 10))
            .put(ConstructionType.PRINTING_PRESS, Point.of(10, 10))
            .put(ConstructionType.CUSTOM_HOUSE, Point.of(10, 10)).build();

    /**
     * Validation that constants are consistent.
     */
    static {
        Preconditions.checkState(ConstructionType.ALL.size() == constructionPlaces.size(),
                String.format(
                        "There is different number of construction types '%s' and it's definitions of position '%s'.",
                        ConstructionType.ALL.size(), constructionPlaces.size()));
    }

    private final static int CANVAS_WIDTH = 500;

    private final static int CANVAS_HEIGHT = 300;

    private final static int GOOD_ICON_WIDTH = 30;

    private final Canvas canvas;

    private final LocalizationHelper localizationHelper;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private Map<Rectangle, ConstructionSlot> slots;

    private final TmpPanel mainPanel;

    private final List<ColonyStructure> structures;

    private final I18n i18n;

    private final EventBus eventBus;

    @Inject
    public PanelColonyStructures(final LocalizationHelper localizationHelper,
            final ImageProvider imageProvider, final GameModelController gameModelController,
            final EventBus eventBus, final I18n i18n) {
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        structures = new ArrayList<>();
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setOnDragEntered(this::onDragEntered);
        canvas.setOnDragExited(this::onDragExited);
        canvas.setOnDragOver(this::onDragOver);
        canvas.setOnDragDropped(this::onDragDropped);
        canvas.setOnDragDetected(this::onDragDetected);
        canvas.setOnMouseMoved(this::onMouseMoved);

        mainPanel = new TmpPanel();
        mainPanel.getStyleClass().add("colony-structures");
        mainPanel.getContentPane().getChildren().add(canvas);
        mainPanel.setMinWidth(CANVAS_WIDTH);
        mainPanel.setMinHeight(CANVAS_HEIGHT);
    }

    private void onMouseMoved(final MouseEvent event) {
        structures.forEach(structure -> {
            structure.evaluateMouseMove(event);
        });
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
            event.consume();
        }
        event.consume();
    }

    @SuppressWarnings("unused")
    private void onDragEntered(final DragEvent event) {
        logger.debug("Drag entered");
    }

    @SuppressWarnings("unused")
    private void onDragExited(final DragEvent event) {
        logger.debug("Drag Exited");
    }

    private void onDragOver(final DragEvent event) {
        logger.debug("Drag Over");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && loc.get().isEmpty()) {
            event.acceptTransferModes(TransferMode.MOVE);
            logger.debug("was clicked at: " + loc.get());
        } else {
            event.acceptTransferModes(TransferMode.NONE);
        }
        event.consume();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Drag dropped");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && loc.get().isEmpty()) {
            ConstructionSlot slot = loc.get();
            ClipboardEval.make(gameModelController.getModel(), event.getDragboard())
                    .tryReadUnit((unit, transferFrom) -> {
                        unit.placeToColonyStructureSlot(slot);
                        event.setDropCompleted(true);
                    });
        }
        event.consume();
    }

    private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return slots.entrySet().stream().filter(entry -> entry.getKey().isIn(point))
                .map(entry -> entry.getValue()).findAny();
    }

    void repaint(final Colony colony) {
        slots = new HashMap<>();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        structures.clear();
        colony.getConstructions().forEach(construction -> {
            paintConstruction(gc, colony, construction);
            final Point position = constructionPlaces.get(construction.getType());
            final ColonyStructure colonyStructure = new ColonyStructure(position,
                    Point.of(COLUMN_WIDTH, ROW_HEIGHT));
            colonyStructure.setOnMouseEntered(event -> {
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(ConstructionTypeName.getNameForType(construction.getType())),
                        Source.COLONY));
            });
            colonyStructure.setOnMouseExited(event -> {
                eventBus.post(new StatusBarMessageEvent(Source.COLONY));
            });
            structures.add(colonyStructure);
        });
    }

    private void paintConstruction(final GraphicsContext gc, final Colony colony,
            final Construction construction) {
        final Point position = constructionPlaces.get(construction.getType());
        Preconditions.checkNotNull(position,
                String.format("There is no defined position for construction type '%s'", position));
        final Optional<Image> oImage = imageProvider.getConstructionImage(construction.getType());
        if (oImage.isPresent()) {
            gc.drawImage(oImage.get(), position.getX(), position.getY());
        } else {
            final String name = localizationHelper.getConstructionTypeName(construction.getType());
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.fillText(name, position.getX(), position.getY());
            gc.setStroke(Color.DARKGRAY);
        }
        final AtomicInteger cx = new AtomicInteger(0);
        construction.getConstructionSlots().forEach(constructionSlot -> {
            final Point topLeftCorner = position.add(SLOT_POSITIONS[cx.get()]);
            slots.put(Rectangle.ofPointAndSize(topLeftCorner, SLOT_SIZE), constructionSlot);
            if (!constructionSlot.isEmpty()) {
                gc.drawImage(imageProvider.getUnitImage(constructionSlot.getUnit()),
                        topLeftCorner.getX(), topLeftCorner.getY());
            }
            cx.incrementAndGet();
        });
        paintProduction(gc, position, colony, construction);
    }

    private void paintProduction(final GraphicsContext gc, final Point point, final Colony colony,
            final Construction construction) {
        if (construction.getType().getProduce().isPresent()) {

            final ColonyProductionStats colonyStats = colony.getGoodsStats();
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
                gc.fillText(toWrite, prod.getX(), prod.getY());
                final double width = getTextWidth(gc, toWrite);
                gc.drawImage(
                        imageProvider.getGoodsTypeImage(construction.getType().getProduce().get()),
                        prod.getX() - width / 2 - GOOD_ICON_WIDTH, prod.getY() - 10,
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

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
