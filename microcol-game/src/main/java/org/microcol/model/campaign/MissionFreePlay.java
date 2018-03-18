package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;

/**
 * Free play mission definition. There are no limitations player can do
 * anything.
 */
public class MissionFreePlay extends AbstractMission {

    /**
     * Free play game map.
     */
    private final static String FREE_PLAY_MISSION_MAP = "/maps/free-play.json";

    MissionFreePlay() {
        super(CampaignFreePlay.FREE_PLAY, 0, FREE_PLAY_MISSION_MAP);
    }
    
    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        
        
    }

}
