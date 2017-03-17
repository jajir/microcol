package org.microcol.gui;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.microcol.model.Terrain;

import com.google.common.collect.ImmutableMap;

/**
 * Provide image instances.
 * 
 * @author jan
 *
 */
public class ImageProvider {

	public final static String IMG_CURSOR_GOTO = "cursor-goto.png";

	public final static String IMG_ICON_STEPS_25x25 = "icon-steps-25x25.png";

	public final static String IMG_ICON_STEPS_TURN_25x25 = "icon-steps-turn-25x25.png";

	public final static String IMG_SPLASH_SCREEN = "splash-screen.png";

	public final static String IMG_TILE_OCEAN = "tile-ocean.png";

	public final static String IMG_TILE_LAND = "tile-land.png";

	public final static String IMG_TILE_SHIP1 = "tile-ship1.png";

	public final static String IMG_TILE_SHIP2 = "tile-ship2.png";

	public final static String IMG_TILE_MODE_GOTO = "tile-mode-goto.png";

	public final static String IMG_TILE_MODE_FORTIFY = "tile-mode-fortify.png";

	public final static String IMG_TILE_MODE_PLOW = "tile-mode-plow.png";

	public final static String IMG_TILE_MODE_ROAD = "tile-mode-road.png";

	private final static String BASE_PACKAGE = "images";

	private final Map<String, BufferedImage> images;

	private final Map<Terrain, Image> terrainMap = ImmutableMap.<Terrain, Image>builder()
			.put(Terrain.CONTINENT, getRawImage(IMG_TILE_LAND)).put(Terrain.OCEAN, getRawImage(IMG_TILE_OCEAN)).build();

	public ImageProvider() {
		images = new HashMap<>();
	}

	/**
	 * Load image for name.
	 * 
	 * @param name
	 *            required image name
	 * @return loaded image
	 */
	public Image getImage(final String name) {
		BufferedImage img = images.get(name);
		if (img == null) {
			img = ImageProvider.getRawImage(name);
			if (img == null) {
				return null;
			} else {
				images.put(name, img);
			}
		}
		return img;
	}

	/**
	 * This should improve image draw speed.
	 *
	 * @param image
	 *            required image
	 * @return improved image
	 */
	@SuppressWarnings("unused")
	private BufferedImage getCompatibleImage(final BufferedImage image) {
		// obtain the current system graphical settings
		GraphicsConfiguration gfx_config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		/*
		 * if image is already compatible and optimized for current system
		 * settings, simply return it
		 */
		if (image.getColorModel().equals(gfx_config.getColorModel()))
			return image;

		// image is not optimized, so create a new image that is
		BufferedImage new_image = gfx_config.createCompatibleImage(image.getWidth(), image.getHeight(),
				image.getTransparency());

		// get the graphics context of the new image to draw the old image on
		Graphics2D g2d = (Graphics2D) new_image.getGraphics();

		// actually draw the image and dispose of context no longer needed
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		// return the new optimized image
		return new_image;
	}

	/**
	 * Simplify loading image from resource. Path should look like: <code>
	 * org/microcol/images/unit-60x60.gif
	 * </code>. Class suppose that all images are in directory <i>images</i>.
	 * 
	 * @param path
	 *            path at classpath where is stored image
	 * @return image object
	 */
	public static BufferedImage getRawImage(final String rawPath) {
		final String path = BASE_PACKAGE + "/" + rawPath;
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

	/**
	 * For specific terrain type find corresponding image.
	 * 
	 * @param terrain
	 *            required terrain type
	 * @return image representing terrain image
	 */
	public Image getTerrainImage(final Terrain terrain) {
		return terrainMap.get(terrain);
	}

}
