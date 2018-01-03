package org.microcol.integration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.microcol.gui.image.ImageProvider;

import com.google.common.io.Files;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ImageProviderTest {

	@Test
	public void testName() throws Exception {
		ImageProvider mapImageStore = new ImageProvider();
		
		for (final String key : mapImageStore.getTileNames()) {
			final Image image = mapImageStore.getImage(key);
			final String path = "target/img/" + key + ".png";
			Files.createParentDirs(new File(path));
			saveToFile(image, path);
		}
		
	}

	public static void saveToFile(final Image image, String filePath) {
		final File outputFile = new File(filePath);
		final BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
