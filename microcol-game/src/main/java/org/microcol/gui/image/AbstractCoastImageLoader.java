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
public abstract class AbstractCoastImageLoader implements ImageLoader {
	
	abstract String getTypePrefix();
	
	abstract String getBackgroundRow();
	
	@Override
	public void preload(final ImageProvider imageProvider) {
		
		imageProvider.registerImage(getTypePrefix() + "well", imageProvider.getImage("type00"));

		// U-shape
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '3', getImgInRow(imageProvider, "0"));
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0', '2', getImgInRow(imageProvider, "1"));
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '3',
				getImg(imageProvider, getTypePrefix() + "u-shapeNorth-02").getImageReverseRows());
		addCycle(imageProvider, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1', '2', getImgInRow(imageProvider, "2"));

		// L-shape
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', '9',
				getImgInRow(imageProvider, "3"));
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', '9',
				getImgInRow(imageProvider, "4"));
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '3', 'a',
				getImg(imageProvider, getTypePrefix() + "l-shapeSouthEast-29").getImageTranspose());
		addCycle(imageProvider, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-", "l-shapeNorthEast-", '2', 'a',
				getImgInRow(imageProvider, "5"));

		// I-shape
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '3', getImgInRow(imageProvider, "6"));
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0', '4', getImgInRow(imageProvider, "7"));
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '3',
				getImg(imageProvider, getTypePrefix() + "i-shapeNorth-04").getImageReverseRows());
		addCycle(imageProvider, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b', '4', getImgInRow(imageProvider, "8"));

		// II-shape
		generateII_shape(imageProvider, Connector.of('0'), Connector.of('3'));
		generateII_shape(imageProvider, Connector.of('0'), Connector.of('4'));
		generateII_shape(imageProvider, Connector.of('b'), Connector.of('3'));
		generateII_shape(imageProvider, Connector.of('b'), Connector.of('4'));
	}
	
	private void generateII_shape(final ImageProvider imageProvider, final Connector start, final Connector end) {
		final String name1 = getTypePrefix() + "ii-shapeNorthSouth-" + start.get() + end.get();
		final String name2 = getTypePrefix() + "ii-shapeEastWest-" + start.rotateRight().get() + end.rotateRight().get();
		final String imagName = getTypePrefix() + "i-shapeNorth-" + start.get() + end.get();

		TileDef.of(name1, Connector.of('6'), Connector.of('9'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, getTypePrefix() + "i-shapeSouth-69")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('5'), Connector.of('9'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, getTypePrefix() + "i-shapeSouth-59")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('6'), Connector.of('a'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, getTypePrefix() + "i-shapeSouth-6a")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
		TileDef.of(name1, Connector.of('5'), Connector.of('a'), getImg(imageProvider, imagName).addImage(getImg(imageProvider, getTypePrefix() + "i-shapeSouth-5a")))
				.storeTo(imageProvider).rotateRight(name2).storeTo(imageProvider);
	}

	private void addCycle(final ImageProvider imageProvider, final String direction1, final String direction2, final String direction3,
			final String direction4, final char start, final char end, final ImageWrapper imageWrapper) {
		final String dir1 = getTypePrefix() + direction1;
		final String dir2 = getTypePrefix() + direction2;
		final String dir3 = getTypePrefix() + direction3;
		final String dir4 = getTypePrefix() + direction4;
		TileDef.of(dir1, Connector.of(start), Connector.of(end), imageWrapper).storeTo(imageProvider)
				.rotateRight(dir2).storeTo(imageProvider).rotateRight(dir3).storeTo(imageProvider).rotateRight(dir4)
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

	private ImageWrapper getImgInRow(final ImageProvider imageProvider, final String count) {
		return getImg(imageProvider, "type" + count + getBackgroundRow());
	}
	
}
