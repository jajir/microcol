package org.microcol.model;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Helps build colony.
 */
public class ColonyBuilder {

	private final ColonyPo colonyPo;

	private final PlayerBuilder playerBuilder;

	public ColonyBuilder(final String name, final PlayerBuilder playerBuilder) {
		this.playerBuilder = Preconditions.checkNotNull(playerBuilder);
		this.colonyPo = new ColonyPo();
		colonyPo.setName(Preconditions.checkNotNull(name));
		colonyPo.setOwnerName(Preconditions.checkNotNull(playerBuilder.getPlayerPo().getName()));
	}

	public PlayerBuilder build() {
		playerBuilder.getModelPo().getColonies().add(colonyPo);
		return playerBuilder;
	}

	public ColonyBuilder setLocation(final Location location) {
		colonyPo.setLocation(location);
		return this;
	}

	public ColonyBuilder setDefaultConstructions() {
		ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(constructionType -> {
			final ConstructionPo constructionPo = new ConstructionPo();
			constructionPo.setType(constructionType);
			colonyPo.getConstructions().add(constructionPo);
		});
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
		final ConstructionPo constructionPo = new ConstructionPo();
		constructionPo.setType(constructionType);
		colonyPo.getConstructions().add(constructionPo);
		return this;
	}

	private UnitPo createUnitByType(final UnitType unitType) {
		final UnitPo worker = new UnitPo();
		worker.setId(IdManager.nextId());
		worker.setOwnerId(playerBuilder.getPlayerPo().getName());
		worker.setType(unitType);
		playerBuilder.getModelPo().getUnits().add(worker);
		return worker;
	}

	public ColonyBuilder setWorker(final ConstructionType constructionType, final int position,
			final UnitType unitType) {
		Preconditions.checkNotNull(constructionType);
		Preconditions.checkNotNull(unitType);
		Preconditions.checkArgument(position >= 0 && position < 3, "Position is not within range 0,1,2.");
		final ConstructionPo constructionPo = colonyPo.getConstructionByType(constructionType);

		final UnitPo worker = createUnitByType(unitType);
		final PlaceConstructionSlotPo placeConstructionSlotPo = new PlaceConstructionSlotPo();
		worker.setPlaceConstructionSlot(placeConstructionSlotPo);

		constructionPo.getSlots().get(position).setWorkerId(worker.getId());
		return this;
	}

	public ColonyBuilder setWorker(final Location fieldDirection, final UnitType unitType,
			final GoodType producedGoodType) {
		Preconditions.checkNotNull(fieldDirection);
		Preconditions.checkNotNull(unitType);
		
		final UnitPo worker = createUnitByType(unitType);
		final PlaceColonyFieldPo placeColonyFieldPo = new PlaceColonyFieldPo();
		worker.setPlaceColonyField(placeColonyFieldPo);
		
		colonyPo.getFieldByDirection(fieldDirection).setWorkerId(worker.getId());
		colonyPo.getFieldByDirection(fieldDirection).setProducedGoodType(producedGoodType);
		return this;
	}

	public ColonyBuilder setGood(final GoodType goodType, final Integer amount) {
		Preconditions.checkNotNull(goodType);
		Preconditions.checkNotNull(amount);
		Preconditions.checkArgument(!colonyPo.getColonyWarehouse().containsKey(goodType),
				"Good type (%s) was alredy defined.");
		colonyPo.getColonyWarehouse().put(goodType.name(), amount);
		return this;
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

}
