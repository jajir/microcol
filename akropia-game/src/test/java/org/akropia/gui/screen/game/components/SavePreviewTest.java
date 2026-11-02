package org.akropia.gui.screen.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;

import javafx.scene.paint.Color;

/** The saved map preview shows only the human player's knowledge (R-0382). */
class SavePreviewTest {

    @Test
    void previewKeepsTheLimitsAndHidesUnexploredSquares() throws Exception {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player human = model.getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow();
        final Location size = model.getMap().getMapSize();

        final byte[] png = SavePreview.render(model);

        assertNotNull(png);
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertTrue(image.getWidth() <= GameModelPoDao.MAX_PREVIEW_WIDTH);
        assertTrue(image.getHeight() <= GameModelPoDao.MAX_PREVIEW_HEIGHT);
        final double scale = (double) image.getWidth() / size.getX();
        assertEquals(size.getY() * scale, image.getHeight(), scale + 1,
                "The preview keeps the map aspect ratio");
        int hidden = 0;
        for (int y = 1; y <= size.getY(); y++) {
            for (int x = 1; x <= size.getX(); x++) {
                if (!human.isVisible(Location.of(x, y))) {
                    hidden++;
                    final int rgb = image.getRGB((int) ((x - 1 + .5) * scale),
                            (int) ((y - 1 + .5) * scale)) & 0xffffff;
                    assertEquals(rgb(MinimapTiles.COLORS[0]), rgb,
                            "Unexplored " + x + "," + y + " stays fog");
                }
            }
        }
        assertTrue(hidden > 0, "The fixture has unexplored squares");
    }

    private static int rgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }
}
