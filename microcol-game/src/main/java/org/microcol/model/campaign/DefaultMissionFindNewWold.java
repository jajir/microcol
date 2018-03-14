package org.microcol.model.campaign;

/**
 * First mission. Find New World.
 */
public class DefaultMissionFindNewWold extends AbstractMission {

    private final static String NAME = "findNewWorld";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionFindNewWold() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

}
