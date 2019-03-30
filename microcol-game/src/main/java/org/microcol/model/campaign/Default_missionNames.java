package org.microcol.model.campaign;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public enum Default_missionNames implements MissionName {

    findNewWorld("findNewWorld", "default-campaign-mission-0-findNewWorld"),
    buildArmy("buildArmy", "default-campaign-mission-1-buildArmy"),
    thrive("thrive", "default-campaign-mission-2-thrive");

    private final String name;

    private final String classPathFile;

    Default_missionNames(final String key, final String fileName) {
        this.name = Preconditions.checkNotNull(key);
        this.classPathFile = PATH_PREFIX + fileName + PATH_SUFFIX;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name)
                .add("classPathFile", classPathFile).toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassPathFile() {
        return classPathFile;
    }

    @Override
    public CampaignName getCampaignName() {
        return CampaignNames.defaultCampaign;
    }

}