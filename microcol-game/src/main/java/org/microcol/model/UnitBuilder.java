package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class UnitBuilder {

	private final ModelBuilder modelBuilder;

	private UnitType type;

	private Player player;

	private final PlaceBuilder placeBuilder;

	private final List<UnitBuilder> unitsInCargo = Lists.newArrayList();

	private final List<GoodAmount> goodAmounts = Lists.newArrayList();

	UnitBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		placeBuilder = new PlaceBuilder();
	}

	public UnitBuilder setPlayerName(final String playerName) {
		player = modelBuilder.getPlayer(Preconditions.checkNotNull(playerName));
		return this;
	}

	public UnitBuilder setPlayer(final Player player) {
		this.player = Preconditions.checkNotNull(player);
		return this;
	}

	public UnitBuilder setType(final UnitType type) {
		Preconditions.checkNotNull(type, "Unit type is empty");
		this.type = type;
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

	public UnitBuilder setUnitToConstruction(final ConstructionType constructionType, final Colony colony) {
		placeBuilder.setToCostruction(constructionType, colony);
		return this;
	}
	
	public UnitBuilder setUnitToFiled(final Location fieldDirection, final Colony colony){
		placeBuilder.setUnitToFiled(fieldDirection, colony);
		return this;		
	}

	public UnitBuilder addCargoGood(final GoodType goodType, final int amount) {
		// FIXME JJ dokoncit implementaci
		return this;
	}

	public UnitBuilder addCargoUnit(final UnitType type, final boolean hasHorse, final boolean hasTools,
			final boolean hasMuskets) {
		unitsInCargo.add(modelBuilder.makeUnitBuilder().setType(type).setPlayerName(player.getName()));
		return this;
	}

	public Unit build() {
		Preconditions.checkNotNull(modelBuilder);
		Preconditions.checkNotNull(player, "player was not set");
		Unit unit = new Unit(type, player, placeBuilder);
		unitsInCargo.forEach(cargoUnitBuilder -> {
			cargoUnitBuilder.setUnitToCargoSlot(unit);
			final Unit cargoUnit = cargoUnitBuilder.build();
			modelBuilder.addUnit(cargoUnit);
		});
		return unit;
	}

}
