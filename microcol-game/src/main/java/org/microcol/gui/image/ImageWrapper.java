package org.microcol.gui.image;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public final class ImageWrapper {

    private final Image tile;

    private ImageWrapper(final Image tile) {
        this.tile = Preconditions.checkNotNull(tile);
    }

    public final static ImageWrapper of(final Image tile) {
        return new ImageWrapper(tile);
    }

    public ImageWrapper getImageRotareRight() {
        return getImageTranspose().getImageReverseRows();
    }

    public ImageWrapper getImageReverseRows() {
        return getImageTransform(this, (pixelWriter, pixelReader, x, y) -> {
            final Color color = pixelReader.getColor(x, y);
            pixelWriter.setColor(TILE_WIDTH_IN_PX - x - 1, y, color);
        });
    }

    public ImageWrapper getImageTranspose() {
        return getImageTransform(this, (pixelWriter, pixelReader, x, y) -> {
            final Color color = pixelReader.getColor(x, y);
            pixelWriter.setColor(y, x, color);
        });
    }

    public ImageWrapper addImage(final ImageWrapper wrapper) {
        PixelReader pixelReaderA = get().getPixelReader();
        PixelReader pixelReaderB = wrapper.get().getPixelReader();

        // process from source to destination pixel by pixel
        WritableImage writableImage = new WritableImage(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < TILE_WIDTH_IN_PX; y++) {
            for (int x = 0; x < TILE_WIDTH_IN_PX; x++) {
                final Color colorA = pixelReaderA.getColor(x, y);
                final Color colorB = pixelReaderB.getColor(x, y);
                if (colorA.getOpacity() < colorB.getOpacity()) {
                    pixelWriter.setColor(x, y, colorB);
                } else {
                    pixelWriter.setColor(x, y, colorA);
                }
            }
        }

        return ImageWrapper.of(writableImage);
    }

    private ImageWrapper getImageTransform(final ImageWrapper tileWrapper,
            final Tranform tranform) {
        PixelReader pixelReader = tileWrapper.get().getPixelReader();

        int width = TILE_WIDTH_IN_PX;
        int height = TILE_WIDTH_IN_PX;

        // process from source to destination pixel by pixel
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tranform.apply(pixelWriter, pixelReader, x, y);
            }
        }

        return ImageWrapper.of(writableImage);
    }

    /**
     * Helps to transform image tile pixel to another one.
     */
    private interface Tranform {

        void apply(PixelWriter pixelWriter, PixelReader pixelReader, int x, int y);

    }

    public Image get() {
        return tile;
    }

}
