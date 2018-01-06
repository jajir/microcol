package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;

public class ImageRandomProvider {

	private final ImageProvider imageProvider;

	private final WorldMapRandoms mapRandoms;

	public ImageRandomProvider(final ImageProvider imageProvider, final WorldMap worldMap) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(worldMap);
		mapRandoms = new WorldMapRandoms(worldMap);
	}

	public Image getTerrainImage(final TerrainType terrainType, final Location location) {
		//TODO it should be replaced by type
		if (TerrainType.OCEAN.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			if(rnd % 11 == 0){
				return imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN_2);
			}else{
				if(rnd % 63 == 0){
					return imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN_3);
				}else{
					return imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN_1);
				}
			}
		}
		if (TerrainType.MOUNTAIN.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			return rnd % 2 == 0 ? imageProvider.getImage(ImageProvider.IMG_TILE_MOUNTAIN_2)
					: imageProvider.getImage(ImageProvider.IMG_TILE_MOUNTAIN_1);
		}
		if (TerrainType.HILL.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			return rnd % 2 == 0 ? imageProvider.getImage(ImageProvider.IMG_TILE_HILL_1)
					: imageProvider.getImage(ImageProvider.IMG_TILE_HILL_2);
		}
		if (TerrainType.PRAIRIE.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			return rnd % 2 == 0 ? imageProvider.getImage(ImageProvider.IMG_TILE_PRAIRIE_1)
					: imageProvider.getImage(ImageProvider.IMG_TILE_PRAIRIE_2);
		}
		if (TerrainType.ARCTIC.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			return rnd % 2 == 0 ? imageProvider.getImage(ImageProvider.IMG_TILE_ARCTIC_2)
					: imageProvider.getImage(ImageProvider.IMG_TILE_ARCTIC_1);
		}
		if (TerrainType.TUNDRA.equals(terrainType)) {
			final Integer rnd = mapRandoms.getRandomAt(location);
			return rnd % 7 == 0 ? imageProvider.getImage(ImageProvider.IMG_TILE_TUNDRA_2)
					: imageProvider.getImage(ImageProvider.IMG_TILE_TUNDRA_1);
		}
		return null;
	}

	public Image getTreeImage(final Location location) {
		final Integer rnd = mapRandoms.getRandomAt(location);
		return rnd % 3 == 0 ? imageProvider.getImage(ImageProvider.IMG_TREE_2)
				: imageProvider.getImage(ImageProvider.IMG_TREE_1);
	}
	

}
