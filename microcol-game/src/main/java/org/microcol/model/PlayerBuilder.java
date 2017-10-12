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

	private final List<ColonyBuilder> colonyBuilders = new ArrayList<>();

	PlayerBuilder(final ModelBuilder modelBuilder, final String name) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		this.name = Preconditions.checkNotNull(name);
	}

	public ModelBuilder build() {
		final Player player = new Player(name, isComputerPlayer, gold);
		modelBuilder.getPlayers().add(player);
		colonyBuilders.forEach(colonyBuilder -> {
			final List<Construction> constructions = Lists.newArrayList();
			if (colonyBuilder.isDefaultCostructions()) {
				ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(constructionType -> {
					final Construction c = Construction.build(constructionType);
					constructions.add(c);
				});
			}
			colonyBuilder.getConstructionTypes().forEach(constructionType -> {
				final Construction c = Construction.build(constructionType);
				constructions.add(c);
			});
			final Colony colony = new Colony(colonyBuilder.getName(), player, colonyBuilder.getLocation(), constructions);
			colonyBuilder.getUnitPlaces().forEach(unitPlace -> {
				final UnitBuilder unitBuilder = modelBuilder.makeUnitBuilder();
				unitBuilder.setPlayer(player);
				unitBuilder.setType(unitPlace.getUnitType());
				unitBuilder.setUnitToConstruction(unitPlace.getConstructionType(), colony, unitPlace.getPosition());
				final Unit unit = unitBuilder.build();
				final Construction construction = colony.getConstructionByType(unitPlace.getConstructionType());
				construction.place(unitPlace.getPosition(), unit.getPlaceConstruction());
				modelBuilder.addUnit(unit);
			});
			colonyBuilder.getFieldPlaces().forEach(fieldPlace->{
				final UnitBuilder unitBuilder = modelBuilder.makeUnitBuilder();
				unitBuilder.setPlayer(player);
				unitBuilder.setType(fieldPlace.getUnitType());
				unitBuilder.setUnitToFiled(fieldPlace.getFieldDirection(), colony, fieldPlace.getProducedGoodType());
				final Unit unit = unitBuilder.build();
				colony.getColonyFieldInDirection(fieldPlace.getFieldDirection())
						.setPlaceColonyField(unit.getPlaceColonyField());
				modelBuilder.addUnit(unit);
			});
			colonyBuilder.getGoodAmounts().forEach(goodAmount -> {
				colony.getColonyWarehouse().addToWarehouse(goodAmount.getGoodType(), goodAmount.getAmount());
			});
			modelBuilder.getColonies().add(colony);
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

	public ColonyBuilder addColony(final String name) {
		final ColonyBuilder colonyBuilder = new ColonyBuilder(name, this);
		colonyBuilders.add(colonyBuilder);
		return colonyBuilder;
	}

}
