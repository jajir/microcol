package org.microcol.gui;

import org.microcol.gui.util.Text;
import org.microcol.model.Terrain;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Helps localize messages.
 */
public class LocalizationHelper {

	private static final String TERRAIN_PREFIX = "terrain.";
	private static final String TERRAIN_SUFFIX_NAME = ".name";

	private static final String UNIT_PREFIX = "unit.";
	private static final String UNIT_SUFFIX_NAME = ".name";

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

	public String getUnitName(final UnitType unitType) {
		return text.get(UNIT_PREFIX + unitType.name() + UNIT_SUFFIX_NAME);
	}

}
