package org.microcol.model;

import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

public class UnitBuilder {

	private final UnitPo unitPo;

	private final PlaceBuilderImpl placeBuilder;

	UnitBuilder() {
		unitPo = new UnitPo();
		unitPo.setId(IdManager.nextId());
		placeBuilder = new PlaceBuilderImpl();
	}

	public UnitBuilder setPlayerName(final String playerName) {
		unitPo.setOwnerId(playerName);
		return this;
	}

	@Deprecated
	public UnitBuilder setPlayer(final Player player) {
		unitPo.setOwnerId(player.getName());
		return this;
	}

	public UnitBuilder setType(final UnitType type) {
		Preconditions.checkNotNull(type, "Unit type is empty");
		unitPo.setType(type);
		return this;
	}

	public UnitBuilder setLocation(final Location location) {
		placeBuilder.setLocation(location);
		return this;
	}

	public UnitBuilder setShipIncomingToColonies(int inHowManyturns) {
		placeBuilder.setShipIncomingToColonies(inHowManyturns);
		return this;
	}

	public UnitBuilder setShipIncomingToEurope(int inHowManyturns) {
		placeBuilder.setShipIncomingToEurope(inHowManyturns);
		return this;
	}

	public UnitBuilder setUnitToEuropePortPier() {
		placeBuilder.setUnitToEuropePortPier();
		return this;
	}

	public UnitBuilder setUnitToCargoSlot(final Unit cargoHolder) {
		placeBuilder.setToCargoSlot(cargoHolder);
		return this;
	}

	public UnitBuilder setUnitToConstruction(final ConstructionType constructionType, final Colony colony,
			final int position) {
		placeBuilder.setToCostruction(constructionType, colony, position);
		return this;
	}

	public UnitBuilder setUnitToFiled(final Location fieldDirection, final Colony colony,
			final GoodType producedGoodType) {
		placeBuilder.setUnitToFiled(fieldDirection, colony, producedGoodType);
		return this;
	}

	public UnitBuilder addCargoGood(final GoodType goodType, final int amount) {
		final CargoSlotPo cargoSlotPo = new CargoSlotPo();
		cargoSlotPo.setGoodType(goodType);
		cargoSlotPo.setAmount(amount);
		unitPo.getCargo().getSlots().add(cargoSlotPo);
		return this;
	}

	public UnitBuilder addCargoUnit(final UnitType type, final boolean hasHorse, final boolean hasTools,
			final boolean hasMuskets) {
		final CargoSlotPo cargoSlotPo = new CargoSlotPo();
		//TODO JJ create unit with ID and put it to cargo
//		unitPo.getCargo().
		return this;
	}

	public UnitPo build() {
		return unitPo;
	}

}
