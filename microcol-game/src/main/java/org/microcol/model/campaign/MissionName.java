package org.microcol.model.campaign;

/**
 * It's marker interface for mission name enums.
 */
public interface MissionName {

    String PATH_PREFIX = "/maps/";

    String PATH_SUFFIX = ".microcol";

    /**
     * Name used for identification of mission. Name is also used and key to
     * localization resource bundles.
     *
     * @return mission unique name
     */
    String getName();

    /**
     * Mission model file name at class path. It's something like
     * <i>/maps/03-before-game-over.microcol</i>.
     *
     * @return misson definition file name at class path
     */
    String getClassPathFile();

    /**
     * Get campaign unique name.
     *
     * @return campaign unique name
     */
    CampaignName getCampaignName();

}
