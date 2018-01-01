package org.microcol.gui.gamepanel;

import java.util.HashMap;
import java.util.Map;

import org.microcol.gui.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Define all map images.
 * <p>
 * Class should be singleton.
 * </p>
 */
public class MapImageStore {

	private final int TILE_MAX_X = 3;

	private final int TILE_MAX_Y = 4;

	private final static String BACKGROUND_IMAGE_NAME = "backgroud.png";

	private final Map<String, Image> images = new HashMap<>();

	@Inject
	MapImageStore() {
		init();
	}

	private void init() {
		final Image img = ImageProvider.getRawImage(BACKGROUND_IMAGE_NAME);
		for (int y = 0; y < TILE_MAX_Y; y++) {
			for (int x = 0; x < TILE_MAX_X; x++) {
				final String name = "type" + x + y;
				final PixelReader reader = img.getPixelReader();
				WritableImage tile = new WritableImage(reader, x * GamePanelView.TILE_WIDTH_IN_PX,
						y * GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX,
						GamePanelView.TILE_WIDTH_IN_PX);
				images.put(name, tile);
			}
		}

		images.put("well", images.get("type00"));

		// U-shape
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '3', getImg("type01"));
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '2', getImg("type11"));
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '3',
				getImg("u-shapeNorth-02").getImageReverseRows());
		addCycle("u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '2', getImg("type21"));

		// L-shape
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', '9',
				getImg("type02"));
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', '9',
				getImg("type12"));
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', 'a',
				getImg("l-shapeSouthEast-29").getImageTranspose());
		addCycle("l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', 'a',
				getImg("type22"));

		// I-shape
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '3', getImg("type03"));
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '4', getImg("type13"));
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '3',
				getImg("i-shapeNorth-04").getImageReverseRows());
		addCycle("i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '4', getImg("type23"));

		// II-shape
		generateII_shape(Connector.of('0'), Connector.of('3'));
		generateII_shape(Connector.of('0'), Connector.of('4'));
		generateII_shape(Connector.of('b'), Connector.of('3'));
		generateII_shape(Connector.of('b'), Connector.of('4'));

	}

	private void generateII_shape(final Connector start, final Connector end) {
		final String name1 = "ii-shapeNorthSouth-" + start.get() + end.get();
		final String name2 = "ii-shapeEastWest-" + start.rotateRight().get() + end.rotateRight().get();
		final String imagName = "i-shapeNorth-" + start.get() + end.get();

		TileDef.of(name1, Connector.of('6'), Connector.of('9'), getImg(imagName).addImage(getImg("i-shapeSouth-69")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('5'), Connector.of('9'), getImg(imagName).addImage(getImg("i-shapeSouth-59")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('6'), Connector.of('a'), getImg(imagName).addImage(getImg("i-shapeSouth-6a")))
				.storeTo(images).rotateRight(name2).storeTo(images);
		TileDef.of(name1, Connector.of('5'), Connector.of('a'), getImg(imagName).addImage(getImg("i-shapeSouth-5a")))
				.storeTo(images).rotateRight(name2).storeTo(images);
	}

	private void addCycle(final String direction1, final String direction2, final String direction3,
			final String direction4, final char start, final char end, final ImageWrapper imageWrapper) {
		TileDef.of(direction1, Connector.of(start), Connector.of(end), imageWrapper).storeTo(images)
				.rotateRight(direction2).storeTo(images).rotateRight(direction3).storeTo(images).rotateRight(direction4)
				.storeTo(images);
	}

	private ImageWrapper getImg(final String name) {
		return ImageWrapper.of(images.get(name));
	}

	/**
	 * Get tile image for it's code.
	 * 
	 * @param code
	 *            required tile code.
	 * @return When there is tile image for given code image is returned when
	 *         there is not image for code <code>null</code> is returned.
	 */
	public Image getImage(final String code) {
		Preconditions.checkNotNull(code);
		return images.get(code);
	}

}
