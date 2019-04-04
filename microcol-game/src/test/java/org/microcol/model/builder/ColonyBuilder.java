package org.microcol.model.builder;

import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.IdManager;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Helps build colony.
 */
public class ColonyBuilder {

    private final ColonyPo colonyPo;

    private final PlayerBuilder playerBuilder;
    
    private final IdManager idManager;

    public ColonyBuilder(final String name, final PlayerBuilder playerBuilder, final IdManager idManager) {
        this.playerBuilder = Preconditions.checkNotNull(playerBuilder);
        this.idManager = Preconditions.checkNotNull(idManager);
        this.colonyPo = new ColonyPo();
        
        colonyPo.setName(Preconditions.checkNotNull(name));
        colonyPo.setOwnerName(Preconditions.checkNotNull(playerBuilder.getPlayerPo().getName()));
    }

    public PlayerBuilder build() {
        playerBuilder.getModelPo().getColonies().add(colonyPo);
        return playerBuilder;
    }

    public ColonyBuilder setLocation(final Location location) {
        colonyPo.setLocation(location);
        return this;
    }

    public ColonyBuilder setDefaultConstructions() {
        ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(constructionType -> {
            final ConstructionPo constructionPo = new ConstructionPo();
            constructionPo.setType(constructionType);
            colonyPo.getConstructions().add(constructionPo);
        });
        return this;
    }

    /**
     * Allows to specify extra construction in colony.
     * 
     * @param constructionType
     *            required construction type
     * @return colony builder
     */
    public ColonyBuilder setConstruction(final ConstructionType constructionType) {
        final ConstructionPo constructionPo = new ConstructionPo();
        constructionPo.setType(constructionType);
        colonyPo.getConstructions().add(constructionPo);
        return this;
    }

    private UnitPo createUnitByType(final UnitType unitType) {
        final UnitPo worker = new UnitPo();
        worker.setId(idManager.nextId());
        worker.setOwnerId(playerBuilder.getPlayerPo().getName());
        worker.setType(unitType);
        playerBuilder.getModelPo().addUnit(worker);
        return worker;
    }

    public ColonyBuilder setWorker(final ConstructionType constructionType, final int position,
            final UnitType unitType) {
        Preconditions.checkNotNull(constructionType);
        Preconditions.checkNotNull(unitType);
        Preconditions.checkArgument(position >= 0 && position < 3,
                "Position is not within range 0,1,2.");
        final ConstructionPo constructionPo = colonyPo.getConstructionByType(constructionType);

        final UnitPo worker = createUnitByType(unitType);
        final PlaceConstructionSlotPo placeConstructionSlotPo = new PlaceConstructionSlotPo();
        worker.setPlaceConstructionSlot(placeConstructionSlotPo);
        worker.setAction(new UnitActionNoActionPo());

        constructionPo.getSlots().get(position).setWorkerId(worker.getId());
        return this;
    }

    public ColonyBuilder setWorker(final Location fieldDirection, final UnitType unitType,
            final GoodsType producedGoodsType) {
        Preconditions.checkNotNull(fieldDirection);
        Preconditions.checkNotNull(unitType);

        final UnitPo worker = createUnitByType(unitType);
        final PlaceColonyFieldPo placeColonyFieldPo = new PlaceColonyFieldPo();
        worker.setPlaceColonyField(placeColonyFieldPo);
        worker.setAction(new UnitActionNoActionPo());

        colonyPo.getFieldByDirection(fieldDirection).setWorkerId(worker.getId());
        colonyPo.getFieldByDirection(fieldDirection).setProducedGoodsType(producedGoodsType);
        return this;
    }

    public ColonyBuilder setGood(final GoodsType goodsType, final Integer amount) {
        Preconditions.checkNotNull(goodsType);
        Preconditions.checkNotNull(amount);
        Preconditions.checkArgument(!colonyPo.getColonyWarehouse().containsKey(goodsType.name()),
                "Good type (%s) was alredy defined.", goodsType.name());
        colonyPo.getColonyWarehouse().put(goodsType.name(), amount);
        return this;
    }

}
