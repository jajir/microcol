package org.microcol.gui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.microcol.gui.image.ImageProvider;

import javafx.scene.image.Image;

public class ImageProviderTest {

	@Test
	public void test_existingImage() throws Exception {
		ImageProvider imageProvider = new ImageProvider();
		Image i1 = imageProvider.getImage("cursor-goto.png");
		assertNotNull(i1);
	}

	@Test
	public void test_two_getImage_returns_same_image_instance() throws Exception {
		ImageProvider imageProvider = new ImageProvider();
		Image i1 = imageProvider.getImage("cursor-goto.png");
		Image i2 = imageProvider.getImage("cursor-goto.png");
		assertNotNull(i1);
		assertSame(i1, i2);
	}

	@Test(expected = MicroColException.class)
	public void test_load_non_existing_image() throws Exception {
		ImageProvider imageProvider = new ImageProvider();
		imageProvider.getImage("doesn't exists.png");
	}

}
