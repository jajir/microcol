package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.Ship;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Colony {

    /**
     * When this level is reached than new colonist appears.
     */
    private final static int FOOD_LEVEL_TO_FREE_COLONIST = 200;

    /**
     * Colony unique name.
     */
    private String name;

    private Player owner;

    private final Location location;

    private final List<ColonyField> colonyFields;

    private final List<Construction> constructions;

    private final ColonyWarehouse warehouse;

    private final ColonyBuildingQueue colonyBuildingQueue;

    private final Model model;

    private final Random random = new Random();

    public Colony(final Model model, final String name, final Player owner, final Location location,
            final Function<Colony, List<Construction>> constructionsBuilder,
            final Map<String, Integer> initialGoods,
            final List<ColonyBuildingItemProgress<?>> buildingQueue) {
        this.model = Preconditions.checkNotNull(model);
        this.name = Preconditions.checkNotNull(name);
        this.owner = Preconditions.checkNotNull(owner, "owner is null");
        this.location = Preconditions.checkNotNull(location);
        colonyFields = new ArrayList<>();
        Direction.getAll().forEach(loc -> colonyFields.add(new ColonyField(model, loc, this)));
        this.constructions = Preconditions.checkNotNull(constructionsBuilder.apply(this));
        warehouse = new ColonyWarehouse(this, initialGoods);
        colonyBuildingQueue = new ColonyBuildingQueue(model, this, buildingQueue);
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
                throw new IllegalStateException(String
                        .format("Construction type '%s' is duplicated", constructionType.name()));
            }
        });

        final Map<GoodsType, Long> l2 = constructions.stream()
                .filter(construction -> construction.getType().getProductionPerTurn().isPresent())
                .collect(
                        Collectors
                                .groupingBy(
                                        construction -> construction.getType()
                                                .getProductionPerTurn().get().getType(),
                                        Collectors.counting()));
        l2.forEach((goodsType, count) -> {
            if (count != 1) {
                throw new IllegalStateException(
                        String.format("Good type type '%s' is prodecen in more than one building",
                                goodsType.name()));
            }
        });

        constructions.forEach(constructio -> {
            if (constructio.getType().getUpgradeTo().isPresent()) {
                final ConstructionType upgradeTo = constructio.getType().getUpgradeTo().get();
                constructions.stream().filter(cc -> cc.getType().equals(upgradeTo)).findAny()
                        .ifPresent(cc -> {
                            throw new IllegalStateException(String.format(
                                    "There is building type '%s' and it's upgrade '%s'",
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
        out.setColonyWarehouse(warehouse.save());
        out.setConstructions(saveCostructions());
        out.setBuildingQueue(colonyBuildingQueue.save());
        return out;
    }

    public void captureColony(final Unit capturingUnit) {
        final Player player = capturingUnit.getOwner();
        model.getEnemyUnitsAt(player, location).stream()
                .forEach(unit -> Preconditions.checkState(
                        !unit.getType().canAttack() || unit.getType().isShip(),
                        "At least one unit '%s' at captured city still could fight", unit));
        model.getEnemyUnitsAt(player, location).stream().filter(unit -> unit.getType().isShip())
                .forEach(unit -> escapeOneStep(unit));
        model.getEnemyUnitsAt(player, location).stream().filter(unit -> !unit.getType().isShip())
                .forEach(unit -> unit.takeOver(player));
        colonyFields.stream().filter(field -> !field.isEmpty())
                .forEach(field -> field.getUnit().get().takeOver(player));
        constructions.forEach(construction -> construction.getConstructionSlots().stream()
                .filter(slot -> !slot.isEmpty()).forEach(slot -> slot.getUnit().takeOver(player)));

        placeUnitToProduceFood(capturingUnit);

        if (owner.isHuman()) {
            model.getTurnEventStore().add(TurnEventProvider.getColonyWasLost(owner, this));
        }

        owner = player;
        model.fireColonyWasCaptured(model, capturingUnit, this);
    }

    /**
     * Unit escape from occupied place. When colony is captured naval unit
     * escape from occupied colony.
     * 
     * @param unit
     *            required escaping unit
     */
    private void escapeOneStep(final Unit unit) {
        final List<Location> whereCouldMove = unit.getLocation().getNeighbors().stream().filter(
                loc -> unit.getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(loc)))
                .filter(loc -> model.getEnemyUnitsAt(unit.getOwner(), loc).isEmpty())
                .collect(ImmutableList.toImmutableList());
        final Location target = whereCouldMove.get(random.nextInt(whereCouldMove.size()));
        final Location oldLocation = unit.getLocation();
        model.fireUnitMovedStepStarted(unit, oldLocation, target,
                unit.getPlaceLocation().getOrientation());
        unit.placeToLocation(target);
        model.fireUnitMovedStepFinished(unit, oldLocation, target);
    }

    /**
     * Try to place unit to produce food. If there is no free field than place
     * unit outside colony.
     *
     * @param unit
     *            required unit
     */
    void placeUnitToProduceFood(final Unit unit) {
        final List<ColonyField> fields = getEmptyFieldsWithMaxCornProduction();
        Preconditions.checkState(!fields.isEmpty(), "There are no empty field in colony");
        final ColonyField field = fields.get(random.nextInt(fields.size()));
        unit.placeToColonyField(field, GoodsType.CORN);
    }

    /**
     * Get list of empty fields with maximal corn production. At all returned
     * field could be place unit and start producing food.
     *
     * @return list of colony fields
     */
    List<ColonyField> getEmptyFieldsWithMaxCornProduction() {
        final Integer max = colonyFields.stream().filter(f -> f.isEmpty())
                .map(f -> f.getGoodsTypeProduction(GoodsType.CORN)).max(Comparator.naturalOrder())
                .orElse(-1);
        return colonyFields.stream()
                .filter(f -> f.isEmpty() && f.getGoodsTypeProduction(GoodsType.CORN) == max)
                .collect(ImmutableList.toImmutableList());
    }

    private List<ColonyFieldPo> saveColonyFields() {
        final List<ColonyFieldPo> out = new ArrayList<>();
        colonyFields.forEach(field -> out.add(field.save()));
        return out;
    }

    private List<ConstructionPo> saveCostructions() {
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
    public void startTurn() {
        eatFoodAndOptionallyMakeNewColonist();
        // from now colony could be destroyed by famine.
        if (isValid()) {
            final ColonyProductionStats colonyStats = getGoodsStats();
            colonyStats.forEach((goodsType, stats) -> {
                if (GoodsType.CORN.equals(goodsType)) {
                    if (stats.getInWarehouseAfter() < 0) {
                        model.addTurnEvent(
                                TurnEventProvider.getFamineWillPlagueColony(owner, this));
                    }
                    return;
                }
                if (stats.getDiff() > 0) {
                    warehouse.addGoodsWithThrowingAway(Goods.of(goodsType, stats.getDiff()),
                            thrownAwayGoods -> {
                                model.addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(owner,
                                        thrownAwayGoods, this));
                            });

                } else {
                    warehouse.removeGoods(Goods.of(goodsType, -stats.getDiff()));
                }
            });
            colonyBuildingQueue.startTurn();
        }
    }

    private void eatFoodAndOptionallyMakeNewColonist() {
        final ColonyProductionStats stats = getGoodsStats();
        final GoodsProductionStats cornStats = stats.getStatsByType(GoodsType.CORN);
        if (cornStats.getInWarehouseAfter() < 0) {
            killOneRandomUnit();
            warehouse.setGoods(Goods.of(GoodsType.CORN, 0));
        } else {
            if (cornStats.getInWarehouseAfter() >= FOOD_LEVEL_TO_FREE_COLONIST) {
                produceNewColonist();
                warehouse.setGoods(Goods.of(GoodsType.CORN,
                        cornStats.getInWarehouseAfter() - FOOD_LEVEL_TO_FREE_COLONIST));
            } else {
                if (cornStats.getDiff() < 0) {
                    warehouse.removeGoods(Goods.of(GoodsType.CORN, -cornStats.getDiff()));
                } else {
                    warehouse.addGoodsWithThrowingAway(
                            Goods.of(GoodsType.CORN, cornStats.getDiff()), thrownAwayGoods -> {
                                model.addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(owner,
                                        thrownAwayGoods, this));
                            });
                }
            }
        }
    }

    /**
     * New colonist will appears outside of colony.
     */
    private void produceNewColonist() {
        final Unit newUnit = model.addUnitOutSideColony(this);
        model.getTurnEventStore().add(TurnEventProvider.getNewUnitInColony(getOwner(), this));
        model.fireNewUnitWasBorn(newUnit);
    }

    private void killOneRandomUnit() {
        int index = random.nextInt(getUnitsInsideColony().size());
        final Unit u = getUnitsInsideColony().get(index);
        u.getPlace().destroy();
        model.destroyUnit(u);
        model.addTurnEvent(TurnEventProvider.getFaminePlagueColony(getOwner(), this));
    }

    public Integer getRequiredFoodPerTurn() {
        return getUnitsInsideColony().stream().mapToInt(unit -> unit.getType().getAteFoodPerTurn())
                .sum();
    }

    public List<Ship> getUnitsInPort() {
        return model.getUnitsAt(location).stream().filter(unit -> unit.getType().isShip())
                .map(unit -> (Ship) unit).collect(ImmutableList.toImmutableList());
    }

    public List<Unit> getUnitsOutSideColony() {
        return model.getUnitsAt(location).stream().filter(unit -> !unit.getType().canHoldCargo())
                .collect(ImmutableList.toImmutableList());
    }

    public Construction getConstructionByType(final ConstructionType constructionType) {
        return constructions.stream()
                .filter(construction -> construction.getType().equals(constructionType)).findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No such construction type (%s) in colony (%s)",
                                constructionType, getName())));
    }

    public boolean isContainsConstructionByType(final ConstructionType constructionType) {
        return constructions.stream().map(construction -> construction.getType())
                .filter(type -> type.getUpgradeFromChain().contains(constructionType)).findAny()
                .isPresent();
    }

    /**
     * Investigate in construction type is builded or was builded and
     * construction was upgraded.
     *
     * @param constructionType
     *            required construction type
     * @return Return <code>true</code> when construction type is builded or was
     *         builded and was upgraded otherwise return <code>false</code>.
     */
    public boolean wasConstructionBuilded(final ConstructionType constructionType) {
        return constructions.stream()
                .filter(construction -> construction.getType().equals(constructionType)).findAny()
                .isPresent();
    }

    ConstructionType getWarehouseType() {
        return constructions.stream()
                .filter(cont -> ConstructionType.WAREHOUSES.contains(cont.getType())).findAny()
                .orElseThrow(
                        () -> new IllegalStateException("Colony doesn't contains any warehouse."))
                .getType();
    }

    /**
     * Get colony in direction. All fields are identified by direction in which
     * it lies.
     *
     * @param fieldDirection
     *            required filed direction
     * @return return colony field
     */
    public ColonyField getColonyFieldInDirection(final Direction fieldDirection) {
        Preconditions.checkNotNull(fieldDirection, "Field direction is null");
        return colonyFields.stream()
                .filter(colonyFiled -> colonyFiled.getDirection().equals(fieldDirection)).findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Field directiond (%s) is not in colony (%s)", fieldDirection, this)));
    }

    void createConstruction(final ConstructionType constructionType) {
        if (constructionType.getUpgradeFrom().isPresent()) {
            final ConstructionType alreadyHave = constructionType.getUpgradeFrom().get();
            getConstructionByType(alreadyHave).upgrade();
        } else {
            constructions.add(Construction.build(model, this, constructionType));
        }
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
        return MoreObjects.toStringHelper(Colony.class).add("name", name).add("location", location)
                .toString();
    }

    public List<Construction> getConstructions() {
        return constructions;
    }

    public ColonyWarehouse getWarehouse() {
        return warehouse;
    }

    List<Unit> getUnitsInsideColony() {
        final List<Unit> out = new ArrayList<>();
        constructions.forEach(construction -> {
            construction.getConstructionSlots().forEach(slot -> {
                if (!slot.isEmpty()) {
                    out.add(slot.getUnit());
                }
            });
        });
        colonyFields.forEach(field -> {
            if (field.getUnit().isPresent()) {
                out.add(field.getUnit().get());
            }
        });
        return ImmutableList.copyOf(out);
    }

    public boolean isLastUnitIncolony(final Unit unit) {
        Preconditions.checkNotNull(unit);
        final List<Unit> unitsInColony = getUnitsInsideColony();
        if (unitsInColony.size() > 1) {
            return false;
        } else if (unitsInColony.size() == 1) {
            final Unit u = unitsInColony.get(0);
            return unit.equals(u);
        } else {
            throw new IllegalStateException(String
                    .format("Colony have invalid number of units (%s)", unitsInColony.size()));
        }
    }

    /**
     * Verify number of units in colony and when it's 0 than destroy colony.
     */
    void verifyNumberOfUnitsOptionallyDestroyColony() {
        if (getUnitsInsideColony().isEmpty()) {
            model.destroyColony(this);
        }
    }

    public boolean isValid() {
        return model.isExists(this);
    }

    /**
     * Compute military strength of colony. Method just sum up strength of all
     * units in colony.
     * 
     * @return Return military power of colony
     */
    public int getMilitaryForce() {
        double force = 0;
        // count units outside colony
        for (final Unit unit : model.getUnitsAt(owner, location)) {
            force += unit.getMilitaryStrenght();
        }
        // count units in colony
        for (final Unit unit : getUnitsInsideColony()) {
            force += unit.getMilitaryStrenght();
        }
        return (int) force;
    }

    /**
     * Get production statistics per turn.
     *
     * @return Return colony production statistics per turn.
     */
    public ColonyProductionStats getGoodsStats() {
        final ColonyProductionStats out = new ColonyProductionStats();
        // set initial warehouse stack
        GoodsType.GOOD_TYPES.forEach(goodsType -> {
            GoodsProductionStats goodsStats = out.getStatsByType(goodsType);
            goodsStats.setInWarehouseBefore(warehouse.getGoods(goodsType).getAmount());
        });

        // get production from all fields
        colonyFields.forEach(field -> {
            if (field.getProduction().isPresent()) {
                final Goods production = field.getProduction().get();
                final GoodsProductionStats goodsStats = out.getStatsByType(production.getType());
                goodsStats.addRowProduction(production.getAmount());
            }
        });

        out.getStatsByType(GoodsType.CORN).addConsumed(getRequiredFoodPerTurn());

        // get production from town factories that doesn't consume any sources
        ConstructionType.SOURCE_1.forEach(goodsType -> {
            getConstructionProducing(goodsType).ifPresent(con -> {
                GoodsProductionStats goodsStats = out.getStatsByType(goodsType);
                ConstructionTurnProduction turnProd = con.getProduction();
                goodsStats.setRowProduction(turnProd.getProducedGoods().get().getAmount());
            });
        });

        // get production from town factories that consume some primary sources
        ConstructionType.SOURCE_2.forEach(goodsType -> {
            computeSecondaryProduction(out, goodsType);
        });

        // get production from town factories that consume secondary sources
        ConstructionType.SOURCE_3.forEach(goodsType -> {
            computeSecondaryProduction(out, goodsType);
        });

        return out;
    }

    private void computeSecondaryProduction(final ColonyProductionStats out,
            final GoodsType goodsTypeProduced) {
        if (getConstructionProducing(goodsTypeProduced).isPresent()) {
            final Construction producedAt = getConstructionProducing(goodsTypeProduced).get();
            GoodsProductionStats goodProdStats = out.getStatsByType(goodsTypeProduced);
            GoodsType goodsTypeConsumed = producedAt.getType().getConsumptionPerTurn().get()
                    .getType();
            GoodsProductionStats goodConsumedStats = out.getStatsByType(goodsTypeConsumed);

            Preconditions.checkState(goodConsumedStats.getConsumed() == 0,
                    "good type was already computed, good was already consumed.");
            int numberOfavailableInputGoods = goodConsumedStats.getInWarehouseAfter();

            final ConstructionTurnProduction turnProd = producedAt
                    .getProduction(Goods.of(goodsTypeConsumed, numberOfavailableInputGoods));
            goodConsumedStats.addConsumed(turnProd.getConsumedGoods().get().getAmount());
            goodProdStats.setRowProduction(turnProd.getProducedGoods().get().getAmount());
            if (turnProd.getBlockedGoods().isPresent()) {
                goodProdStats.setBlockedProduction(turnProd.getBlockedGoods().get().getAmount());
            } else {
                goodProdStats.setBlockedProduction(0);
            }
        }
    }

    private Optional<Construction> getConstructionProducing(final GoodsType goodsType) {
        return constructions.stream()
                .filter(construction -> construction.getType().getProductionPerTurn().isPresent()
                        && construction.getType().getProductionPerTurn().get().getType()
                                .equals(goodsType))
                .findAny();
    }

    /**
     * @return the colonyBuildingQueue
     */
    public ColonyBuildingQueue getColonyBuildingQueue() {
        return colonyBuildingQueue;
    }
}
