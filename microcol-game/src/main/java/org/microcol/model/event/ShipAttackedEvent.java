package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Ship;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ShipAttackedEvent extends ModelEvent {
	private final Ship attacker;
	private final Ship defender;
	private final Ship destroyed;

	public ShipAttackedEvent(final Model model, final Ship attacker, final Ship defender, final Ship destroyed) {
		super(model);

		this.attacker = Preconditions.checkNotNull(attacker);
		this.defender = Preconditions.checkNotNull(defender);
		this.destroyed = Preconditions.checkNotNull(destroyed);
	}

	public Ship getAttacker() {
		return attacker;
	}

	public Ship getDefender() {
		return defender;
	}

	public Ship getDestroyed() {
		return destroyed;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("attacker", attacker)
			.add("defender", defender)
			.add("destroyed", destroyed)
			.toString();
	}
}
