package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

import com.google.inject.Inject;

public class IceCoastMapGenerator extends AbstractCoastMapGenerator {

	@Inject
	IceCoastMapGenerator(ImageProvider imageProvider) {
		super(imageProvider);
	}

	@Override
	public String getPrefix() {
		return "ice-";
	}

	@Override
	public boolean isVoid(final Location infoHolder) {
		final TerrainType terrainType = getTerrainTypeAt(infoHolder);
		return terrainType.isSea();
	}

	@Override
	public boolean isMass(final Location infoHolder) {
		final TerrainType terrainType = getTerrainTypeAt(infoHolder);
		return TerrainType.TUNDRA.equals(terrainType) || TerrainType.ARCTIC.equals(terrainType);
	}

	@Override
	public boolean skipp(final Location infoHolder) {
		final TerrainType terrainType = getTerrainTypeAt(infoHolder);
		return terrainType.isLand() && !TerrainType.TUNDRA.equals(terrainType)
				&& !TerrainType.ARCTIC.equals(terrainType);
	}
}
