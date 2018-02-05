package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.gamepanel.PaintService;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelColonyFields extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelColonyFields.class);

	private Canvas canvas;

	private Colony colony;

	private final ImageProvider imageProvider;

	private final GameModelController gameModelController;

	private final ColonyDialogCallback colonyDialog;

	private final ClickableArea clickableArea = new ClickableArea();

	private final TileClickListener tileClickListener;

	private final ContextMenu contextMenu;
	
	private final PaintService paintService;

	@Inject
	public PanelColonyFields(final ImageProvider imageProvider, final GameModelController gameModelController,
			final ColonyDialogCallback colonyDialog, final PaintService paintService) {
		super("Colony layout", new Label("Colony layout"));
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		this.paintService = Preconditions.checkNotNull(paintService);
		final int size = 3 * GamePanelView.TILE_WIDTH_IN_PX;
		canvas = new Canvas(size, size);
		getContentPane().getChildren().add(canvas);
		canvas.setOnDragOver(this::onDragOver);
		canvas.setOnDragDropped(this::onDragDropped);
		canvas.setOnDragDetected(this::onDragDetected);
		canvas.setOnMousePressed(this::onMousePressed);
		canvas.setOnMouseReleased(this::onMouseReleased);
		tileClickListener = new TileClickListener(clickableArea, this::onClickDirection);
		contextMenu = new ContextMenu();
		contextMenu.getStyleClass().add("popup");
		contextMenu.setAutoHide(true);
		canvas.setOnContextMenuRequested(this::onContextMenuRequested);
	}

	private void onContextMenuRequested(final ContextMenuEvent event) {
		final Optional<Location> direction = clickableArea.getDirection(Point.of(event.getX(), event.getY()));
		if (direction.isPresent()) {
			final ColonyField colonyField = colony.getColonyFieldInDirection(direction.get());
			final Terrain terrain = colonyField.getTerrain();
			if (!colonyField.isEmpty()) {
				contextMenu.getItems().clear();
				colonyField.getTerrainType()
						.getProductions()
						.stream()
						.filter(production -> terrain.canProduceAmmount(production) > 0)
						.forEach(production -> {
							final MenuItem item = new MenuItem(
							production.getGoodType().name() + "   " + terrain.canProduceAmmount(production));
					item.setOnAction(evt -> {
						colonyField.setProducedGoodType(production.getGoodType());
						colonyDialog.repaint();
					});
					contextMenu.getItems().add(item);
				});
				contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
			}
		}
	}

	private final void onMousePressed(final MouseEvent event) {
		tileClickListener.onMousePressed(event);
		if(contextMenu.isShowing())
			contextMenu.hide();
	}

	private final void onMouseReleased(final MouseEvent event) {
		tileClickListener.onMouseReleased(event);
	}

	private final void onClickDirection(final Location direction) {
		final ColonyField colonyField = colony.getColonyFieldInDirection(direction);
		if (!colonyField.isEmpty()) {
			//FIXME why is this code here?
		}
	}

	private final void onDragDetected(final MouseEvent event) {
		logger.debug("Drag detected");
		final Point point = Point.of(event.getX(), event.getY());
		final Optional<Location> loc = clickableArea.getDirection(point);
		if (loc.isPresent()) {
			final ColonyField colonyField = colony.getColonyFieldInDirection(loc.get());
			if (!colonyField.isEmpty()) {
				final Image image = imageProvider.getUnitImage(colonyField.getUnit());
				final Dragboard db = canvas.startDragAndDrop(TransferMode.MOVE);
				ClipboardWritter.make(db).addImage(image).addTransferFromColonyField(loc.get())
						.addUnit(colonyField.getUnit()).build();
				event.consume();
			}
		}
	}

	private final void onDragOver(final DragEvent event) {
		logger.debug("Drag Over");
		if (isItUnit(event.getDragboard())) {
			final Point point = Point.of(event.getX(), event.getY());
			final Optional<Location> loc = clickableArea.getDirection(point);
			if (loc.isPresent() && loc.get().isDirection()) {
				final ColonyField colonyField = colony.getColonyFieldInDirection(loc.get());
				if (colonyField.isEmpty()) {
					event.acceptTransferModes(TransferMode.MOVE);
					event.consume();
					return;
				}
			}
		}
		event.acceptTransferModes(TransferMode.NONE);
		event.consume();
	}

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Drag dropped");
		final Point point = Point.of(event.getX(), event.getY());
		final Optional<Location> loc = clickableArea.getDirection(point);
		if (loc.isPresent() && loc.get().isDirection()) {
			final ColonyField colonyField = colony.getColonyFieldInDirection(loc.get());
			if (colonyField.isEmpty()) {
				final Dragboard db = event.getDragboard();
				ClipboardReader.make(gameModelController.getModel(), db).tryReadUnit((unit, transferFrom) -> {
					unit.placeToColonyField(colonyField, GoodType.CORN);
					event.setDropCompleted(true);
					colonyDialog.repaint();
				});
			}
			logger.debug("was clicked at: " + loc.get());
		}
		event.consume();
	}

	private boolean isItUnit(final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		return ClipboardReader.make(gameModelController.getModel(), db).getUnit().isPresent();
	}

	public void setColony(final Colony colony) {
		this.colony = Preconditions.checkNotNull(colony);
		paint(canvas.getGraphicsContext2D());
	}

	private void paint(final GraphicsContext gc) {
		colony.getColonyFields().forEach(colonyField -> paintColonyField(gc, colonyField));
		final Point colonyPoint = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
		paintService.paintTerrainOnTile(gc, colonyPoint, colony.getLocation(),
				gameModelController.getModel().getMap().getTerrainAt(colony.getLocation()), false);
		paintService.paintColony(gc, colonyPoint, colony, false);
	}

	private void paintColonyField(final GraphicsContext gc, final ColonyField colonyField) {
		final Terrain terrain = colonyField.getTerrain();
		final Point centre = Point.of(1, 1).multiply(GamePanelView.TILE_WIDTH_IN_PX);
		final Point point = Point.of(colonyField.getDirection()).add(centre);
		paintService.paintTerrainOnTile(gc, point, colonyField.getLocation(), terrain, false);
		if (!colonyField.isEmpty()) {
			gc.drawImage(imageProvider.getUnitImage(colonyField.getUnit().getType()), point.getX(), point.getY());
			//TODO on ocean show fish
			gc.drawImage(imageProvider.getGoodTypeImage(colonyField.getProducedGoodType()), point.getX(), point.getY(),
					25, 25);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setTextBaseline(VPos.CENTER);
			gc.setFill(Color.BLACK);
			gc.fillText("x " + colonyField.getProducedGoodsAmmount(), point.getX() + 10, point.getY() + 28);
		}
	}
	
}
