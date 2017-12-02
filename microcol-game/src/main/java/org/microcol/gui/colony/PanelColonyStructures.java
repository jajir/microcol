package org.microcol.gui.colony;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.ConstructionType;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Show building factories and other structures build in colony.
 */
public class PanelColonyStructures extends TitledPanel {
	
	private final Logger logger = LoggerFactory.getLogger(PanelColonyStructures.class);

	private final static int COLUMN_1 = 90;
	private final static int COLUMN_2 = 250;
	private final static int COLUMN_3 = 410;

	private final static int ROW_1 = 10;
	private final static int ROW_2 = 100;
	private final static int ROW_3 = 190;
	
	private final static int PRODUCTION_TEXT_X = 0;
	private final static int PRODUCTION_TEXT_Y = 55;
	
	private final static Point PRODUCTION_TEXT = Point.of(PRODUCTION_TEXT_X, PRODUCTION_TEXT_Y);
	
	private final static Point[] SLOT_POSITIONS = new Point[] {
			Point.of(-58, 10),
			Point.of(-18, 10),
			Point.of(22, 10) };

	/**
	 * Following structure define position of constructions images on colony map.
	 */
	private final static Map<ConstructionType, Point> constructionPlaces = ImmutableMap
			.<ConstructionType, Point>builder()
			.put(ConstructionType.TOWN_HALL,            Point.of(COLUMN_2, ROW_1))
			.put(ConstructionType.LUMBER_MILL,          Point.of(COLUMN_1, ROW_3))
			.put(ConstructionType.CARPENTERS_SHOP,      Point.of(COLUMN_1, ROW_3))
			.put(ConstructionType.IRON_WORKS,           Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.BLACKSMITHS_SHOP,     Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.BLACKSMITHS_HOUSE,    Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.FORTRESS,             Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.FORT,                 Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.STOCKADE,             Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.CIGAR_FACTORY,        Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TOBACCONISTS_SHOP,    Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TOBACCONISTS_HOUSE,   Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TEXTILE_MILL,         Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.WEAVERS_SHOP,         Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.WEAVERS_HOUSE,        Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.RUM_FACTORY,          Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.RUM_DISTILLERY,       Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.RUM_DISTILLERS_HOUSE, Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.FUR_FACTORY,          Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.FUR_TRADING_POST,     Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.FUR_TRADERS_HOUSE,    Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.ARSENAL,              Point.of(10, 10))
			.put(ConstructionType.MAGAZINE,             Point.of(10, 10))
			.put(ConstructionType.ARMORY,               Point.of(10, 10))
			.put(ConstructionType.SHIPYARD,             Point.of(10, 10))
			.put(ConstructionType.DRYDOCK,              Point.of(10, 10))
			.put(ConstructionType.DOCK,                 Point.of(10, 10))
			.put(ConstructionType.UNIVERSITY,           Point.of(10, 10))
			.put(ConstructionType.COLLEGE,              Point.of(10, 10))
			.put(ConstructionType.SCHOOLHOUSE,          Point.of(10, 10))
			.put(ConstructionType.WAREHOUSE_EXPANSION,  Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.WAREHOUSE,            Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.BASIC_WAREHOUSE,      Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.STABLES,              Point.of(10, 10))
			.put(ConstructionType.CATHEDRAL,            Point.of(COLUMN_3, ROW_1))
			.put(ConstructionType.CHURCH,               Point.of(COLUMN_3, ROW_1))
			.put(ConstructionType.NEWSPAPER,            Point.of(10, 10))
			.put(ConstructionType.PRINTING_PRESS,       Point.of(10, 10))
			.put(ConstructionType.CUSTOM_HOUSE,         Point.of(10, 10))
			.build();
	
	{
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
	
	private final ColonyDialogCallback colonyDialog;
	
	private Map<Rectangle, ConstructionSlot> slots;
	
	@Inject
	public PanelColonyStructures(final LocalizationHelper localizationHelper, final ImageProvider imageProvider,
			final GameModelController gameModelController, final ColonyDialogCallback colonyDialog) {
		super("Colony Structures", null);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		getContentPane().getChildren().add(canvas);
		setMinWidth(CANVAS_WIDTH);
		setMinHeight(CANVAS_HEIGHT);
		canvas.setOnDragEntered(this::onDragEntered);
		canvas.setOnDragExited(this::onDragExited);
		canvas.setOnDragOver(this::onDragOver);
		canvas.setOnDragDropped(this::onDragDropped);
		canvas.setOnDragDetected(this::onDragDetected);
	}
	
	private final void onDragDetected(final MouseEvent event) {
		logger.debug("Drag detected");
		final Point point = Point.of(event.getX(), event.getY());
		final Optional<ConstructionSlot> loc = findConstructionSlot(point);
		if (loc.isPresent() && !loc.get().isEmpty()) {
			final Unit unit = loc.get().getUnit();
			final Image image = imageProvider.getUnitImage(unit);
			final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
			ClipboardWritter.make(db).addImage(image).addTransferFromConstructionSlot().addUnit(unit).build();
			event.consume();
		}
		event.consume();
	}

	@SuppressWarnings("unused")
	private final void onDragEntered(final DragEvent event) {
		logger.debug("Drag entered");
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		logger.debug("Drag Exited");
	}

	private final void onDragOver(final DragEvent event) {
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

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Drag dropped");
		final Point point = Point.of(event.getX(), event.getY());
		final Optional<ConstructionSlot> loc = findConstructionSlot(point);
		if (loc.isPresent() && loc.get().isEmpty()) {
			ConstructionSlot slot = loc.get();
			final Dragboard db = event.getDragboard();
			ClipboardReader.make(gameModelController.getModel(), db).tryReadUnit((unit, transferFrom) -> {
				unit.placeToColonyStructureSlot(slot);
				event.setDropCompleted(true);
				colonyDialog.repaint();
			});
		}
		event.consume();
	}
	
	private Optional<ConstructionSlot> findConstructionSlot(final Point point) {
		return slots.entrySet().stream().filter(entry -> entry.getKey().isIn(point)).map(entry -> entry.getValue())
				.findAny();
	}
	
	void repaint(final Colony colony){
		slots = new HashMap<>();
		final GraphicsContext gc = canvas.getGraphicsContext2D();
		final Point square = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
		gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		colony.getConstructions().forEach(construction -> {
			final Point position = constructionPlaces.get(construction.getType());
			Preconditions.checkNotNull(position,
					String.format("There is no defined position for construction type '%s'", position));
			final String name = localizationHelper.getConstructionTypeName(construction.getType());
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setTextBaseline(VPos.CENTER);
			gc.setFill(Color.BLACK);
			gc.fillText(name, position.getX(), position.getY());
			gc.setStroke(Color.DARKGRAY);
			final AtomicInteger cx = new AtomicInteger(0);
			construction.getConstructionSlots().forEach(constructionSlot -> {
				final Point topLeftCorner = position.add(SLOT_POSITIONS[cx.get()]);
				slots.put(Rectangle.ofPointAndSize(topLeftCorner, square), constructionSlot);
				paintWorkerContainer(gc, topLeftCorner);
				if (!constructionSlot.isEmpty()) {
					gc.drawImage(imageProvider.getUnitImage(constructionSlot.getUnit().getType()), topLeftCorner.getX(),
							topLeftCorner.getY());
				}
				cx.incrementAndGet();
			});
			paintProduction(gc, position, colony, construction);
		});
	}
	
	private void paintProduction(final GraphicsContext gc, final Point point, final Colony colony, final Construction construction) {
		if (construction.getType().getProduce().isPresent()) {
			final Point prod = point.add(PRODUCTION_TEXT);
			final String toWrite = "x " + construction.getProductionPerTurn(colony);
			gc.fillText(toWrite, prod.getX(), prod.getY());
			final Text theText = new Text(toWrite);
			theText.setFont(gc.getFont());
			final double width = theText.getBoundsInLocal().getWidth();
			gc.drawImage(imageProvider.getGoodTypeImage(construction.getType().getProduce().get()),
					prod.getX() - width / 2 - GOOD_ICON_WIDTH, prod.getY() - 10, GOOD_ICON_WIDTH, GOOD_ICON_WIDTH);
		}
	}
	
	private void paintWorkerContainer(final GraphicsContext gc, final Point point){
		gc.strokeRect(point.getX(), point.getY(), GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
	}
	
}
