package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class UnitAttackedEvent extends ModelEvent {
	private final Unit attacker;
	private final Unit defender;
	private final Unit destroyed;

	public UnitAttackedEvent(final Model model, final Unit attacker, final Unit defender, final Unit destroyed) {
		super(model);

		this.attacker = Preconditions.checkNotNull(attacker);
		this.defender = Preconditions.checkNotNull(defender);
		this.destroyed = Preconditions.checkNotNull(destroyed);
	}

	public Unit getAttacker() {
		return attacker;
	}

	public Unit getDefender() {
		return defender;
	}

	public Unit getDestroyed() {
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
