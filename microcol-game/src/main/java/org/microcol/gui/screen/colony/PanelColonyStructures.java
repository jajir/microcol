package org.microcol.gui.screen.colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.ConstructionType;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    private final static int ROW_HEIGHT = 134;
    private final static int ROW_GAP = 10;
    private final static int ROW_1 = 40;
    private final static int ROW_2 = ROW_1 + ROW_HEIGHT + ROW_GAP;
    private final static int ROW_3 = ROW_2 + ROW_HEIGHT + ROW_GAP;

    private final static Point CONSTRUCTION_SIZE = Point.of(COLUMN_WIDTH, ROW_HEIGHT);
    private final static int CANVAS_WIDTH = 530;
    private final static int CANVAS_HEIGHT = 500;

    /**
     * Following structure define position of constructions images on colony
     * map.
     */
    private final static Map<ConstructionType, Point> constructionPlaces = ImmutableMap
            .<ConstructionType, Point>builder()
            /*
             * Row 1
             */
            // warehouse
            .put(ConstructionType.WAREHOUSE_EXPANSION, Point.of(COLUMN_1, ROW_1))
            .put(ConstructionType.WAREHOUSE, Point.of(COLUMN_1, ROW_1))
            .put(ConstructionType.WAREHOUSE_BASIC, Point.of(COLUMN_1, ROW_1))
            // Townhall
            .put(ConstructionType.TOWN_HALL, Point.of(COLUMN_2, ROW_1))
            // Church
            .put(ConstructionType.CHAPEL, Point.of(COLUMN_3, ROW_1))
            .put(ConstructionType.CATHEDRAL, Point.of(COLUMN_3, ROW_1))
            .put(ConstructionType.CHURCH, Point.of(COLUMN_3, ROW_1))
            // Fort
            .put(ConstructionType.FORTRESS, Point.of(COLUMN_4, ROW_1))
            .put(ConstructionType.FORT, Point.of(COLUMN_4, ROW_1))
            .put(ConstructionType.STOCKADE, Point.of(COLUMN_4, ROW_1))
            /*
             * Row 2
             */
            // weaver
            .put(ConstructionType.TEXTILE_MILL, Point.of(COLUMN_1, ROW_2))
            .put(ConstructionType.WEAVERS_SHOP, Point.of(COLUMN_1, ROW_2))
            .put(ConstructionType.WEAVERS_HOUSE, Point.of(COLUMN_1, ROW_2))
            // cigars
            .put(ConstructionType.CIGAR_FACTORY, Point.of(COLUMN_2, ROW_2))
            .put(ConstructionType.TOBACCONISTS_SHOP, Point.of(COLUMN_2, ROW_2))
            .put(ConstructionType.TOBACCONISTS_HOUSE, Point.of(COLUMN_2, ROW_2))
            // rum
            .put(ConstructionType.RUM_FACTORY, Point.of(COLUMN_3, ROW_2))
            .put(ConstructionType.RUM_DISTILLERY, Point.of(COLUMN_3, ROW_2))
            .put(ConstructionType.RUM_DISTILLERS_HOUSE, Point.of(COLUMN_3, ROW_2))
            // School
            .put(ConstructionType.UNIVERSITY, Point.of(COLUMN_4, ROW_2))
            .put(ConstructionType.COLLEGE, Point.of(COLUMN_4, ROW_2))
            .put(ConstructionType.SCHOOLHOUSE, Point.of(COLUMN_4, ROW_2))
            /*
             * Row 3
             */
            // Guns
            .put(ConstructionType.ARSENAL, Point.of(COLUMN_1, ROW_3))
            .put(ConstructionType.MAGAZINE, Point.of(COLUMN_1, ROW_3))
            .put(ConstructionType.ARMORY, Point.of(COLUMN_1, ROW_3))
            // Iron
            .put(ConstructionType.IRON_WORKS, Point.of(COLUMN_2, ROW_3))
            .put(ConstructionType.BLACKSMITHS_SHOP, Point.of(COLUMN_2, ROW_3))
            .put(ConstructionType.BLACKSMITHS_HOUSE, Point.of(COLUMN_2, ROW_3))
            // fur
            .put(ConstructionType.FUR_FACTORY, Point.of(COLUMN_3, ROW_3))
            .put(ConstructionType.FUR_TRADING_POST, Point.of(COLUMN_3, ROW_3))
            .put(ConstructionType.FUR_TRADERS_HOUSE, Point.of(COLUMN_3, ROW_3))
            // lumber
            .put(ConstructionType.LUMBER_MILL, Point.of(COLUMN_4, ROW_3))
            .put(ConstructionType.CARPENTERS_SHOP, Point.of(COLUMN_4, ROW_3))
            .put(ConstructionType.CARPENTERS_STAND, Point.of(COLUMN_4, ROW_3))
            /*
             * TODO following construction should be visible in colony
             */
            // Dock
            .put(ConstructionType.SHIPYARD, Point.of(10, 10))
            .put(ConstructionType.DRYDOCK, Point.of(10, 10))
            .put(ConstructionType.DOCK, Point.of(10, 10))
            // stable
            .put(ConstructionType.LARGE_STABLES, Point.of(10, 10))
            .put(ConstructionType.STABLES, Point.of(10, 10))
            // Newspaper
            .put(ConstructionType.NEWSPAPER, Point.of(10, 10))
            .put(ConstructionType.PRINTING_PRESS, Point.of(10, 10))
            // Custom house
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

    private final Canvas canvas;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;
    
    private final List<ConstructionPainting> constructions = new ArrayList<>();
    
    private final VBox mainPanel = new VBox();

    @Inject
    public PanelColonyStructures(final ImageProvider imageProvider,
            final GameModelController gameModelController, final EventBus eventBus,
            final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        canvas = new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT);
        canvas.setOnDragEntered(this::onDragEntered);
        canvas.setOnDragExited(this::onDragExited);
        canvas.setOnDragOver(this::onDragOver);
        canvas.setOnDragDropped(this::onDragDropped);
        canvas.setOnDragDetected(this::onDragDetected);
        canvas.setOnMouseMoved(this::onMouseMoved);

        mainPanel.getStyleClass().add("colony-structures");
        mainPanel.getChildren().add(canvas);
//        mainPanel.setMinWidth(CANVAS_WIDTH);
//        mainPanel.setMinHeight(CANVAS_HEIGHT);
    }

    private void onMouseMoved(final MouseEvent event) {
        constructions.forEach(c -> {
            c.evaluateMouseMove(event);
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
            final Optional<Construction> oConstruction = findConstruction(point);
            if (oConstruction.isPresent()) {
                event.acceptTransferModes(TransferMode.MOVE);
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
        }
        event.consume();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Drag dropped");
        final Point point = Point.of(event.getX(), event.getY());
        final Optional<ConstructionSlot> loc = findConstructionSlot(point);
        if (loc.isPresent() && loc.get().isEmpty()) {
            final ConstructionSlot slot = loc.get();
            ClipboardEval.make(gameModelController.getModel(), event.getDragboard())
                    .tryReadUnit((unit, transferFrom) -> {
                        unit.placeToColonyStructureSlot(slot);
                        event.setDropCompleted(true);
                    });
        } else {
            final Optional<Construction> oConstruction = findConstruction(point);
            if (oConstruction.isPresent()) {
                final Construction construction = oConstruction.get();
                final Optional<ConstructionSlot> oSlot = construction.getFirstEmptySlot();
                if (oSlot.isPresent()) {
                    ClipboardEval.make(gameModelController.getModel(), event.getDragboard())
                            .tryReadUnit((unit, transferFrom) -> {
                                unit.placeToColonyStructureSlot(oSlot.get());
                                event.setDropCompleted(true);
                            });
                }
            }
        }
        event.consume();
    }

    private Optional<Construction> findConstruction(final Point point) {
        return constructions.stream().map(c -> c.findConstruction(point)).filter(c -> c.isPresent())
                .map(c -> c.get()).findAny();
    }

    private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
        return constructions.stream().map(c -> c.findConstructionSlot(point))
                .filter(c -> c.isPresent()).map(c -> c.get()).findAny();
    }

    void repaint(final Colony colony) {
        constructions.clear();

        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        colony.getConstructions().forEach(construction -> {
            final Point position = constructionPlaces.get(construction.getType());
            final Rectangle rec = Rectangle.ofPointAndSize(position, CONSTRUCTION_SIZE);
            final ConstructionPainting building = new ConstructionPainting(imageProvider, eventBus, i18n,
                    construction, colony.getGoodsStats(), rec);
            building.paint(gc);
            constructions.add(building);
        });
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
