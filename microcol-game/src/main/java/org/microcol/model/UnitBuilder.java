package org.microcol.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class UnitBuilder {

	private final ModelBuilder modelBuilder;

	private UnitType type;

	private Player player;

	private final PlaceBuilder placeBuilder;

	private final List<Unit> unitsInCargo = Lists.newArrayList();;

	private final List<GoodAmmount> goodAmmounts = Lists.newArrayList();;

	UnitBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		placeBuilder = new PlaceBuilder();
	}

	public UnitBuilder setPlayer(final String playerName) {
		player = modelBuilder.getPlayer(Preconditions.checkNotNull(playerName));
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

	public UnitBuilder addCargoGood(final GoodType goodType, final int ammount) {
		// FIXME JJ dokoncit implementaci
		return this;
	}

	public UnitBuilder addCargoUnit(final UnitType type, final boolean hasHorse, final boolean hasTools,
			final boolean hasMuskets) {
		unitsInCargo.add(modelBuilder.makeUnitBuilder().setLocation(Location.of(6, 4)).setType(type)
				.setPlayer(player.getName()).build());
		return this;
	}

	public Unit build() {
		Preconditions.checkNotNull(modelBuilder);
		Preconditions.checkNotNull(player, "player was not set");
		Unit unit = new Unit(type, player, placeBuilder);
		final AtomicInteger cx = new AtomicInteger(0);
		unitsInCargo.forEach(cargoUnit -> {
			int i = cx.getAndIncrement();
			CargoSlot cargoSlot = unit.getHold().getSlots().get(i);
			// FIXME JJ samotne ulozeni nefunguje, je tam na modelu vyvolana udalost jednotka byla ulozena
			cargoSlot.unsafeStore(cargoUnit);
		});
		return unit;
	}

}
