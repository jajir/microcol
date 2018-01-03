package org.microcol.gui.image;

import org.microcol.gui.gamepanel.GamePanelView;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Load background image, split it into separate tiles ad store then back to
 * image store.
 */
public class BackgroundImageLoader implements ImageLoader {

	/**
	 * How many tiles will be loaded in each row.
	 */
	private final int TILE_WIDTH = 9;

	/**
	 * How many tiles will be loaded in each column.
	 */
	private final int TILE_HEIGHT = 5;

	@Override
	public void preload(final ImageProvider imageProvider) {
		final Image img = ImageProvider.getRawImage(ImageProvider.BACKGROUND_IMAGE_NAME);
		imageProvider.registerImage(ImageProvider.BACKGROUND_IMAGE_NAME, img);
		
		for (int y = 0; y < TILE_HEIGHT; y++) {
			for (int x = 0; x < TILE_WIDTH; x++) {
				final String name = "type" + x + y;
				final PixelReader reader = img.getPixelReader();
				final WritableImage tile = new WritableImage(reader, x * GamePanelView.TILE_WIDTH_IN_PX,
						y * GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX,
						GamePanelView.TILE_WIDTH_IN_PX);
				imageProvider.registerImage(name, tile);
			}
		}
		
		imageProvider.registerImage(ImageProvider.IMG_TREE, imageProvider.getImage("type34"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN, imageProvider.getImage("type20"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_GRASSLAND, imageProvider.getImage("type10"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA, imageProvider.getImage("type30"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC, imageProvider.getImage("type40"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_HIGH_SEA, imageProvider.getImage("type50"));
	}

}
