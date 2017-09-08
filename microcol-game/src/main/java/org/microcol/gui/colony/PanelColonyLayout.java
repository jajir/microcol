package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.panelview.GamePanelView;
import org.microcol.gui.panelview.PaintService;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelColonyLayout extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelColonyLayout.class);

	private Canvas canvas;

	private Colony colony;

	private final PaintService paintService;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	private final ClickableArea clickableArea = new ClickableArea();
	
	@Inject
	public PanelColonyLayout(final PaintService paintService, final ImageProvider imageProvider,
			final GameController gameController) {
		super("Colony layout", new Label("Colony layout"));
		this.paintService = Preconditions.checkNotNull(paintService);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		final int size = 3 * GamePanelView.TILE_WIDTH_IN_PX;
		canvas = new Canvas(size, size);
		getContentPane().getChildren().add(canvas);
		canvas.setOnDragEntered(this::onDragEntered);
		canvas.setOnDragExited(this::onDragExited);
		canvas.setOnDragOver(this::onDragOver);
		canvas.setOnDragDropped(this::onDragDropped);
	}
	
	private final void onDragEntered(final DragEvent event) {
		logger.debug("Drag entered");
	}

	private final void onDragExited(final DragEvent event) {
		logger.debug("Drag Exited");
	}

	private final void onDragOver(final DragEvent event) {
		logger.debug("Drag Over");
		final Point point = Point.of(event.getX(), event.getY());
		final Optional<Location> loc= clickableArea.getLocation(point);
		if(loc.isPresent()){
			logger.debug("was clicked at: " + loc.get());
		}
	}

	private final void onDragDropped(final DragEvent event) {
		logger.debug("Drag dropped");
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
		final Point point = Point.of(colonyField.getLocation()).add(centre);
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
