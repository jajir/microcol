package org.microcol.integration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.image.ModuleImages;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * This is utility class. It loads all in game images and store them into
 * {@link #TARGET_DIR} directory.
 * <p>
 * It allows to verify that images are correctly stored in image provider.
 * </p>
 */
public class ImageProviderTestUtil {

    /**
     * Here are all images stored.
     */
    private final static String TARGET_DIR = "target/img/";

    public static void main(final String[] args) {
        try {
            final ImageProviderTestUtil util = new ImageProviderTestUtil();
            util.saveAllChachedImagesToTmp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAllChachedImagesToTmp() throws Exception {

        final Injector injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                new ModuleImages());
        final ImageProvider mapImageStore = injector.getInstance(ImageProvider.class);

        for (final String key : mapImageStore.getImageNames()) {
            final Image image = mapImageStore.getImage(key);
            final String path = TARGET_DIR + key + ".png";
            Files.createParentDirs(new File(path));
            saveToFile(image, path);
        }

    }

    public void saveToFile(final Image image, String filePath) {
        final File outputFile = new File(filePath);
        final BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
