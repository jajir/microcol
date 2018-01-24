package org.microcol.gui.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.FontService;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Contains methods for painting particular objects.
 */
public class PaintService {

	/**
	 * Unit's flag width.
	 */
	private static final int FLAG_WIDTH = 7;

	/**
	 * Unit's flag height.
	 */
	private static final int FLAG_HEIGHT = 12;

	/**
	 * Position of unit flag from top left tile corner.
	 */
	private final Point OWNERS_FLAG_POSITION = Point.of(1, 5);

	/**
	 * Position of unit image from top left tile corner.
	 */
	private final Point UNIT_IMAGE_POSITION = Point.of(2, 4);

	private final ImageProvider imageProvider;
	
	private final MapManager mapManager;
	
	private final Font colonyFont;

	@Inject
	public PaintService(final ImageProvider imageProvider, final MapManager mapManager,
			final FontService fontService) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.mapManager = Preconditions.checkNotNull(mapManager);
		this.colonyFont = fontService.getFont(FontService.FONT_CARDO_REGULAR, 16);
	}

	/**
	 * Draw unit to tile. 
	 * 
	 * @param graphics
	 *            required graphics to draw
	 * @param point
	 *            required point of top left corner or tile where to draw unit
	 * @param unit
	 *            required unit to draw
	 */
	public void paintUnit(final GraphicsContext graphics, final Point point, final Unit unit) {
		Point p = point.add(UNIT_IMAGE_POSITION);
		graphics.drawImage(imageProvider.getUnitImage(unit.getType()), p.getX(), p.getY());
		paintOwnersFlag(graphics, point.add(OWNERS_FLAG_POSITION), unit.getOwner());
	}

	public void paintColony(final GraphicsContext graphics, final Point point, final Colony colony) {
		Point p = point.add(UNIT_IMAGE_POSITION);
		graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_TOWN), p.getX(), p.getY());
		paintOwnersFlag(graphics, point.add(OWNERS_FLAG_POSITION), colony.getOwner());
		graphics.setFont(colonyFont);
		graphics.setTextAlign(TextAlignment.CENTER);
		graphics.setTextBaseline(VPos.CENTER);
		graphics.setFill(Color.BLACK);
		graphics.fillText(colony.getName(), p.getX() + 20, p.getY() + 55);
	}

	/**
	 * Draw unit to tile.
	 * 
	 * @param graphics
	 *            required graphics to draw
	 * @param point
	 *            required point of top left corner or tile where to draw unit
	 * @param unit
	 *            required unit to draw
	 * @param flagLeterImageName
	 *            required name of image that will be drawn into unit flag
	 */
	public void paintUnit(final GraphicsContext graphics, final Point point, final Unit unit,
			final String flagLeterImageName) {
		final Point p = point.add(UNIT_IMAGE_POSITION);
		graphics.drawImage(imageProvider.getUnitImage(unit.getType()), p.getX(), p.getY());
		paintOwnersFlag(graphics, point.add(OWNERS_FLAG_POSITION), unit.getOwner());
		graphics.drawImage(imageProvider.getImage(flagLeterImageName), point.getX() + 23, point.getY());
	}

	/**
	 * All units have flag containing color of owner. Method draw this flag.
	 * 
	 * @param graphics
	 *            required graphics
	 * @param point
	 *            required point where will be flag placed
	 * @param player
	 *            required player
	 */
	public void paintOwnersFlag(final GraphicsContext graphics, final Point point, final Player player) {
		graphics.setStroke(Color.LIGHTGREY);
		graphics.setLineWidth(1.5F);
		graphics.strokeRect(point.getX(), point.getY(), FLAG_WIDTH, FLAG_HEIGHT);
		if (player.isHuman()) {
			graphics.setFill(Color.YELLOW);
		} else {
			switch (player.getName().hashCode() % 4) {
			case 0:
				graphics.setFill(Color.RED);
				break;
			case 1:
				graphics.setFill(Color.GREEN);
				break;
			case 2:
				graphics.setFill(Color.MAGENTA);
				break;
			case 3:
				graphics.setFill(Color.BLUE);
				break;
			default:
				graphics.setFill(Color.GRAY);
				break;
			}
		}
		// TODO JJ vyzkouset na win, jak kresleni ramecku funguje
		graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 2, FLAG_HEIGHT - 2);
	}

	/**
	 * Paint one tile on game space.
	 * 
	 * @param graphics
	 *            required graphics where will be tile drawn
	 * @param point
	 *            required point where will be painted
	 * @param location
	 *            required map location that will be drawn
	 * @param terrain
	 *            required terrain object
	 * @param isHighlighted
	 *            when it's <code>true</code> than tile is highlighted
	 */
	public void paintTerrainOnTile(final GraphicsContext graphics, final Point point, final Location location,
			final Terrain terrain, final boolean isHighlighted) {
		//terrain tile
		final Image imageBackground = mapManager.getTerrainImage(terrain.getTerrainType(), location);
		graphics.drawImage(imageBackground, 0, 0,
				GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
				GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
		
		//prechody
		final Image imageCoast = mapManager.getCoatsImageAt(location);
		graphics.drawImage(imageCoast, 0, 0,
				GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
				GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
		
		if (terrain.isHasTrees()) {
			graphics.drawImage(mapManager.getTreeImage(location), point.getX(), point.getY());
		}
		if (isHighlighted) {
			graphics.setFill(new Color(0.95, 0.75, 0.90, 0.4F));
			graphics.fillRect(point.getX(), point.getY(), GamePanelView.TILE_WIDTH_IN_PX,
					GamePanelView.TILE_WIDTH_IN_PX);
		}
		final Image hiddenCoast = mapManager.getHiddenImageCoast(location);
		if (hiddenCoast != null) {
			graphics.drawImage(hiddenCoast, 0, 0,
					GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
					GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);			
		}
	}
	
}
