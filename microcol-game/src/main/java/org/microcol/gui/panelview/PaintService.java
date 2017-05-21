package org.microcol.gui.panelview;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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

	@Inject
	public PaintService(final ImageProvider imageProvider) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
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
		Point p = point.add(UNIT_IMAGE_POSITION);
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
		graphics.setFill(Color.BLACK);
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
		graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 1, FLAG_HEIGHT - 1);
	}

	public void paintDebugInfo(final GraphicsContext graphics, final VisualDebugInfo visualDebugInfo, final Area area) {
		visualDebugInfo.getLocations().stream().filter(location -> area.isVisible(location)).forEach(location -> {
			final Point p = area.convertToPoint(location).add(10, 4);
			graphics.setFill(Color.WHITE);
			graphics.fillRect(p.getX() + 1, p.getY() + 1, FLAG_WIDTH - 1, FLAG_HEIGHT - 1);
		});
	}

}
