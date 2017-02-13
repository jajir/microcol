package model;

import com.google.common.base.MoreObjects;

public class Ship extends Unit {

	private final int type;

	private int availableSteps;

	public Ship(final int type) {
		this.type = type;
		availableSteps = 5;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Ship.class).add("type", type).add("availableSteps", availableSteps)
				.toString();
	}

	public int getType() {
		return type;
	}

	public void resetActionPoints() {
		availableSteps = 5;
	}

	public void decreaseActionPoint(int howMuch) {
		availableSteps -= howMuch;
	}

	public int getAvailableSteps() {
		return availableSteps;
	}

}
