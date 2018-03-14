package org.microcol.model.campaign;

/**
 * First mission. Thrive.
 */
public class DefaultMissionThrive extends AbstractMission {

    private final static String NAME = "thrive";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionThrive() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

}
