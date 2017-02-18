package org.microcol.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Provide image instances.
 * 
 * @author jan
 *
 */
public class ImageProvider {

	public final static String IMG_CURSOR_GOTO = "cursor-goto.png";

	public final static String IMG_ICON_STEPS_25x25 = "icon-steps-25x25.png";

	public final static String IMG_SPLASH_SCREEN = "splash-screen.png";

	public final static String IMG_TILE_OCEAN = "tile-ocean.png";

	public final static String IMG_TILE_SHIP1 = "tile-ship2.png";

	public final static String IMG_TILE_SHIP2 = "tile-ship2.png";

	public final static String IMG_TILE_MODE_GOTO = "tile-mode-goto.png";

	public final static String IMG_TILE_MODE_FORTIFY = "tile-mode-fortify.png";

	public final static String IMG_TILE_MODE_PLOW = "tile-mode-plow.png";

	public final static String IMG_TILE_MODE_ROAD = "tile-mode-road.png";

	private final static String BASE_PACKAGE = "images";

	private final Map<String, BufferedImage> images;

	public ImageProvider() {
		images = new HashMap<>();
	}

	/**
	 * Load image for name.
	 * 
	 * @param name
	 *            required image name
	 */
	public Image getImage(final String name) {
		BufferedImage img = images.get(name);
		if (img == null) {
			img = ImageProvider.getRawImage(BASE_PACKAGE + "/" + name);
			if (img == null) {
				return null;
			} else {
				images.put(name, img);
			}
		}
		return img;
	}

	/**
	 * Simplify loading image from resource. Path should look like: <code>
	 * org/microcol/images/unit-60x60.gif
	 * </code>
	 * 
	 * @param path
	 *            path at classpath where is stored image
	 * @return image object
	 */
	public static BufferedImage getRawImage(final String path) {
		try {
			ClassLoader cl = ImageProvider.class.getClassLoader();
			final InputStream in = cl.getResourceAsStream(path);
			if (in == null) {
				throw new MicroColException("Unable to load file '" + path + "'.");
			} else {
				return ImageIO.read(cl.getResourceAsStream(path));
			}
		} catch (IOException e) {
			throw new MicroColException("Unable to load file '" + path + "'.", e);
		}
	}

}
