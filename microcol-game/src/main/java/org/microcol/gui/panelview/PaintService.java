package org.microcol.gui.panelview;

import java.awt.Color;
import java.awt.Graphics2D;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

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
	public void paintUnit(final Graphics2D graphics, final Point point, final Unit unit) {
		// TODO JJ replace magic numbers
		Point p = point.add(2, 4);
		graphics.drawImage(imageProvider.getUnitImage(unit.getType()), p.getX(), p.getY(), null);
		paintOwnersFlag(graphics, point.add(1, 5), unit.getOwner());
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
	public void paintUnit(final Graphics2D graphics, final Point point, final Unit unit,
			final String flagLeterImageName) {
		// TODO JJ replace magic numbers
		Point p = point.add(2, 4);
		graphics.drawImage(imageProvider.getUnitImage(unit.getType()), p.getX(), p.getY(), null);
		paintOwnersFlag(graphics, point.add(1, 5), unit.getOwner());
		graphics.drawImage(imageProvider.getImage(flagLeterImageName), point.getX() + 35 - 12, point.getY(), null);
	}

	/**
	 * All units have flag containing color of owner. Method draw this flag.
	 */
	public void paintOwnersFlag(final Graphics2D graphics, final Point point, final Player player) {
		graphics.setColor(Color.BLACK);
		graphics.drawRect(point.getX(), point.getY(), FLAG_WIDTH, FLAG_HEIGHT);
		// TODO JJ player's color should be property
		if (player.isHuman()) {
			graphics.setColor(Color.YELLOW);
		} else {
			switch (player.getName().hashCode() % 4) {
			case 0:
				graphics.setColor(Color.RED);
				break;
			case 1:
				graphics.setColor(Color.GREEN);
				break;
			case 2:
				graphics.setColor(Color.MAGENTA);
				break;
			case 3:
				graphics.setColor(Color.BLUE);
				break;
			default:
				graphics.setColor(Color.gray);
				break;
			}
		}
		graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 1, FLAG_HEIGHT - 1);
	}

	public void paintDebugInfo(final Graphics2D graphics, final VisualDebugInfo visualDebugInfo, final Area area) {
		visualDebugInfo.getLocations().stream().filter(location -> area.isInArea(location)).forEach(location -> {
			final Point p = area.convert(location).add(10, 4);
			graphics.setColor(Color.white);
			graphics.fillRect(p.getX() + 1, p.getY() + 1, FLAG_WIDTH - 1, FLAG_HEIGHT - 1);
		});
	}

}
