package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.NewGameController;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.IceCoastMapGenerator;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.image.ImageRandomProvider;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.Image;

/**
 * Class determine correct border tile for actual game model.
 *
 */
public class MapManager {

	private final GrassCoastMapGenerator grassCoastMapGenerator;

	private final IceCoastMapGenerator iceCoastMapGenerator;

	private final ImageProvider imageProvider;

	private ImageRandomProvider imageRandomProvider;

	@Inject
	MapManager(final GrassCoastMapGenerator grassCoastMapGenerator, final IceCoastMapGenerator iceCoastMapGenerator,
			final GameModelController gameModelController, final NewGameController newGameController,
			final ImageProvider imageProvider) {
		this.grassCoastMapGenerator = Preconditions.checkNotNull(grassCoastMapGenerator);
		this.iceCoastMapGenerator = Preconditions.checkNotNull(iceCoastMapGenerator);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		newGameController.addListener(event -> {
			grassCoastMapGenerator.setMap(gameModelController.getModel().getMap());
			iceCoastMapGenerator.setMap(gameModelController.getModel().getMap());
			imageRandomProvider = new ImageRandomProvider(imageProvider, gameModelController.getModel().getMap());
		});
	}

	public Image getCoatsImageAt(final Location location) {
		Image img = grassCoastMapGenerator.getImageAt(location);
		if (img == null) {
			return iceCoastMapGenerator.getImageAt(location);
		} else {
			return img;
		}
	}

	Image getTerrainImage(final TerrainType terrainType, final Location location) {
		final Image image = imageRandomProvider.getTerrainImage(terrainType, location);
		if (image == null) {
			return imageProvider.getTerrainImage(terrainType);
		} else {
			return image;
		}
	}
	
	public Image getTreeImage(final Location location) {
		return imageRandomProvider.getTreeImage(location);
	}

}
