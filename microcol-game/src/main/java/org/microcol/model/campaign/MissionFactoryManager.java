package org.microcol.model.campaign;

/**
 * For given mission name return mission instance.
 */
public interface MissionFactoryManager {

    /**
     * Create correct mission instance.
     * 
     * @param missionName
     *            required mission name enum value
     * @param context
     *            required creation context
     * @return new mission instance
     */
    Mission<?> make(MissionName missionName, MissionCreationContext context);

}
