package org.microcol.model.unit;

import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyField;
import org.microcol.model.ConstructionSlot;
import org.microcol.model.GoodType;
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
public final class UnitFreeColonist extends Unit {

    public final static int REQUIRED_HORSES_FOR_MOUNTED_UNIT = 50;

    public final static int REQUIRED_MUSKETS_FOR_ARMED_UNIT = 50;

    private final static int MAXIMAL_NUMBER_OF_TOOLS = CargoSlot.MAX_CARGO_SLOT_CAPACITY;

    private int tools;

    private boolean holdingGuns;

    private boolean mounted;

    public UnitFreeColonist(Model model, Integer id, Function<Unit, Place> placeBuilder,
            Player owner, int availableMoves, final UnitAction unitAction) {
        this(model, id, placeBuilder, owner, availableMoves, unitAction, 0, false, false);
    }

    public UnitFreeColonist(Model model, Integer id, Function<Unit, Place> placeBuilder,
            Player owner, int availableMoves, final UnitAction unitAction, final int tools,
            final boolean holdingGuns, final boolean mounted) {
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
    public void placeToColonyField(final ColonyField colonyField, final GoodType producedGoodType) {
        unequipAll();
        super.placeToColonyField(colonyField, producedGoodType);
    }

    @Override
    public void placeToColonyStructureSlot(final ConstructionSlot structureSlot) {
        unequipAll();
        super.placeToColonyStructureSlot(structureSlot);
    }

    @Override
    public UnitType getType() {
        return UnitType.COLONIST;
    }

    private void unequipAll() {
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
        }else{
            return 1;
        }
    }
    
    @Override
    public double getMilitaryStrenght() {
        if (isMounted()) {
            if (isHoldingGuns()) {
                return 2;
            }else{
                return 1.5;
            }
        }else{
            if (isHoldingGuns()) {
                return 1.5;
            }else{
                return 1;
            }
        }
    }

    public void equipWithHorses() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int horses = colony.getColonyWarehouse().getGoodAmmount(GoodType.HORSE);
        Preconditions.checkState(horses >= REQUIRED_HORSES_FOR_MOUNTED_UNIT,
                "In colony (%s) is not enought horses to mount unit on them", colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getColonyWarehouse().removeFromWarehouse(GoodType.HORSE,
                REQUIRED_HORSES_FOR_MOUNTED_UNIT);
        mounted = true;
    }

    public void unequipWithHorses() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getColonyWarehouse().addToWarehouse(GoodType.HORSE,
                REQUIRED_HORSES_FOR_MOUNTED_UNIT);
        mounted = false;
    }

    public void equipWithMuskets() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int muskets = colony.getColonyWarehouse().getGoodAmmount(GoodType.MUSKET);
        Preconditions.checkState(muskets >= REQUIRED_MUSKETS_FOR_ARMED_UNIT,
                "In colony (%s) is not enought muskets to arm unit with them", colony);
        if (isHoldingTools()) {
            unequipWithTools();
        }
        colony.getColonyWarehouse().removeFromWarehouse(GoodType.MUSKET,
                REQUIRED_MUSKETS_FOR_ARMED_UNIT);
        holdingGuns = true;
    }

    public void unequipWithMuskets() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getColonyWarehouse().addToWarehouse(GoodType.MUSKET,
                REQUIRED_MUSKETS_FOR_ARMED_UNIT);
        holdingGuns = false;
    }

    public void equipWithTools() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        final int totalTools = colony.getColonyWarehouse().getGoodAmmount(GoodType.TOOLS);
        Preconditions.checkState(totalTools > 0, "In colony (%s) is not ane tools", colony);
        if (isHoldingGuns()) {
            unequipWithMuskets();
        }
        if (isMounted()) {
            unequipWithHorses();
        }
        final int removeTools = Math.min(totalTools, MAXIMAL_NUMBER_OF_TOOLS);
        colony.getColonyWarehouse().removeFromWarehouse(GoodType.TOOLS, removeTools);
        tools = removeTools;
    }

    public void unequipWithTools() {
        final Colony colony = verifyThatUnitIsOutsideColony();
        colony.getColonyWarehouse().addToWarehouse(GoodType.TOOLS, tools);
        tools = 0;
    }

    private Colony verifyThatUnitIsOutsideColony() {
        final Optional<Colony> oColony = getModel().getColonyAt(getLocation());
        Preconditions.checkState(oColony.isPresent(), "Unit at (%s) is not outside colony",
                getLocation());
        final Colony colony = oColony.get();
        Preconditions.checkState(getOwner().equals(colony.getOwner()),
                "Colony at (%s) doesn't belong to same owner as unit (%s)", getLocation(), this);
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
     * @param tools
     *            the tools to set
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
     * @param holdingGuns
     *            the holdingGuns to set
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
     * @param mounted
     *            the mounted to set
     */
    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

}
