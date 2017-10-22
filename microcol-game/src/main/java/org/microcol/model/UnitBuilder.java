package org.microcol.model;

import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

public class UnitBuilder {

	private final UnitPo unitPo;
	
	UnitBuilder() {
		unitPo = new UnitPo();
		unitPo.setId(IdManager.nextId());
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
		unitPo.setPlaceMap(new PlaceMapPo());
		unitPo.getPlaceMap().setLocation(location);
		return this;
	}

	public UnitBuilder setShipIncomingToColonies(int inHowManyturns) {
		unitPo.setPlaceHighSeas(new PlaceHighSeasPo());
		unitPo.getPlaceHighSeas().setRemainigTurns(inHowManyturns);
		unitPo.getPlaceHighSeas().setTravelToEurope(false);
		return this;
	}

	public UnitBuilder setShipIncomingToEurope(int inHowManyturns) {
		unitPo.setPlaceHighSeas(new PlaceHighSeasPo());
		unitPo.getPlaceHighSeas().setRemainigTurns(inHowManyturns);
		unitPo.getPlaceHighSeas().setTravelToEurope(true);
		return this;
	}

	public UnitBuilder setUnitToEuropePortPier() {
		unitPo.setPlaceEuropePort(new PlaceEuropePortPo());
		return this;
	}

	public UnitBuilder setUnitToCargoSlot(final Unit cargoHolder) {
		//FIXME JJ NYI
		return this;
	}

	public UnitBuilder setUnitToConstruction(final ConstructionType constructionType, final Colony colony,
			final int position) {
		//FIXME JJ NYI
		return this;
	}

	public UnitBuilder setUnitToFiled(final Location fieldDirection, final Colony colony,
			final GoodType producedGoodType) {
		//FIXME JJ NYI
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
