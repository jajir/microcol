package org.microcol.model.unit;

import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Unit Free Colonist.
 */
public final class UnitFreeColonist extends CanMount {

    public final static int REQUIRED_HORSES_FOR_MOUNTED_UNIT = 50;

    public final static int REQUIRED_MUSKETS_FOR_ARMED_UNIT = 50;

    private final static int MAXIMAL_NUMBER_OF_TOOLS = CargoSlot.MAX_CARGO_SLOT_CAPACITY;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    public UnitFreeColonist(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction unitAction,
            final int tools, final boolean holdingGuns, final boolean mounted) {
        super(model, id, placeBuilder, owner, availableMoves, unitAction);
        this.tools = tools;
        this.holdingGuns = holdingGuns;
        this.mounted = mounted;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setTools(tools);
        out.setMounted(mounted);
        out.setHoldingGuns(holdingGuns);
        return out;
    }

    @Override
    public void placeToColonyField(final ColonyField colonyField,
            final GoodsType producedGoodsType) {
        validateColonyField(colonyField, producedGoodsType);
        unequipAll();
        super.placeToColonyField(colonyField, producedGoodsType);
    }

    @Override
    public void placeToColonyStructureSlot(
            final ConstructionSlot structureSlot) {
        validateColonyStructureSlot(structureSlot);
        unequipAll();
        super.placeToColonyStructureSlot(structureSlot);
    }

    @Override
    public UnitType getType() {
        return UnitType.COLONIST;
    }

    private void unequipAll() {
        // Validate every return before the first inventory mutation.
        if (isHoldingGuns())
            checkEquipmentChange(GoodsType.MUSKET, false);
        if (isHoldingTools())
            checkEquipmentChange(GoodsType.TOOLS, false);
        if (isMounted())
            checkEquipmentChange(GoodsType.HORSE, false);
        if (isHoldingGuns()) {
            unequipWithMuskets();
        }
        if (isHoldingTools()) {
            unequipWithTools();
        }
        if (isMounted()) {
            unequipWithHorses();
        }
    }

    @Override
    public int getSpeed() {
        if (isMounted()) {
            return 5;
        } else {
            return 1;
        }
    }

    @Override
    public double getMilitaryStrenght() {
        if (isMounted()) {
            if (isHoldingGuns()) {
                return 2;
            } else {
                return 1.5;
            }
        } else {
            if (isHoldingGuns()) {
                return 1.5;
            } else {
                return 1;
            }
        }
    }

    public void equipWithHorses() {
        checkEquipmentChange(GoodsType.HORSE, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int horses = colony.getWarehouse().getGoods(GoodsType.HORSE)
                .getAmount();
        Preconditions.checkState(horses >= REQUIRED_HORSES_FOR_MOUNTED_UNIT,
                "In colony (%s) is not enought horses to mount unit on them",
                colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getWarehouse().removeGoods(
                Goods.of(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT));
        mounted = true;
    }

    public void unequipWithHorses() {
        checkEquipmentChange(GoodsType.HORSE, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(
                Goods.of(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT));
        mounted = false;
    }

    public void equipWithMuskets() {
        checkEquipmentChange(GoodsType.MUSKET, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int muskets = colony.getWarehouse().getGoods(GoodsType.MUSKET)
                .getAmount();
        Preconditions.checkState(muskets >= REQUIRED_MUSKETS_FOR_ARMED_UNIT,
                "In colony (%s) is not enought muskets to arm unit with them",
                colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getWarehouse().removeGoods(
                Goods.of(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT));
        holdingGuns = true;
    }

    public void unequipWithMuskets() {
        checkEquipmentChange(GoodsType.MUSKET, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(
                Goods.of(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT));
        holdingGuns = false;
    }

    public void equipWithTools() {
        checkEquipmentChange(GoodsType.TOOLS, true);
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int totalTools = colony.getWarehouse().getGoods(GoodsType.TOOLS)
                .getAmount();
        Preconditions.checkState(totalTools > 0,
                "In colony (%s) is not ane tools", colony);
        if (isHoldingGuns()) {
            unequipWithMuskets();
        }
        if (isMounted()) {
            unequipWithHorses();
        }
        final int removeTools = Math.min(totalTools, MAXIMAL_NUMBER_OF_TOOLS);
        colony.getWarehouse()
                .removeGoods(Goods.of(GoodsType.TOOLS, removeTools));
        tools = removeTools;
    }

    public void unequipWithTools() {
        checkEquipmentChange(GoodsType.TOOLS, false);
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, tools));
        tools = 0;
    }

    public enum EquipmentProblem {
        UNIT_UNAVAILABLE, ALREADY_SET, MISSING_GOODS, STORAGE_FULL
    }

    public java.util.Optional<EquipmentProblem> getEquipmentProblem(
            GoodsType type, boolean equip) {
        com.google.common.base.Preconditions.checkArgument(
                type == GoodsType.MUSKET || type == GoodsType.HORSE
                        || type == GoodsType.TOOLS,
                "Unsupported GoodsType: %s", type);

        if (!getModel().isGameRunning()
                || !getModel().getCurrentPlayer().equals(getOwner())
                || !isAtPlaceLocation()
                || !getOwner().getAllUnits().stream().anyMatch(u -> u == this)
                || getModel().getColonyAt(getLocation()).isEmpty()
                || !getModel().getColonyAt(getLocation()).get().isValid()
                || !getModel().getColonyAt(getLocation()).get().getOwner()
                        .equals(getOwner())) {
            return java.util.Optional.of(EquipmentProblem.UNIT_UNAVAILABLE);
        }

        if (equip) {
            if (type == GoodsType.MUSKET && holdingGuns)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.HORSE && mounted)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.TOOLS && tools > 0)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
        } else {
            if (type == GoodsType.MUSKET && !holdingGuns)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.HORSE && !mounted)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
            if (type == GoodsType.TOOLS && tools == 0)
                return java.util.Optional.of(EquipmentProblem.ALREADY_SET);
        }

        if (equip && type == GoodsType.TOOLS) {
            Colony colony = getModel().getColonyAt(getLocation()).get();
            int availableTools = colony.getWarehouse().getGoods(GoodsType.TOOLS)
                    .getAmount();
            if (availableTools <= 0)
                return java.util.Optional.of(EquipmentProblem.MISSING_GOODS);
        }

        java.util.Map<GoodsType, Integer> delta = getEquipmentGoodsChange(type,
                equip);
        Colony colony = getModel().getColonyAt(getLocation()).get();
        for (GoodsType t : delta.keySet()) {
            int stock = colony.getWarehouse().getGoods(t).getAmount();
            int newStock = stock + delta.get(t);
            if (newStock < 0)
                return java.util.Optional.of(EquipmentProblem.MISSING_GOODS);
            int capacity = colony.getWarehouse().getStorageCapacity(t)
                    .getAmount();
            if (newStock > capacity)
                return java.util.Optional.of(EquipmentProblem.STORAGE_FULL);
        }

        return java.util.Optional.empty();
    }

    public java.util.Map<GoodsType, Integer> getEquipmentGoodsChange(
            GoodsType type, boolean equip) {
        com.google.common.base.Preconditions.checkArgument(
                type == GoodsType.MUSKET || type == GoodsType.HORSE
                        || type == GoodsType.TOOLS,
                "Unsupported GoodsType: %s", type);
        Colony colony = verifyThatUnitIsOutsideColony();

        java.util.Map<GoodsType, Integer> changes = new java.util.LinkedHashMap<>();

        final int gunCost = REQUIRED_MUSKETS_FOR_ARMED_UNIT;
        final int horseCost = REQUIRED_HORSES_FOR_MOUNTED_UNIT;
        final int toolsMax = MAXIMAL_NUMBER_OF_TOOLS;

        if (type == GoodsType.MUSKET) {
            changes.put(GoodsType.MUSKET, equip ? -gunCost : gunCost);
            if (equip && tools > 0)
                changes.put(GoodsType.TOOLS, tools);
        } else if (type == GoodsType.HORSE) {
            changes.put(GoodsType.HORSE, equip ? -horseCost : horseCost);
            if (equip && tools > 0)
                changes.put(GoodsType.TOOLS, tools);
        } else if (type == GoodsType.TOOLS) {
            int consume = Math.max(1, Math.min(
                    colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount(),
                    toolsMax));
            changes.put(GoodsType.TOOLS, equip ? -consume : tools);
            if (equip && holdingGuns)
                changes.put(GoodsType.MUSKET, REQUIRED_MUSKETS_FOR_ARMED_UNIT);
            if (equip && mounted)
                changes.put(GoodsType.HORSE, REQUIRED_HORSES_FOR_MOUNTED_UNIT);
        }

        return java.util.Collections.unmodifiableMap(changes);
    }

    private void checkEquipmentChange(GoodsType type, boolean equip) {
        java.util.Optional<EquipmentProblem> problem = getEquipmentProblem(type,
                equip);
        com.google.common.base.Preconditions.checkState(problem.isEmpty(),
                "Equipment change rejected: %s", problem.orElse(null));
    }

    private Colony verifyThatUnitIsOutsideColony() {
        final Optional<Colony> oColony = getModel().getColonyAt(getLocation());
        Preconditions.checkState(oColony.isPresent(),
                "Unit at (%s) is not outside colony", getLocation());
        final Colony colony = oColony.get();
        Preconditions.checkState(getOwner().equals(colony.getOwner()),
                "Colony at (%s) doesn't belong to same owner as unit (%s)",
                getLocation(), this);
        return colony;
    }

    public boolean isHoldingTools() {
        return tools > 0;
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

}
