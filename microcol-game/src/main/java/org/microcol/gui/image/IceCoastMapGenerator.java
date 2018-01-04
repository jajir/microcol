package org.microcol.gui.image;

import org.microcol.model.TerrainType;

import com.google.inject.Inject;

public class IceCoastMapGenerator extends AbstractCoastMapGenerator {

	@Inject
	IceCoastMapGenerator(ImageProvider imageProvider) {
		super(imageProvider);
	}
	
	@Override
	public String getPrefix(){
		return "ice-";
	}

	@Override
	public boolean isItCoast(final TerrainType terrainType){
		return TerrainType.TUNDRA.equals(terrainType) || TerrainType.ARCTIC.equals(terrainType);
	}
	
}
