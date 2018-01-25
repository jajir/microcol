package org.microcol.model.event;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitMoveFinishedEvent extends ModelEvent {

	private final Unit unit;
	
	private final Path path;

	public UnitMoveFinishedEvent(final Model model, final Unit unit, final Path path) {
		super(model);

		this.unit = Preconditions.checkNotNull(unit);
		this.path = Preconditions.checkNotNull(path);
	}

	public Unit getUnit() {
		return unit;
	}
	
	public Location getTargetLocation(){
		return path.getTarget();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("unit", unit).toString();
	}

}
