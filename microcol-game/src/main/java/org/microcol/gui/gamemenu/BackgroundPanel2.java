package org.microcol.gui.gamemenu;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;

import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class BackgroundPanel2 extends BackgroundPanel {

    private final static String IMG_TOP = "sunset-top-line.png";

    private final static String IMG_BOTTOM = "sunset-bottom-line.png";

    private final Image imageTop;

    private final Image imageBottom;

    private final static int PAINT_FROM_TOP_OF_SUNSET = 105;

    private final static int PAINT_FROM_BOTTOM_OF_SUNSET = 65;

    private final static int GAP_BETWEEN_STRIPES = 10;

    @Inject
    public BackgroundPanel2(final ImageProvider imageProvider) {
        super(imageProvider);
        imageTop = imageProvider.getImage(IMG_TOP);
        imageBottom = imageProvider.getImage(IMG_BOTTOM);
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Image image = getImageProvider().getImage(ImageProvider.IMG_SUNSET);
        final Point sunset = Point.of(image.getWidth(), image.getHeight());
        final Point canvas = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        final Point diff = canvas.substract(sunset).divide(2);
        if (canvas.getX() > 10000 || canvas.getY() > 10000) {
            return;
        }

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getX(), canvas.getY());

        if (diff.getX() > 0) {
            final int topLine = diff.getY();
            for (int cx = 0; cx < diff.getX(); cx += imageTop.getWidth()) {
                // left
                gc.drawImage(imageTop, diff.getX() - cx - imageTop.getWidth() + 4, topLine + 68);
                // right
                gc.drawImage(imageTop, diff.getX() + sunset.getX() + cx - 4, topLine + 68);
            }

            final int botomLine = diff.getY() + sunset.getY();
            for (int cx = 0; cx < diff.getX(); cx += imageBottom.getWidth()) {
                // left
                gc.drawImage(imageBottom, diff.getX() - cx - imageBottom.getWidth() + 4,
                        botomLine - 95);
                // right
                gc.drawImage(imageBottom, diff.getX() + sunset.getX() + cx - 4, botomLine - 95);
            }
        }

        // height is drawn always
        {
            // paint top
            final int topHeight = diff.getY() + PAINT_FROM_TOP_OF_SUNSET - GAP_BETWEEN_STRIPES;
            final StripePainter sp1 = new StripePainter(gc, canvas.getX(), Color.web("#d2d1ee"));
            sp1.setStartAtY(0);
            sp1.setHeight(topHeight);
            sp1.paintStripe();

            // paint middle
            final int middleHeight = sunset.getY() - PAINT_FROM_BOTTOM_OF_SUNSET
                    - PAINT_FROM_TOP_OF_SUNSET - GAP_BETWEEN_STRIPES * 2;
            final StripePainter sp2 = new StripePainter(gc, canvas.getX(), Color.web("#9babe0"));
            sp2.setStartAtY(topHeight + GAP_BETWEEN_STRIPES * 2);
            sp2.setHeight(middleHeight);
            sp2.paintStripe();

            // paint bottom
            final int bottomHeight = diff.getY() + PAINT_FROM_BOTTOM_OF_SUNSET
                    - GAP_BETWEEN_STRIPES;
            final StripePainter sp3 = new StripePainter(gc, canvas.getX(), Color.web("#b0cb1f"));
            sp3.setStartAtY(canvas.getY() - diff.getY() - PAINT_FROM_BOTTOM_OF_SUNSET
                    + GAP_BETWEEN_STRIPES);
            sp3.setHeight(bottomHeight);
            sp3.paintStripe();
        }

        super.paint(gc);
    }

}
