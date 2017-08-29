package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * Helps build town.
 */
public class TownBuilder {

	private final String name;

	private final PlayerBuilder playerBuilder;

	private Location location;

	private boolean defaultCostructions = false;

	private final Set<ConstructionType> constructionTypes = new HashSet<>();

	private final List<UnitPlace> unitPlaces = new ArrayList<>();

	private final List<FieldPlace> fieldPlaces = new ArrayList<>();

	public TownBuilder(final String name, final PlayerBuilder playerBuilder) {
		this.name = Preconditions.checkNotNull(name);
		this.playerBuilder = Preconditions.checkNotNull(playerBuilder);
	}

	public PlayerBuilder make() {
		return playerBuilder;
	}

	public TownBuilder setLocation(final Location location) {
		this.location = location;
		return this;
	}

	public TownBuilder setDefaultConstructions(final boolean defaultCostructions) {
		this.defaultCostructions = defaultCostructions;
		return this;
	}

	/**
	 * Allows to specify extra construction in town.
	 * 
	 * @param constructionType
	 *            required construction type
	 * @return town builder
	 */
	public TownBuilder setConstruction(final ConstructionType constructionType) {
		constructionTypes.add(Preconditions.checkNotNull(constructionType));
		return this;
	}

	public TownBuilder setWorker(final ConstructionType constructionType, final int position, final UnitType unitType) {
		Preconditions.checkNotNull(constructionType);
		Preconditions.checkNotNull(unitType);
		Preconditions.checkArgument(position >= 0 && position < 3, "Position is not within range 0,1,2.");
		unitPlaces.add(new UnitPlace(constructionType, position, unitType));
		return this;
	}

	public TownBuilder setWorker(final Location fieldDirection, final UnitType unitType) {
		Preconditions.checkNotNull(fieldDirection);
		Preconditions.checkNotNull(unitType);
		fieldPlaces.add(new FieldPlace(fieldDirection, unitType));
		return this;
	}

	String getName() {
		return name;
	}

	Location getLocation() {
		return location;
	}

	boolean isDefaultCostructions() {
		return defaultCostructions;
	}

	Set<ConstructionType> getConstructionTypes() {
		return constructionTypes;
	}

	class UnitPlace {

		private final ConstructionType constructionType;
		private final int position;
		private final UnitType unitType;

		UnitPlace(final ConstructionType constructionType, final int position, final UnitType unitType) {
			this.constructionType = constructionType;
			this.position = position;
			this.unitType = unitType;
		}

		ConstructionType getConstructionType() {
			return constructionType;
		}

		int getPosition() {
			return position;
		}

		UnitType getUnitType() {
			return unitType;
		}

	}

	static class FieldPlace {

		private final Location fieldDirection;
		private final UnitType unitType;

		FieldPlace(final Location fieldDirection, final UnitType unitType) {
			this.fieldDirection = Preconditions.checkNotNull(fieldDirection);
			this.unitType = Preconditions.checkNotNull(unitType);
		}

		public Location getFieldDirection() {
			return fieldDirection;
		}

		public UnitType getUnitType() {
			return unitType;
		}

	}

	List<UnitPlace> getUnitPlaces() {
		return unitPlaces;
	}

	public List<FieldPlace> getFieldPlaces() {
		return fieldPlaces;
	}

}
