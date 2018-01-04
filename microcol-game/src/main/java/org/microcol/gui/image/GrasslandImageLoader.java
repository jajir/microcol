package org.microcol.gui.image;

import org.microcol.gui.gamepanel.Connector;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;

/**
 * Define all map tiles.
 * <p>
 * Class should be singleton.
 * </p>
 */
public class GrasslandImageLoader implements ImageLoader {

	@Override
	public void preload(final ImageProvider imageProvider) {
		
		imageProvider.registerImage("well", imageProvider.getImage("type00"));

		// U-shape
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '3', getImg(imageProvider, "type01"));
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '2', getImg(imageProvider, "type11"));
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '3',
				getImg(imageProvider, "u-shapeNorth-02").getImageReverseRows());
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '2', getImg(imageProvider, "type21"));

		// L-shape
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', '9',
				getImg(imageProvider, "type31"));
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', '9',
				getImg(imageProvider, "type41"));
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', 'a',
				getImg(imageProvider, "l-shapeSouthEast-29").getImageTranspose());
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', 'a',
				getImg(imageProvider, "type51"));

		// I-shape
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '3', getImg(imageProvider, "type61"));
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '4', getImg(imageProvider, "type71"));
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '3',
				getImg(imageProvider, "i-shapeNorth-04").getImageReverseRows());
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '4', getImg(imageProvider, "type81"));

		// II-shape
		generateII_shape(imageProvider, Connector.of('0'), Connector.of('3'));
		generateII_shape(imageProvider, Connector.of('0'), Connector.of('4'));
		generateII_shape(imageProvider, Connector.of('b'), Connector.of('3'));
		generateII_shape(imageProvider, Connector.of('b'), Connector.of('4'));
	}
	
	private void generateII_shape(final ImageProvider imageProvider, final Connector start, final Connector end) {
		final String name1 = "ii-shapeNorthSouth-" + start.get() + end.get();
		final String name2 = "ii-shapeEastWest-" + start.rotateRight().get() + end.rotateRight().get();
		final String imagName = "i-shapeNorth-" + start.get() + end.get();

		TileDef.of(name1, Connector.of('6'), Connector.of('9'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, "i-shapeSouth-69")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('5'), Connector.of('9'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, "i-shapeSouth-59")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('6'), Connector.of('a'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, "i-shapeSouth-6a")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('5'), Connector.of('a'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, "i-shapeSouth-5a")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
	}

	private void addCycle(final ImageProvider imageProvider, final String direction1, final String direction2, final String direction3,
			final String direction4, final char start, final char end, final ImageWrapper imageWrapper) {
		TileDef.of(direction1, Connector.of(start), Connector.of(end), imageWrapper).storeTo(imageProvider)
				.rotateRight(direction2).storeTo(imageProvider).rotateRight(direction3).storeTo(imageProvider).rotateRight(direction4)
				.storeTo(imageProvider);
	}

	private ImageWrapper getImg(final ImageProvider imageProvider, final String name) {
		return ImageWrapper.of(imageProvider.getImage(name));
	}

	/**
	 * Get tile image for it's code.
	 * 
	 * @param code
	 *            required tile code.
	 * @return When there is tile image for given code image is returned when
	 *         there is not image for code <code>null</code> is returned.
	 */
	public Image getImage(final ImageProvider imageProvider, final String code) {
		Preconditions.checkNotNull(code);
		return imageProvider.getImage(code);
	}

}
