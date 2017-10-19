package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Helps build colony.
 */
public class ColonyBuilder {

	private final ColonyPo colonyPo;
	
	private final PlayerBuilder playerBuilder;

	private boolean defaultCostructions = false;

	private final Set<ConstructionType> constructionTypes = new HashSet<>();

	private final List<UnitPlace> unitPlaces = new ArrayList<>();

	private final List<FieldPlace> fieldPlaces = new ArrayList<>();

	private final List<GoodAmount> goodAmounts = new ArrayList<>();

	public ColonyBuilder(final String name, final PlayerBuilder playerBuilder, final ModelPo modelPo) {
		this.colonyPo = new ColonyPo();
		colonyPo.setName(Preconditions.checkNotNull(name));
		colonyPo.setOwnerName(playerBuilder.getPlayerPo().getName());
		this.playerBuilder = Preconditions.checkNotNull(playerBuilder);
		modelPo.getColonies().add(colonyPo);
	}

	public PlayerBuilder build() {
		return playerBuilder;
	}

	public ColonyBuilder setLocation(final Location location) {
		colonyPo.setLocation(location);
		return this;
	}

	public ColonyBuilder setDefaultConstructions(final boolean defaultCostructions) {
		//TODO construct default constructions.
		this.defaultCostructions = defaultCostructions;
		return this;
	}

	/**
	 * Allows to specify extra construction in colony.
	 * 
	 * @param constructionType
	 *            required construction type
	 * @return colony builder
	 */
	public ColonyBuilder setConstruction(final ConstructionType constructionType) {
		constructionTypes.add(Preconditions.checkNotNull(constructionType));
		return this;
	}

	public ColonyBuilder setWorker(final ConstructionType constructionType, final int position, final UnitType unitType) {
		Preconditions.checkNotNull(constructionType);
		Preconditions.checkNotNull(unitType);
		Preconditions.checkArgument(position >= 0 && position < 3, "Position is not within range 0,1,2.");
		unitPlaces.add(new UnitPlace(constructionType, position, unitType));
		return this;
	}

	public ColonyBuilder setWorker(final Location fieldDirection, final UnitType unitType,
			final GoodType producedGoodType) {
		Preconditions.checkNotNull(fieldDirection);
		Preconditions.checkNotNull(unitType);
		fieldPlaces.add(new FieldPlace(fieldDirection, unitType, producedGoodType));
		return this;
	}

	public ColonyBuilder setGood(final GoodType goodType, final Integer amount) {
		Preconditions.checkNotNull(goodType);
		Preconditions.checkNotNull(amount);
		GoodAmount goodAmount = new GoodAmount(goodType, amount);
		Preconditions.checkArgument(!goodAmounts.contains(goodAmount), "Good type (%s) was alredy defined.");
		goodAmounts.add(goodAmount);
		return this;
	}

	String getName() {
		return colonyPo.getName();
	}

	Location getLocation() {
		return colonyPo.getLocation();
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
		private final GoodType producedGoodType;

		FieldPlace(final Location fieldDirection, final UnitType unitType, final GoodType producedGoodType) {
			this.fieldDirection = Preconditions.checkNotNull(fieldDirection);
			this.unitType = Preconditions.checkNotNull(unitType);
			this.producedGoodType = Preconditions.checkNotNull(producedGoodType);
		}

		public Location getFieldDirection() {
			return fieldDirection;
		}

		public UnitType getUnitType() {
			return unitType;
		}

		public GoodType getProducedGoodType() {
			return producedGoodType;
		}

	}
	

	static class GoodAmount {

		private final GoodType goodType;
		private final Integer amount;

		GoodAmount(final GoodType goodType, final Integer amount) {
			this.goodType = Preconditions.checkNotNull(goodType);
			this.amount = Preconditions.checkNotNull(amount);
		}

		public GoodType getGoodType() {
			return goodType;
		}

		public Integer getAmount() {
			return amount;
		}

		@Override
		public int hashCode() {
			return goodType.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof GoodAmount)) {
				return false;
			}
			final GoodAmount other = (GoodAmount) obj;
			return goodType.equals(other.goodType);
		}
		
	}

	List<UnitPlace> getUnitPlaces() {
		return unitPlaces;
	}

	public List<FieldPlace> getFieldPlaces() {
		return fieldPlaces;
	}

	public List<GoodAmount> getGoodAmounts() {
		return goodAmounts;
	}

}
