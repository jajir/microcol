package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.NewGameController;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.Image;

/**
 * Class determine correct border tile for actual game model.
 *
 */
public class MapManager {

	private final MapImageGenerator mapImageGenerator;

	@Inject
	MapManager(final MapImageGenerator mapImageGenerator,
			final GameModelController gameModelController,
			final NewGameController newGameController) {
		this.mapImageGenerator = Preconditions.checkNotNull(mapImageGenerator);
		newGameController.addListener(event -> {
			mapImageGenerator.setMap(gameModelController.getModel().getMap());
		});
	}
	
	public Image getImageAt(final Location location){
		return mapImageGenerator.getImageAt(location);
	}
	
}
