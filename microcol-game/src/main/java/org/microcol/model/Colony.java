package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Colony {

	/**
	 * Colony unique name.
	 */
	private String name;

	private final Player owner;

	private final Location location;

	private final List<ColonyField> colonyFields;

	private final List<Construction> constructions;

	private final ColonyWarehouse colonyWarehouse;
	
	private Model model;
	

	public Colony(final String name, final Player owner, final Location location,
			final List<Construction> constructions) {
		this.name = Preconditions.checkNotNull(name);
		this.owner = Preconditions.checkNotNull(owner, "owner is null");
		this.location = Preconditions.checkNotNull(location);
		colonyFields = new ArrayList<>();
		Location.DIRECTIONS.forEach(loc -> colonyFields.add(new ColonyField(loc, this)));
		this.constructions = Preconditions.checkNotNull(constructions);
		colonyWarehouse = new ColonyWarehouse(this);
		checkConstructions();
	}

	/**
	 * Verify that constructions are consistent. It verify:
	 * <ul>
	 * <li>That all construction types are at list just one.</li>
	 * <li>That each good is produces by just one construction.</li>
	 * <li>That no building and it's upgrade are at constructions list.</li>
	 * </ul>
	 */
	private void checkConstructions() {
		Preconditions.checkState(colonyFields.size() == 8,
				String.format("Incorrect colony filed number '%s'", colonyFields.size()));
		Map<ConstructionType, Long> l1 = constructions.stream()
				.collect(Collectors.groupingBy(Construction::getType, Collectors.counting()));
		l1.forEach((constructionType, count) -> {
			if (count > 1) {
				throw new IllegalStateException(
						String.format("Construction type '%s' is duplicated", constructionType.name()));
			}
		});

		final Map<GoodType, Long> l2 = constructions.stream()
				.filter(construction -> construction.getType().getProduce().isPresent()).collect(Collectors
						.groupingBy(construction -> construction.getType().getProduce().get(), Collectors.counting()));
		l2.forEach((goodType, count) -> {
			if (count != 1) {
				throw new IllegalStateException(
						String.format("Good type type '%s' is prodecen in more than one building", goodType.name()));
			}
		});

		constructions.forEach(constructio -> {
			if (constructio.getType().getUpgradeTo().isPresent()) {
				final ConstructionType upgradeTo = constructio.getType().getUpgradeTo().get();
				constructions.stream().filter(cc -> cc.getType().equals(upgradeTo)).findAny().ifPresent(cc -> {
					throw new IllegalStateException(String.format("There is building type '%s' and it's upgrade '%s'",
							constructio.getType(), cc));
				});
			}
		});

	}
	
	/**
	 * Perform operation for next turn.
	 */
	public void startTurn(){
		constructions.forEach(construction -> {
			if (construction.getType().getProduce().isPresent()) {
				colonyWarehouse.putToWarehouse(construction.getType().getProduce().get(),
						construction.getProductionPerTurn());
			}
		});
	}
	
	public List<Unit> getUnitsInPort() {
		return model.getUnitsAt(location).stream().filter(unit -> unit.getType().canHoldCargo())
				.collect(ImmutableList.toImmutableList());
	}
	
	public List<Unit> getUnitsOutSideColony() {
		return model.getUnitsAt(location).stream().filter(unit -> !unit.getType().canHoldCargo())
				.collect(ImmutableList.toImmutableList());
	}
	
	public Construction getConstructionByType(final ConstructionType constructionType) {
		return constructions.stream().filter(construction -> construction.getType().equals(constructionType)).findAny()
				.orElseThrow(() -> new IllegalStateException(
						String.format("No such construction type (%s) in colony (%s)", constructionType, getName())));
	}
	
	ConstructionType getWarehouseType() {
		return constructions.stream().filter(cont -> ConstructionType.WAREHOUSES.contains(cont.getType())).findAny()
				.orElseThrow(() -> new IllegalStateException("Colony doesn't contains any warehouse.")).getType();
	}

	public ColonyField getColonyFieldInDirection(final Location fieldDirection) {
		Preconditions.checkNotNull(fieldDirection, "Field direction is null");
		Preconditions.checkArgument(fieldDirection.isDirection(),
				String.format("Direction (%s) is  not known", fieldDirection));
		return colonyFields.stream().filter(colonyFiled -> colonyFiled.getDirection().equals(fieldDirection)).findAny()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Field directiond (%s) is not in colony (%s)", fieldDirection, this)));
	}
	
	public void setModel(Model model) {
		this.model = model;
		colonyFields.forEach(colonyField -> colonyField.setModel(model));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public Player getOwner() {
		return owner;
	}

	public List<ColonyField> getColonyFields() {
		return colonyFields;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Colony.class).add("name", name).add("location", location).toString();
	}

	public List<Construction> getConstructions() {
		return constructions;
	}

	Model getModel() {
		return model;
	}

	public ColonyWarehouse getColonyWarehouse() {
		return colonyWarehouse;
	}
}
