package org.akropia.gui.screen.game.components;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.WorldMap;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.paint.Color;

/**
 * Map preview of a saved game (R-0382): the human player's explored terrain in
 * the minimap palette, without units, colonies or open dialogs. It is drawn
 * without the JavaFX scene, so recovery points saved outside the FX thread have
 * it too.
 */
public final class SavePreview {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SavePreview.class);

    private SavePreview() {
    }

    /**
     * @param model required state being saved
     * @return PNG within the preview limits keeping the map aspect ratio, or
     *         <code>null</code> when it cannot be drawn
     */
    public static byte[] render(final Model model) {
        try {
            final Player player = model.getPlayers().stream()
                    .filter(Player::isHuman).findFirst().orElseThrow();
            final WorldMap world = model.getMap();
            final Location size = world.getMapSize();
            final byte[] tiles = MinimapTiles.knownTiles(player, world);
            final BufferedImage image = renderTiles(tiles, size);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException | RuntimeException unavailable) {
            // A save without its supplementary image is still complete.
            LOGGER.warn("Unable to draw the saved-game preview", unavailable);
            return null;
        }
    }

    static BufferedImage renderTiles(final byte[] tiles, final Location size) {
        final int width = GameModelPoDao.MAX_PREVIEW_WIDTH;
        final int height = GameModelPoDao.MAX_PREVIEW_HEIGHT;
        final BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        int left = size.getX(), top = size.getY(), right = -1, bottom = -1;
        for (int y = 0; y < size.getY(); y++) {
            for (int x = 0; x < size.getX(); x++) {
                if (tiles[y * size.getX() + x] != 0) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        final int background = rgb(
                right < 0 ? MinimapTiles.COLORS[0] : Color.web("#fff9ec"));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, background);
            }
        }
        if (right < 0) {
            return image;
        }
        final int columns = right - left + 1;
        final int rows = bottom - top + 1;
        final double scale = Math.min((double) width / columns,
                (double) height / rows);
        final int drawnWidth = Math.max(1, (int) Math.round(columns * scale));
        final int drawnHeight = Math.max(1, (int) Math.round(rows * scale));
        final int offsetX = (width - drawnWidth) / 2;
        final int offsetY = (height - drawnHeight) / 2;
        for (int y = 0; y < drawnHeight; y++) {
            final int sourceY = top + Math.min(rows - 1, (int) (y / scale));
            for (int x = 0; x < drawnWidth; x++) {
                final int sourceX = left
                        + Math.min(columns - 1, (int) (x / scale));
                image.setRGB(offsetX + x, offsetY + y,
                        rgb(MinimapTiles.COLORS[tiles[sourceY * size.getX()
                                + sourceX]]));
            }
        }
        return image;
    }

    private static int rgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }
}
