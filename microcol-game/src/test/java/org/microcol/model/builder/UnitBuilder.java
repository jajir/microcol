package org.microcol.model.builder;

import org.microcol.model.GoodType;
import org.microcol.model.IdManager;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.UnitType;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

public class UnitBuilder {

    private final UnitPo unitPo;

    private final ModelPo modelPo;

    private final IdManager idManager;

    UnitBuilder(final ModelPo modelPo, final IdManager idManager) {
        this.modelPo = Preconditions.checkNotNull(modelPo);
        this.idManager = Preconditions.checkNotNull(idManager);
        unitPo = new UnitPo();
        unitPo.setId(idManager.nextId());
    }

    public UnitBuilder setPlayerName(final String playerName) {
        unitPo.setOwnerId(playerName);
        return this;
    }

    @Deprecated
    public UnitBuilder setPlayer(final Player player) {
        unitPo.setOwnerId(player.getName());
        return this;
    }

    public UnitBuilder setType(final UnitType type) {
        Preconditions.checkNotNull(type, "Unit type is empty");
        unitPo.setType(type);
        return this;
    }

    public UnitBuilder setLocation(final Location location) {
        unitPo.setPlaceMap(new PlaceMapPo());
        unitPo.getPlaceMap().setLocation(location);
        return this;
    }

    public UnitBuilder setShipIncomingToColonies(int inHowManyturns) {
        unitPo.setPlaceHighSeas(new PlaceHighSeasPo());
        unitPo.getPlaceHighSeas().setRemainigTurns(inHowManyturns);
        unitPo.getPlaceHighSeas().setTravelToEurope(false);
        return this;
    }

    public UnitBuilder setShipIncomingToEurope(int inHowManyturns) {
        unitPo.setPlaceHighSeas(new PlaceHighSeasPo());
        unitPo.getPlaceHighSeas().setRemainigTurns(inHowManyturns);
        unitPo.getPlaceHighSeas().setTravelToEurope(true);
        return this;
    }

    public UnitBuilder setUnitToEuropePortPier() {
        unitPo.setPlaceEuropePort(new PlaceEuropePortPo());
        return this;
    }

    public UnitBuilder addCargoGood(final GoodType goodType, final int amount) {
        final CargoSlotPo cargoSlotPo = new CargoSlotPo();
        cargoSlotPo.setGoodType(goodType);
        cargoSlotPo.setAmount(amount);
        unitPo.getCargo().getSlots().add(cargoSlotPo);
        return this;
    }

    public UnitBuilder addCargoUnit(final UnitType type) {
        final UnitPo tmpPo = new UnitPo();
        tmpPo.setId(idManager.nextId());
        tmpPo.setType(type);
        tmpPo.setOwnerId(
                Preconditions.checkNotNull(unitPo.getOwnerId(), "Player name was not set"));
        tmpPo.setPlaceCargoSlot(new PlaceCargoSlotPo());
        tmpPo.setAction(new UnitActionNoActionPo());
        modelPo.addUnit(tmpPo);
        final CargoSlotPo cargoSlotPo = new CargoSlotPo();
        cargoSlotPo.setUnitId(tmpPo.getId());
        unitPo.getCargo().getSlots().add(cargoSlotPo);
        return this;
    }

    public UnitPo build() {
        unitPo.setAction(new UnitActionNoActionPo());
        return unitPo;
    }

}
