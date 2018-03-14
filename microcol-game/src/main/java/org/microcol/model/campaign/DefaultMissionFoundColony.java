package org.microcol.model.campaign;

/**
 * First mission. Find New World.
 */
public class DefaultMissionFoundColony extends AbstractMission {

    private final static String NAME = "foundColony";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionFoundColony() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

}
