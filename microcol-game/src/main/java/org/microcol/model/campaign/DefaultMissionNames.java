package org.microcol.model.campaign;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public enum DefaultMissionNames implements MissionName {

	findNewWorld("findNewWorld", "default-findNewWorld"), buildArmy("buildArmy", "default-buildArmy"), thrive("thrive",
			"default-thrive");

	private final String name;

	private final String classPathFile;

	DefaultMissionNames(final String key, final String fileName) {
		this.name = Preconditions.checkNotNull(key);
		this.classPathFile = PATH_PREFIX + fileName + PATH_SUFFIX;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass()).add("name", name).add("classPathFile", classPathFile).toString();
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