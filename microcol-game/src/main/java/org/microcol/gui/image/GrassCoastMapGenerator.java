package org.microcol.gui.image;

import org.microcol.model.TerrainType;

import com.google.inject.Inject;

public class GrassCoastMapGenerator extends AbstractCoastMapGenerator {

	@Inject
	GrassCoastMapGenerator(ImageProvider imageProvider) {
		super(imageProvider);
	}

	@Override
	public String getPrefix() {
		return "grass-";
	}

	@Override
	public boolean isItCoast(final TerrainType terrainType) {
		return terrainType.isLand() && !TerrainType.TUNDRA.equals(terrainType)
				&& !TerrainType.ARCTIC.equals(terrainType);
	}

}
