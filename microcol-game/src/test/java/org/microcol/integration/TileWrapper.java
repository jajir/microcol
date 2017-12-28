package org.microcol.integration;

import org.microcol.gui.gamepanel.GamePanelView;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class TileWrapper {

	private final Image tile;

	private TileWrapper(final Image tile) {
		this.tile = Preconditions.checkNotNull(tile);
	}

	public final static TileWrapper of(final Image tile) {
		return new TileWrapper(tile);
	}

	public TileWrapper getImageRotareRight() {
		return getImageTranspose().getImageReverseRows();
	}

	public TileWrapper getImageReverseRows() {
		return getImageTransform(this, (pixelWriter, pixelReader, x, y) -> {
			final Color color = pixelReader.getColor(x, y);
			pixelWriter.setColor(GamePanelView.TILE_WIDTH_IN_PX - x - 1, y, color);
		});
	}

	public TileWrapper getImageTranspose() {
		return getImageTransform(this, (pixelWriter, pixelReader, x, y) -> {
			final Color color = pixelReader.getColor(x, y);
			pixelWriter.setColor(y, x, color);
		});
	}

	private TileWrapper getImageTransform(final TileWrapper tileWrapper, final Tranform tranform) {
		PixelReader pixelReader = tileWrapper.get().getPixelReader();

		int width = GamePanelView.TILE_WIDTH_IN_PX;
		int height = GamePanelView.TILE_WIDTH_IN_PX;

		// process from source to destination pixel by pixel
		WritableImage writableImage = new WritableImage(width, height);
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tranform.apply(pixelWriter, pixelReader, x, y);
			}
		}

		return TileWrapper.of(writableImage);
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
