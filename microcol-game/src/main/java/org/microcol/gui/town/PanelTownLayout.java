package org.microcol.gui.town;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.panelview.GamePanelView;
import org.microcol.gui.panelview.PaintService;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Terrain;
import org.microcol.model.Town;
import org.microcol.model.TownField;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;

/**
 * Show 3 x 3 tiles occupied by colony. User can assign worker to work outside
 * of colony.
 */
public class PanelTownLayout extends TitledPanel {

	private Canvas canvas;

	private Town town;

	private final PaintService paintService;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	@Inject
	public PanelTownLayout(final PaintService paintService, final ImageProvider imageProvider,
			final GameController gameController) {
		super("Colony layout", new Label("Colony layout"));
		this.paintService = Preconditions.checkNotNull(paintService);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		final int size = 3 * GamePanelView.TILE_WIDTH_IN_PX;
		canvas = new Canvas(size, size);
		getContentPane().getChildren().add(canvas);
	}

	public void setTown(final Town town) {
		this.town = Preconditions.checkNotNull(town);
		paint(canvas.getGraphicsContext2D());
	}

	private void paint(final GraphicsContext gc) {
		town.getTownSection().forEach(townSection -> paintSection(gc, townSection));
		paintTile(gc, gameController.getModel().getMap().getTerrainAt(town.getLocation()),
				Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX));
	}

	private void paintSection(final GraphicsContext gc, final TownField townSection) {
		final Terrain terrain = townSection.getTerrain();
		final Point centre = Point.of(1, 1).multiply(GamePanelView.TILE_WIDTH_IN_PX);
		final Point point = Point.of(townSection.getLocation()).add(centre);
		paintTile(gc, terrain, point);
		if (!townSection.isEmpty()) {
			gc.drawImage(imageProvider.getUnitImage(townSection.getUnit().getType()), point.getX(), point.getY());
		}
	}

	private void paintTile(final GraphicsContext gc, final Terrain terrain, final Point point) {
		gc.drawImage(imageProvider.getTerrainImage(terrain), 0, 0, GamePanelView.TILE_WIDTH_IN_PX,
				GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(), GamePanelView.TILE_WIDTH_IN_PX,
				GamePanelView.TILE_WIDTH_IN_PX);
	}

}
