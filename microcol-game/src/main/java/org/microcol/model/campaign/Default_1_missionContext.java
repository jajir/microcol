package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.store.ModelPo;

/**
 * Mission context for 1 mission from default campaign.
 */
public class Default_1_missionContext extends AbstractMissionContext {

    private final static String MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN = "wasContinentOnSightMessageWasShown";

    private final static String MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN = "wasSellCigarsMessageShown";

    private final static String MAP_KEY_CIGARS_WAS_SOLD = "cigarsWasSold";

    private Integer cigarsWasSold = 0;

    private boolean wasContinentOnSightMessageShown = false;

    private boolean wasMessageSellCigarsShown = false;

    @Override
    public void initialize(final ModelPo modelPo) {
        if (modelPo.getCampaign().getData() != null) {
            wasContinentOnSightMessageShown = getBoolean(modelPo,
                    MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN);
            wasMessageSellCigarsShown = getBoolean(modelPo, MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN);
            cigarsWasSold = getInt(modelPo, MAP_KEY_CIGARS_WAS_SOLD);
        }
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out = new HashMap<>();
        out.put(MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN,
                Boolean.toString(wasContinentOnSightMessageShown));
        out.put(MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN, Boolean.toString(wasMessageSellCigarsShown));
        out.put(MAP_KEY_CIGARS_WAS_SOLD, Integer.toString(cigarsWasSold));
        return out;
    }

    /**
     * @return the cigarsWasSold
     */
    public Integer getCigarsWasSold() {
        return cigarsWasSold;
    }

    /**
     * @param cigarsWasSold
     *            the cigarsWasSold to set
     */
    public void setCigarsWasSold(Integer cigarsWasSold) {
        this.cigarsWasSold = cigarsWasSold;
    }

    /**
     * @return the wasContinentOnSightMessageShown
     */
    public boolean isWasContinentOnSightMessageShown() {
        return wasContinentOnSightMessageShown;
    }

    /**
     * @param wasContinentOnSightMessageShown
     *            the wasContinentOnSightMessageShown to set
     */
    public void setWasContinentOnSightMessageShown(boolean wasContinentOnSightMessageShown) {
        this.wasContinentOnSightMessageShown = wasContinentOnSightMessageShown;
    }

    /**
     * @return the wasMessageSellCigarsShown
     */
    public boolean isWasMessageSellCigarsShown() {
        return wasMessageSellCigarsShown;
    }

    /**
     * @param wasMessageSellCigarsShown
     *            the wasMessageSellCigarsShown to set
     */
    public void setWasMessageSellCigarsShown(boolean wasMessageSellCigarsShown) {
        this.wasMessageSellCigarsShown = wasMessageSellCigarsShown;
    }

}
