package org.microcol.model.event;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class UnitMovedEvent extends ModelEvent {
	private final Unit unit;
	private final Location start;
	private final Location end;

	public UnitMovedEvent(final Model model, final Unit unit, final Location start, final Location end) {
		super(model);

		this.unit = Preconditions.checkNotNull(unit);
		this.start = Preconditions.checkNotNull(start);
		this.end = Preconditions.checkNotNull(end);
	}

	public Unit getUnit() {
		return unit;
	}

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		return end;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("unit", unit)
			.add("start", start)
			.add("end", end)
			.toString();
	}
	
	/**
	 * Return <code>true</code> when player can see move. Player can see move
	 * when start location or end location is in explored part of world.
	 * 
	 * @param player
	 *            required player
	 * @return return <code>true</code> when user can see move otherwise return
	 *         <code>false</code>
	 */
	public boolean canPlayerSeeMove(final Player player){
		Preconditions.checkNotNull(player);
		return player.isVisible(start) || player.isVisible(end);
	}
}
