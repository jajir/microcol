package org.microcol.gui.image;

import org.microcol.gui.gamepanel.GamePanelView;

import com.google.common.base.Preconditions;

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
	private final static int TILE_WIDTH_COUNT = 12;

	/**
	 * How many tiles will be loaded in each column.
	 */
	private final static int TILE_HEIGHT_COUNT = 8;

	/**
	 * Tiles in background image are separated with strip. This strips prevent
	 * interfering of tiles. Without it anti-aliasing mixed colors of adjacent
	 * tiles together.
	 */
	private final static int BACKGROUND_IMAGE_TILE_BORDER_IN_PX = 9;

	/**
	 * Total with of tiles with border in background image;
	 */
	private final static int IMAGE_TILE_WIDTH_IN_PX = GamePanelView.TILE_WIDTH_IN_PX
			+ BACKGROUND_IMAGE_TILE_BORDER_IN_PX;

	/**
	 * Expected background image width in pixels.
	 */
	private final static int EXPECTED_IMAGE_WIDTH = IMAGE_TILE_WIDTH_IN_PX * TILE_WIDTH_COUNT;

	/**
	 * Expected background image height in pixels.
	 */
	private final static int EXPECTED_IMAGE_HEIGHT = IMAGE_TILE_WIDTH_IN_PX * TILE_HEIGHT_COUNT;

	@Override
	public void preload(final ImageProvider imageProvider) {
		final Image img = ImageProvider.getRawImage(ImageProvider.BACKGROUND_IMAGE_NAME);
		Preconditions.checkState(img.getWidth() == EXPECTED_IMAGE_WIDTH,
				"Background image width is %s but expected is %s.", String.valueOf(img.getWidth()),
				EXPECTED_IMAGE_WIDTH);
		Preconditions.checkState(img.getHeight() == EXPECTED_IMAGE_HEIGHT,
				"Background image height is %s but expected is %s.", String.valueOf(img.getHeight()),
				EXPECTED_IMAGE_HEIGHT);
		imageProvider.registerImage(ImageProvider.BACKGROUND_IMAGE_NAME, img);

		for (int y = 0; y < TILE_HEIGHT_COUNT; y++) {
			for (int x = 0; x < TILE_WIDTH_COUNT; x++) {
				final String name = "type" + x + y;
				final PixelReader reader = img.getPixelReader();
				final WritableImage tile = new WritableImage(reader, x * IMAGE_TILE_WIDTH_IN_PX,
						y * IMAGE_TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
				imageProvider.registerImage(name, tile);
			}
		}

		imageProvider.registerImage(ImageProvider.IMG_TREE_1, imageProvider.getImage("type34"));
		imageProvider.registerImage(ImageProvider.IMG_TREE_2, imageProvider.getImage("type44"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_1, imageProvider.getImage("type50"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_2, imageProvider.getImage("type60"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_3, imageProvider.getImage("type70"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_GRASSLAND, imageProvider.getImage("type10"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_1, imageProvider.getImage("type30"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_2, imageProvider.getImage("type40"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_1, imageProvider.getImage("type04"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_2, imageProvider.getImage("type14"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_HIGH_SEA, imageProvider.getImage("type00"));
		imageProvider.registerImage(ImageProvider.IMG_UNIT_SHIP_GALEON, imageProvider.getImage("type45"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_TOWN, imageProvider.getImage("type05"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_1, imageProvider.getImage("type65"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_2, imageProvider.getImage("type75"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_1, imageProvider.getImage("type54"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_2, imageProvider.getImage("type64"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_1, imageProvider.getImage("type80"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_2, imageProvider.getImage("type90"));
		imageProvider.registerImage(ImageProvider.IMG_TILE_HIDDEN, imageProvider.getImage("type103"));
	}

}
