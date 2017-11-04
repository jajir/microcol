package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;

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
		this(name, owner, location, constructions, new HashMap<>());
	}

	public Colony(final String name, final Player owner, final Location location,
			final List<Construction> constructions, final Map<String, Integer> initialGoodAmounts) {
		this.name = Preconditions.checkNotNull(name);
		this.owner = Preconditions.checkNotNull(owner, "owner is null");
		this.location = Preconditions.checkNotNull(location);
		colonyFields = new ArrayList<>();
		Location.DIRECTIONS.forEach(loc -> colonyFields.add(new ColonyField(loc, this)));
		this.constructions = Preconditions.checkNotNull(constructions);
		colonyWarehouse = new ColonyWarehouse(this, initialGoodAmounts);
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
	
	ColonyPo save() {
		ColonyPo out = new ColonyPo();
		out.setName(name);
		out.setOwnerName(owner.getName());
		out.setLocation(location);
		out.setColonyFields(saveColonyFields());
		out.setColonyWarehouse(colonyWarehouse.save());
		out.setConstructions(saveCostructions());
		return out;
	}
	
	 private List<ColonyFieldPo> saveColonyFields(){
		 final List<ColonyFieldPo> out = new ArrayList<>();
		 colonyFields.forEach(field -> out.add(field.save()));
		 return out;
	 }
	 
	 private List<ConstructionPo> saveCostructions(){
		 final List<ConstructionPo> out = new ArrayList<>();
		 constructions.forEach(construction -> out.add(construction.save()));
		 return out;
	 }
	
	/**
	 * Perform operation for next turn. Producing order:
	 * <ul>
	 * <li>Outside colony produce</li>
	 * <li>Inside colony</li>
	 * </ul>
	 */
	public void startTurn(){
		colonyFields.forEach(field -> field.produce(colonyWarehouse));
		constructions.forEach(construction -> construction.produce(this, getColonyWarehouse()));
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
	
	public boolean isContainsConstructionByType(final ConstructionType constructionType) {
		return constructions.stream().filter(construction -> construction.getType().equals(constructionType)).findAny()
				.isPresent();
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

	public ColonyWarehouse getColonyWarehouse() {
		return colonyWarehouse;
	}
	
	/**
	 * Return contains of warehouse after fields produce it's goods in next
	 * turn.
	 * 
	 * @return return colony warehouse
	 */
	public ColonyWarehouse getNexTurnTempWarehouse(){
		ColonyWarehouse out = colonyWarehouse.clone();
		colonyFields.forEach(field -> field.produce(out));
		return out;
	}
	
	private List<Unit> getUnitsInColony(){
		final List<Unit> out = new ArrayList<>();
		constructions.forEach(construction->{
			construction.getConstructionSlots().forEach(slot->{
				if (!slot.isEmpty()) {
					out.add(slot.getUnit());
				}
			});
		});
		colonyFields.forEach(field->{
			if (!field.isEmpty()) {
				out.add(field.getUnit());
			}
		});
		return ImmutableList.copyOf(out);
	}
	
	//TODO move statistic read-only method to statistics class.
	public int getMilitaryForce(){
		int force = 0;
		// count units outside colony
		for (final Unit unit : model.getUnitsAt(owner, location)) {
			force += unit.getMilitaryStrenght();
		}
		// count units in colony
		for (final Unit unit : getUnitsInColony()) {
			force += unit.getMilitaryStrenght();
		}
		return force;
	}
	
}
