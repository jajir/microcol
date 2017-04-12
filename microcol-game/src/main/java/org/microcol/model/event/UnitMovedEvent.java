package org.microcol.model.event;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class UnitMovedEvent extends ModelEvent {
	private final Unit unit;
	private final Location start;
	private final Path path;

	public UnitMovedEvent(final Model model, final Unit unit, final Location start, final Path path) {
		super(model);

		this.unit = Preconditions.checkNotNull(unit);
		this.start = Preconditions.checkNotNull(start);
		this.path = Preconditions.checkNotNull(path);
	}

	public Unit getUnit() {
		return unit;
	}

	public Location getStart() {
		return start;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("unit", unit)
			.add("start", start)
			.add("path", path)
			.toString();
	}
}
