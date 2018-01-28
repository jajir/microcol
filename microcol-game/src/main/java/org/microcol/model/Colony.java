package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

	private Player owner;

	private final Location location;

	private final List<ColonyField> colonyFields;

	private final List<Construction> constructions;

	private final ColonyWarehouse colonyWarehouse;
	
	private final Model model;

	public Colony(final Model model, final String name, final Player owner, final Location location,
			final Function<Colony, List<Construction>> constructionsBuilder,
			final Map<String, Integer> initialGoodAmounts) {
		this.model = Preconditions.checkNotNull(model);
		this.name = Preconditions.checkNotNull(name);
		this.owner = Preconditions.checkNotNull(owner, "owner is null");
		this.location = Preconditions.checkNotNull(location);
		colonyFields = new ArrayList<>();
		Location.DIRECTIONS.forEach(loc -> colonyFields.add(new ColonyField(model, loc, this)));
		this.constructions = Preconditions.checkNotNull(constructionsBuilder.apply(this));
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
	
	public void captureColony(final Player player, final Unit capturingUnit) {
		//TODO add verification that colony could be captured and there are no military units
		owner = player;
		model.fireColonyWasCaptured(model, capturingUnit, this);
	}
	
	void placeUnitToProduceFood(final Unit unit) {
		final ColonyField field = colonyFields.stream()
				.max((f1, f2) -> f2.isPossibleToProduceOfGooodsType(GoodType.CORN)
						- f1.isPossibleToProduceOfGooodsType(GoodType.CORN))
				.orElseThrow(() -> new IllegalStateException("There is not place to produce food."));
		unit.placeToColonyField(field, GoodType.CORN);
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
	
	public String getName() {
		return name;
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
	
	List<Unit> getUnitsInColony(){
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
	
	public boolean isLastUnitIncolony(final Unit unit){
		Preconditions.checkNotNull(unit);
		final List<Unit> unitsInColony = getUnitsInColony();
		if(unitsInColony.size()>1){
			return false;
		}else if(unitsInColony.size() == 1){
			final Unit u = unitsInColony.get(0);
			return unit.equals(u);
		}else{
			throw new IllegalStateException(
					String.format("Colony have invalid number of units (%s)", unitsInColony.size()));
		}
	}
	
	/**
	 * Verify number of units in colony and when it's 0 than destroy colony. 
	 */
	void verifyNumberOfUnitsOptionallyDestroyColony(){
		if (getUnitsInColony().isEmpty()){
			model.destroyColony(this);
		}
	}
	
	public boolean isValid(){
		return model.isExists(this);
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
	
	//TODO add some tests
	public ColonyProductionStats getGoodsStats() {
		final ColonyProductionStats out = new ColonyProductionStats();
		//set initial warehouse stack
		GoodType.GOOD_TYPES.forEach(goodType -> {
			GoodProductionStats goodsStats = out.getStatsByType(goodType);
			goodsStats.setInWarehouseBefore(colonyWarehouse.getGoodAmmount(goodType));
		});
		
		//get production from all fields
		colonyFields.forEach(field -> {
			if(!field.isEmpty()){
				GoodProductionStats goodsStats = out.getStatsByType(field.getProducedGoodType());
				goodsStats.addRowProduction(field.getProducedGoodsAmmount());
			}
		});
		
		//get production from town factories that doesn't consume any sources
		ConstructionType.SOURCE_1.forEach(goodType -> {
			getConstructionProducing(goodType).ifPresent(con ->{
				GoodProductionStats goodsStats = out.getStatsByType(goodType);
				ConstructionTurnProduction turnProd = con.getProduction(0);
				goodsStats.setRowProduction(turnProd.getProducedGoods());
			});
		});
		
		//get production from town factories that consume some primary sources
		ConstructionType.SOURCE_2.forEach(goodType -> {
			computeSecondaryProduction(out, goodType);
		});
		
		//get production from town factories that consume secondary sources
		ConstructionType.SOURCE_3.forEach(goodType -> {
			computeSecondaryProduction(out, goodType);			
		});
		
		return out;
	}
	
	private void computeSecondaryProduction(final ColonyProductionStats out, final GoodType goodTypeProduced) {
		if (getConstructionProducing(goodTypeProduced).isPresent()) {
			final Construction producedAt = getConstructionProducing(goodTypeProduced).get();
			GoodProductionStats goodProdStats = out.getStatsByType(goodTypeProduced);
			GoodType goodTypeConsumed = producedAt.getType().getConsumed().get();
			GoodProductionStats goodConsumedStats = out.getStatsByType(goodTypeConsumed);

			Preconditions.checkState(goodConsumedStats.getConsumed() == 0,
					"good type was already computed, good was already consumed.");
			int numberOfavailableInputGoods = goodConsumedStats.getInWarehouseAfter();

			ConstructionTurnProduction turnProd = producedAt.getProduction(numberOfavailableInputGoods);
			goodConsumedStats.setConsumed(turnProd.getConsumedGoods());
			goodProdStats.setRowProduction(turnProd.getProducedGoods());
			goodProdStats.setBlockedProduction(turnProd.getBlockedGoods());
		}
	}

	private Optional<Construction> getConstructionProducing(final GoodType goodType) {
		return constructions.stream().filter(construction -> construction.getType().getProduce().isPresent()
				? construction.getType().getProduce().get().equals(goodType) : false).findAny();
	}
}
