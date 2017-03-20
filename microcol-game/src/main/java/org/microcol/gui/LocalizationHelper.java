package org.microcol.gui;

import org.microcol.model.Terrain;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Helps localize messages.
 */
public class LocalizationHelper {

	private final static String TERRAIN_PREFIX = "terrain.";
	private final static String TERRAIN_SUFFIX_NAME = ".name";

	/**
	 * Localization class.
	 */
	private final Text text;

	@Inject
	public LocalizationHelper(final Text text) {
		this.text = Preconditions.checkNotNull(text);
	}

	public String getTerrainName(final Terrain terrain) {
		return text.get(TERRAIN_PREFIX + terrain.name() + TERRAIN_SUFFIX_NAME);
	}

}
