package org.akropia.gui.screen.game.components;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import javafx.scene.paint.Color;

class SavePreviewTest {
    @Test
    void savedPreviewHasConstantDimensions() throws Exception {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final byte[] png = SavePreview.render(model);
        assertNotNull(png);
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(GameModelPoDao.MAX_PREVIEW_WIDTH, image.getWidth());
        assertEquals(GameModelPoDao.MAX_PREVIEW_HEIGHT, image.getHeight());
    }

    @Test
    void exploredBoundsAreCenteredWithoutRevealingFogHoles() {
        final byte[] tiles = new byte[100];
        tiles[22] = 1;
        tiles[24] = 2;
        final BufferedImage image = SavePreview.renderTiles(tiles,
                Location.of(10, 10));
        // A three-by-one explored bounding box fills 960 x 320, centered
        // vertically.
        assertEquals(rgb(Color.web("#fff9ec")),
                image.getRGB(480, 0) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[1]),
                image.getRGB(160, 270) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[0]),
                image.getRGB(480, 270) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[2]),
                image.getRGB(800, 270) & 0xffffff);
        assertEquals(rgb(Color.web("#fff9ec")),
                image.getRGB(480, 539) & 0xffffff);
    }

    @Test
    void singleKnownTileKeepsSquareAspectAndEmptyExplorationStaysUnknown() {
        final byte[] tiles = new byte[4];
        final BufferedImage unknown = SavePreview.renderTiles(tiles,
                Location.of(2, 2));
        assertEquals(rgb(MinimapTiles.COLORS[0]),
                unknown.getRGB(0, 0) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[0]),
                unknown.getRGB(959, 539) & 0xffffff);
        tiles[3] = 1;
        final BufferedImage known = SavePreview.renderTiles(tiles,
                Location.of(2, 2));
        assertEquals(rgb(Color.web("#fff9ec")),
                known.getRGB(209, 270) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[1]),
                known.getRGB(210, 270) & 0xffffff);
        assertEquals(rgb(MinimapTiles.COLORS[1]),
                known.getRGB(749, 270) & 0xffffff);
        assertEquals(rgb(Color.web("#fff9ec")),
                known.getRGB(750, 270) & 0xffffff);
    }

    private static int rgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }
}
