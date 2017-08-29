package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Allow to easily create player.
 */
public class PlayerBuilder {

	private final ModelBuilder modelBuilder;

	private final String name;

	private int gold;

	private boolean isComputerPlayer;

	private final List<TownBuilder> townBuilders = new ArrayList<>();

	PlayerBuilder(final ModelBuilder modelBuilder, final String name) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		this.name = Preconditions.checkNotNull(name);
	}

	public ModelBuilder make() {
		final Player player = new Player(name, isComputerPlayer, gold);
		modelBuilder.getPlayers().add(player);
		townBuilders.forEach(townBuilder -> {
			final List<Construction> constructions = Lists.newArrayList();
			if (townBuilder.isDefaultCostructions()) {
				ConstructionType.NEW_TOWN_CONSTRUCTIONS.forEach(constructionType -> {
					final Construction c = new Construction(constructionType);
					constructions.add(c);
				});
			}
			townBuilder.getConstructionTypes().forEach(constructionType -> {
				final Construction c = new Construction(constructionType);
				constructions.add(c);
			});
			final Town town = new Town(townBuilder.getName(), player, townBuilder.getLocation(), constructions);
			townBuilder.getUnitPlaces().forEach(unitPlace -> {
				final UnitBuilder unitBuilder = modelBuilder.makeUnitBuilder();
				unitBuilder.setPlayer(player);
				unitBuilder.setType(unitPlace.getUnitType());
				unitBuilder.setUnitToConstruction(unitPlace.getConstructionType(), town);
				final Unit unit = unitBuilder.build();
				final Construction construction = town.getConstructionByType(unitPlace.getConstructionType());
				construction.place(unitPlace.getPosition(), unit.getPlaceConstruction());
				modelBuilder.addUnit(unit);
			});
			modelBuilder.getTowns().add(town);
		});
		return modelBuilder;
	}

	public PlayerBuilder setGold(int gold) {
		this.gold = gold;
		return this;
	}

	public PlayerBuilder setComputerPlayer(boolean isComputerPlayer) {
		this.isComputerPlayer = isComputerPlayer;
		return this;
	}

	public TownBuilder addTown(final String name) {
		final TownBuilder townBuilder = new TownBuilder(name, this);
		townBuilders.add(townBuilder);
		return townBuilder;
	}

}
