package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.event.GameStartedEvent;

/**
 * First mission. Find New World.
 */
public class DefaultMissionFindNewWold extends AbstractMission {

    private final static String NAME = "findNewWorld";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionFindNewWold() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
        setFinished(true);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ModelAdapter() {
            @Override
            public void gameStarted(final GameStartedEvent event) {
                missionCallBack.showMessage("campaign.default.start");
            }
        });
    }

}
