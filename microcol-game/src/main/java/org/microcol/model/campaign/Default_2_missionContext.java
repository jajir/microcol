package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.store.ModelPo;

/**
 * First mission. Find New World.
 */
public class Default_2_missionContext extends AbstractMissionContext {

    private final static String KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED = "wasNumberOfColoniesTargetReached";

    private final static String KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED = "wasNumberOfGoldTargetReached";

    private final static String KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED = "wasNumberOfMilitaryUnitsTargetReached";

    private boolean wasNumberOfColoniesTargetReached = false;

    private boolean wasNumberOfGoldTargetReached = false;

    private boolean wasNumberOfMilitaryUnitsTargetReached = false;

    @Override
    public void initialize(ModelPo modelPo) {
        if (modelPo.getCampaign().getData() != null) {
            wasNumberOfColoniesTargetReached = getBoolean(modelPo,
                    KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED);
            wasNumberOfGoldTargetReached = getBoolean(modelPo,
                    KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED);
            wasNumberOfMilitaryUnitsTargetReached = getBoolean(modelPo,
                    KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED);
        }
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out = new HashMap<>();
        out.put(KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED,
                Boolean.toString(wasNumberOfColoniesTargetReached));
        out.put(KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED,
                Boolean.toString(wasNumberOfGoldTargetReached));
        out.put(KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED,
                Boolean.toString(wasNumberOfMilitaryUnitsTargetReached));
        return out;
    }

    /**
     * @return the wasNumberOfColoniesTargetReached
     */
    public boolean isWasNumberOfColoniesTargetReached() {
        return wasNumberOfColoniesTargetReached;
    }

    /**
     * @param wasNumberOfColoniesTargetReached
     *            the wasNumberOfColoniesTargetReached to set
     */
    public void setWasNumberOfColoniesTargetReached(boolean wasNumberOfColoniesTargetReached) {
        this.wasNumberOfColoniesTargetReached = wasNumberOfColoniesTargetReached;
    }

    /**
     * @return the wasNumberOfGoldTargetReached
     */
    public boolean isWasNumberOfGoldTargetReached() {
        return wasNumberOfGoldTargetReached;
    }

    /**
     * @param wasNumberOfGoldTargetReached
     *            the wasNumberOfGoldTargetReached to set
     */
    public void setWasNumberOfGoldTargetReached(boolean wasNumberOfGoldTargetReached) {
        this.wasNumberOfGoldTargetReached = wasNumberOfGoldTargetReached;
    }

    /**
     * @return the wasNumberOfMilitaryUnitsTargetReached
     */
    public boolean isWasNumberOfMilitaryUnitsTargetReached() {
        return wasNumberOfMilitaryUnitsTargetReached;
    }

    /**
     * @param wasNumberOfMilitaryUnitsTargetReached
     *            the wasNumberOfMilitaryUnitsTargetReached to set
     */
    public void setWasNumberOfMilitaryUnitsTargetReached(
            boolean wasNumberOfMilitaryUnitsTargetReached) {
        this.wasNumberOfMilitaryUnitsTargetReached = wasNumberOfMilitaryUnitsTargetReached;
    }

}
