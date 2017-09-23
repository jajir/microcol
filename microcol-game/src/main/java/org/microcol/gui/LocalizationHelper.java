package org.microcol.gui;

import org.microcol.gui.util.Text;
import org.microcol.model.ConstructionType;
import org.microcol.model.TerrainType;
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

	private static final String CONSTRUCTION_PREFIX = "construction.";
	private static final String CONSTRUCTION_SUFFIX_NAME = ".name";

	/**
	 * Localization class.
	 */
	private final Text text;

	@Inject
	public LocalizationHelper(final Text text) {
		this.text = Preconditions.checkNotNull(text);
	}

	public String getTerrainName(final TerrainType terrain) {
		return text.get(TERRAIN_PREFIX + terrain.name() + TERRAIN_SUFFIX_NAME);
	}

	public String getUnitName(final UnitType unitType) {
		return text.get(UNIT_PREFIX + unitType.name() + UNIT_SUFFIX_NAME);
	}

	public String getConstructionTypeName(final ConstructionType constructionType) {
		return text.get(CONSTRUCTION_PREFIX + constructionType.name() + CONSTRUCTION_SUFFIX_NAME);
	}

}
