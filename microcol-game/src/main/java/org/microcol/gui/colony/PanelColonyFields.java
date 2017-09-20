package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.panelview.GamePanelView;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelColonyFields extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelColonyFields.class);

	private Canvas canvas;

	private Colony colony;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	private final ColonyDialogCallback colonyDialog;

	private final ClickableArea clickableArea = new ClickableArea();

	private final TileClickListener tileClickListener;

	public PanelColonyFields(final ImageProvider imageProvider, final GameController gameController,
			final ColonyDialogCallback colonyDialog) {
		super("Colony layout", new Label("Colony layout"));
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
		final int size = 3 * GamePanelView.TILE_WIDTH_IN_PX;
		canvas = new Canvas(size, size);
		getContentPane().getChildren().add(canvas);
		canvas.setOnDragEntered(this::onDragEntered);
		canvas.setOnDragExited(this::onDragExited);
		canvas.setOnDragOver(this::onDragOver);
		canvas.setOnDragDropped(this::onDragDropped);
		canvas.setOnDragDetected(this::onDragDetected);
		canvas.setOnMousePressed(this::onMousePressed);
		canvas.setOnMouseReleased(this::onMouseReleased);
		tileClickListener = new TileClickListener(clickableArea, this::onClickDirection);
	}

	private final void onMousePressed(final MouseEvent event) {
		tileClickListener.onMousePressed(event);
	}

	private final void onMouseReleased(final MouseEvent event) {
		tileClickListener.onMouseReleased(event);
	}

	private final void onClickDirection(final Location direction) {
		final ColonyField colonyField = colony.getColonyFieldInDirection(direction);
		if (!colonyField.isEmpty()) {
			System.out.println("clicked: " + colonyField);
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

	private final void onDragEntered(final DragEvent event) {
		logger.debug("Drag entered");
	}

	private final void onDragExited(final DragEvent event) {
		logger.debug("Drag Exited");
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
				ClipboardReader.make(gameController.getModel(), db).tryReadUnit((unit, transferFrom) -> {
					unit.placeToColonyField(colonyField);
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
		return ClipboardReader.make(gameController.getModel(), db).getUnit().isPresent();
	}

	public void setColony(final Colony colony) {
		this.colony = Preconditions.checkNotNull(colony);
		paint(canvas.getGraphicsContext2D());
	}

	private void paint(final GraphicsContext gc) {
		colony.getColonyFields().forEach(colonyField -> paintSection(gc, colonyField));
		paintTile(gc, gameController.getModel().getMap().getTerrainAt(colony.getLocation()),
				Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX));
	}

	private void paintSection(final GraphicsContext gc, final ColonyField colonyField) {
		final Terrain terrain = colonyField.getTerrain();
		final Point centre = Point.of(1, 1).multiply(GamePanelView.TILE_WIDTH_IN_PX);
		final Point point = Point.of(colonyField.getDirection()).add(centre);
		paintTile(gc, terrain, point);
		if (!colonyField.isEmpty()) {
			gc.drawImage(imageProvider.getUnitImage(colonyField.getUnit().getType()), point.getX(), point.getY());
		}
	}

	private void paintTile(final GraphicsContext gc, final Terrain terrain, final Point point) {
		gc.drawImage(imageProvider.getTerrainImage(terrain), 0, 0, GamePanelView.TILE_WIDTH_IN_PX,
				GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(), GamePanelView.TILE_WIDTH_IN_PX,
				GamePanelView.TILE_WIDTH_IN_PX);
	}

}
