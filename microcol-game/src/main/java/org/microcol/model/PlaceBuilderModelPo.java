package org.microcol.model;

import java.util.List;
import java.util.Optional;

import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ConstructionSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class PlaceBuilderModelPo implements PlaceBuilder {

	private final List<Builder> placeBuilders = Lists.newArrayList((unit, unitPo, modelPo, model) -> {
		/**
		 * Map
		 */
		if (unitPo.getPlaceMap() != null) {
			return new PlaceLocation(unit, unitPo.getPlaceMap().getLocation());
		}
		return null;
	}, (unit, unitPo, modelPo, model) -> {
		/**
		 * High seas
		 */
		if (unitPo.getPlaceHighSeas() != null) {
			return new PlaceHighSea(unit, unitPo.getPlaceHighSeas().isTravelToEurope(),
					unitPo.getPlaceHighSeas().getRemainigTurns());
		}
		return null;
	}, (unit, unitPo, modelPo, model) -> {
		/**
		 * Europe port
		 */
		if (unitPo.getPlaceEuropePort() != null) {
			return new PlaceEuropePort(unit, model.getEurope().getPort());
		}
		return null;
	}, (unit, unitPo, modelPo, model) -> {
		/**
		 * Colony field
		 */
		if (unitPo.getPlaceColonyFieldPo() != null) {
			for (final ColonyPo colonyPo : modelPo.getColonies()) {
				final PlaceColonyField out = tryToFindColonyField(colonyPo, unit, model);
				if (out != null) {
					return out;
				}
			}
			throw new IllegalArgumentException(
					String.format("It's not possible to define place unit (%s) to colony field", unitPo));
			
		}
		return null;
	}, (unit, unitPo, modelPo, model) -> {
		/**
		 * Colony construction
		 */
		if (unitPo.getPlaceConstructionSlotPo() != null) {
			for (final ColonyPo colonyPo : modelPo.getColonies()) {
				final PlaceConstructionSlot out = tryToFindConstructionSlot(colonyPo, unit, model);
				if (out != null) {
					return out;
				}
			}
			throw new IllegalArgumentException(
					String.format("It's not possible to define place unit (%s) to construction slot", unitPo));
		}
		return null;
	}, (unit, unitPo, modelPo, model) -> {
		/**
		 * Unit's cargo
		 */
		// TODO NYI
		return null;
	});

	private final UnitPo unitPo;
	private final ModelPo modelPo;
	private final Model model;

	PlaceBuilderModelPo(final UnitPo unitPo, final ModelPo modelPo, final Model model) {
		this.unitPo = Preconditions.checkNotNull(unitPo);
		this.modelPo = Preconditions.checkNotNull(modelPo);
		this.model = Preconditions.checkNotNull(model);
	}

	@Override
	public Place build(final Unit unit) {
		for (final Builder placeBuilder : placeBuilders) {
			final Place place = placeBuilder.tryBuild(unit, unitPo, modelPo, model);
			if (place != null) {
				return place;
			}
		}
		throw new IllegalArgumentException(String.format("It's not possible to define place for unit (%s)", unitPo));
	}

	interface Builder {

		Place tryBuild(final Unit unit, final UnitPo unitPo, final ModelPo modelPo, final Model model);

	}

	private PlaceConstructionSlot tryToFindConstructionSlot(final ColonyPo colonyPo, final Unit unit, final Model model) {
		for (final ConstructionPo constructionPo : colonyPo.getConstructions()) {
			int slotId = 0;
			for (final ConstructionSlotPo slotPo : constructionPo.getSlots()) {
				if (slotPo.getWorkerId() != null && unit.getId() == slotPo.getWorkerId()) {
					final Optional<Colony> oColony = model.getColoniesAt(colonyPo.getLocation());
					Preconditions.checkState(oColony.isPresent(), "Colony at (%s) is not in model",
							colonyPo.getLocation());
					final Colony colony = oColony.get();
					final Construction construction = colony.getConstructionByType(constructionPo.getType());
					final PlaceConstructionSlot out = new PlaceConstructionSlot(unit, construction.getSlotAt(slotId));
					construction.getSlotAt(slotId).set(out);
					return out;
				}
				slotId++;
			}
		}
		return null;
	}

	private PlaceColonyField tryToFindColonyField(final ColonyPo colonyPo, final Unit unit, final Model model) {
		for (final ColonyFieldPo constructionPo : colonyPo.getColonyFields()) {
			if (constructionPo.getWorkerId() != null && unit.getId() == constructionPo.getWorkerId()) {
				final Optional<Colony> oColony = model.getColoniesAt(colonyPo.getLocation());
				Preconditions.checkState(oColony.isPresent(), "Colony at (%s) is not in model", colonyPo.getLocation());
				final Colony colony = oColony.get();
				final ColonyField colonyField = colony.getColonyFieldInDirection(constructionPo.getDirection());
				PlaceColonyField out = new PlaceColonyField(unit, colonyField, constructionPo.getProducedGoodType());
				colonyField.setPlaceColonyField(out);
				return out;
			}
		}
		return null;
	}

	class BuilderLocation implements Builder {

		@Override
		public Place tryBuild(final Unit unit, final UnitPo unitPo, final ModelPo modelPo, final Model model) {
			if (unitPo.getPlaceMap() != null) {
				return new PlaceLocation(unit, unitPo.getPlaceMap().getLocation());
			}
			return null;
		}

	}

}
