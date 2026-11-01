package org.microcol.gui.screen.game.components;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.WorldMap;
import org.microcol.model.campaign.po.GameModelPoDao;
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
            final double scale = Math.min(
                    (double) GameModelPoDao.MAX_PREVIEW_WIDTH / size.getX(),
                    (double) GameModelPoDao.MAX_PREVIEW_HEIGHT / size.getY());
            final int width = Math.max(1,
                    Math.min(GameModelPoDao.MAX_PREVIEW_WIDTH,
                            (int) Math.floor(size.getX() * scale)));
            final int height = Math.max(1,
                    Math.min(GameModelPoDao.MAX_PREVIEW_HEIGHT,
                            (int) Math.floor(size.getY() * scale)));
            final BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                final int row = Math.min(size.getY() - 1, (int) (y / scale));
                for (int x = 0; x < width; x++) {
                    final int column = Math.min(size.getX() - 1,
                            (int) (x / scale));
                    image.setRGB(x, y,
                            rgb(MinimapTiles.COLORS[tiles[row * size.getX()
                                    + column]]));
                }
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException | RuntimeException unavailable) {
            // A save without its supplementary image is still complete.
            LOGGER.warn("Unable to draw the saved-game preview", unavailable);
            return null;
        }
    }

    private static int rgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }
}
