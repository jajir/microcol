package org.microcol.gui.panelview;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Draw walk animation based on predefined path.
 * 
 */
public class AnimationPartWalk implements AnimationPart {

	/**
	 * Total path that have to animated, it's list of location on map.
	 */
	private final List<Location> path;

	/**
	 * Moving unit.
	 */
	private final Unit unit;

	/**
	 * Path computing.
	 */
	private final PathPlanning pathPlanning;

	/**
	 * Contains locations for move between two tiles.
	 */
	private final List<Point> partialPath;

	/**
	 * Draw partial part of path.
	 */
	private Location partialPathFrom;

	private Point nextCoordinates;

	private final PaintService paintService;

	private final ExcludePainting excludePainting;
	
	private final Area area;

	public AnimationPartWalk(final PathPlanning pathPlanning, final List<Location> path, final Unit unit,
			final PaintService paintService, final ExcludePainting excludePainting, final Area area) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty(), "Path can't be empty");
		Preconditions.checkArgument(path.size() > 1, "Path should contains more than one locations");
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.paintService = Preconditions.checkNotNull(paintService);
		this.unit = Preconditions.checkNotNull(unit);
		this.excludePainting = Preconditions.checkNotNull(excludePainting);
		this.path = new ArrayList<>(path);
		this.area = Preconditions.checkNotNull(area);
		excludePainting.excludeUnit(unit);
		partialPathFrom = this.path.remove(0);
		partialPath = new ArrayList<>();
		nextStep();
		Preconditions.checkArgument(hasNextStep(), "Animation can't start without any steps.");
	}

	@Override
	public void nextStep() {
		if (partialPath.isEmpty()) {
			nextCoordinates = null;
			if (!path.isEmpty()) {
				final Point from = area.convert(partialPathFrom);
				final Point to = area.convert(path.get(0));
				pathPlanning.paintPath(from, to, point -> partialPath.add(point));
				partialPathFrom = path.remove(0);
			}
		}
		if (partialPath.isEmpty()) {
			nextCoordinates = null;
		} else {
			nextCoordinates = partialPath.remove(0);
		}
		if (nextCoordinates == null) {
			excludePainting.includeUnit(unit);
		}
	}

	/**
	 * Provide information if animation should continue.
	 * 
	 * @return return <code>true</code> when not all animation was drawn, it
	 *         return <code>false</code> when all animation is done
	 */
	@Override
	public boolean hasNextStep() {
		return nextCoordinates != null;
	}

	public Point getNextCoordinates() {
		return nextCoordinates;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public void paint(final GraphicsContext graphics, final Area area) {
		if (area.isInArea(getNextCoordinates())) {
			final Point point = getNextCoordinates();
			paintService.paintUnit(graphics, point, getUnit(), ImageProvider.IMG_TILE_MODE_MOVE);
		}
	}

}
