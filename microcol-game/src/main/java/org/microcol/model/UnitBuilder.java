package org.microcol.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class UnitBuilder {

	private final ModelBuilder modelBuilder;

	private UnitType type;

	private Player player;

	private Location location;

	private final List<Unit> unitsInCargo = Lists.newArrayList();;

	private final List<GoodAmmount> goodAmmounts = Lists.newArrayList();;

	UnitBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
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
		Preconditions.checkNotNull(location);
		this.location = location;
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
		Preconditions.checkNotNull(location, "location was not set");
		Unit unit = new Unit(type, player, location);
		final AtomicInteger cx = new AtomicInteger(0);
		unitsInCargo.forEach(cargoUnit -> {
			int i = cx.getAndIncrement();
			CargoSlot cargoSlot = unit.getHold().getSlots().get(i);
			//FIXME JJ samotne ulozeni nefunguje, je tam na modelu vyvolana udalost jednotka byla ulozena
//			cargoSlot.store(cargoUnit);
		});
		modelBuilder.addUnit(unit);
		return unit;
	}

}
