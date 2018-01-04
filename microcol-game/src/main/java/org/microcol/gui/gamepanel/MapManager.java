package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.NewGameController;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.IceCoastMapGenerator;
import org.microcol.model.Location;

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

	@Inject
	MapManager(final GrassCoastMapGenerator grassCoastMapGenerator,
			final IceCoastMapGenerator iceCoastMapGenerator,
			final GameModelController gameModelController,
			final NewGameController newGameController) {
		this.grassCoastMapGenerator = Preconditions.checkNotNull(grassCoastMapGenerator);
		this.iceCoastMapGenerator = Preconditions.checkNotNull(iceCoastMapGenerator);
		newGameController.addListener(event -> {
			grassCoastMapGenerator.setMap(gameModelController.getModel().getMap());
			iceCoastMapGenerator.setMap(gameModelController.getModel().getMap());
		});
	}
	
	public Image getImageAt(final Location location){
		Image img = grassCoastMapGenerator.getImageAt(location);
		if (img == null) {
			return iceCoastMapGenerator.getImageAt(location);
		} else {
			return img;
		}
	}
	
}
