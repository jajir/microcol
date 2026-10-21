package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

/**
 * Helping class holding request for creating unit.
 */
public class UnitCreateRequest {

    private final Model model;

    private final Function<Unit, Place> modelPo;

    private final Integer unitId;

    private final UnitType unitType;

    private final Player owner;

    private final UnitAction action;

    /** Movement in thirds of a point; see MovementPoints. */
    private final int movementThirds;

    private Function<Unit, Cargo> cargoBuilder;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    private boolean artilleryDamaged;

    private boolean veteran;

    /** Native brave home settlement ID; {@code null} for a homeless brave. */
    private Integer nativeHomeSettlementId;

    private boolean nativeHomeless;

    public Integer getNativeHomeSettlementId() {
        return nativeHomeSettlementId;
    }

    public void setNativeHomeSettlementId(
            final Integer nativeHomeSettlementId) {
        this.nativeHomeSettlementId = nativeHomeSettlementId;
    }

    public boolean isNativeHomeless() {
        return nativeHomeless;
    }

    public void setNativeHomeless(final boolean nativeHomeless) {
        this.nativeHomeless = nativeHomeless;
    }

    public boolean isVeteran() {
        return veteran;
    }

    public void setVeteran(final boolean veteran) {
        this.veteran = veteran;
    }

    public boolean isArtilleryDamaged() {
        return artilleryDamaged;
    }

    public void setArtilleryDamaged(final boolean damaged) {
        artilleryDamaged = damaged;
    }

    public UnitCreateRequest(final Model model,
            final Function<Unit, Place> placeBuilder, final Integer unitId,
            final UnitType unitType, final Player owner,
            final UnitAction action, final int movementThirds) {
        this.model = Preconditions.checkNotNull(model);
        this.modelPo = Preconditions.checkNotNull(placeBuilder);
        this.unitId = Preconditions.checkNotNull(unitId);
        this.unitType = Preconditions.checkNotNull(unitType);
        this.owner = Preconditions.checkNotNull(owner);
        this.action = Preconditions.checkNotNull(action);
        this.movementThirds = movementThirds;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return the modelPo
     */
    public Function<Unit, Place> getPlaceBuilder() {
        return modelPo;
    }

    /**
     * @return the tools
     */
    public int getTools() {
        return tools;
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(int tools) {
        this.tools = tools;
    }

    /**
     * @return the holdingGuns
     */
    public boolean isHoldingGuns() {
        return holdingGuns;
    }

    /**
     * @param holdingGuns the holdingGuns to set
     */
    public void setHoldingGuns(boolean holdingGuns) {
        this.holdingGuns = holdingGuns;
    }

    /**
     * @return the mounted
     */
    public boolean isMounted() {
        return mounted;
    }

    /**
     * @param mounted the mounted to set
     */
    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

    /**
     * @return the unitId
     */
    public Integer getUnitId() {
        return unitId;
    }

    /**
     * @return the unitType
     */
    public UnitType getUnitType() {
        return unitType;
    }

    /**
     * @return the owner
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @return the action
     */
    public UnitAction getAction() {
        return action;
    }

    /**
     * @return the movement in thirds of a point
     */
    public int getMovementThirds() {
        return movementThirds;
    }

    /**
     * @return the cargoBuilder
     */
    public Function<Unit, Cargo> getCargoBuilder() {
        return cargoBuilder;
    }

    /**
     * @param cargoBuilder the cargoBuilder to set
     */
    public void setCargoBuilder(Function<Unit, Cargo> cargoBuilder) {
        this.cargoBuilder = cargoBuilder;
    }
}
