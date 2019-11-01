package org.microcol.gui.screen.europe;

import org.microcol.gui.ColorScheme;
import org.microcol.gui.GuiColors;
import org.microcol.gui.Point;
import org.microcol.gui.background.AbstractBackground;
import org.microcol.gui.background.ImageStripePainter;
import org.microcol.gui.background.ImageStripePref;
import org.microcol.gui.background.ThreeStripesPainter;
import org.microcol.gui.background.ThreeStripesPref;
import org.microcol.gui.background.ThreeStripesPref.StripeDef;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

@Singleton
public class EuropeBackground extends AbstractBackground {

    private final static String IMG_LEFT = "europe-left.png";
    private final static String IMG_RIGHT = "europe-right.png";
    private final static String IMG_CENTER = "europe.png";

    private final Image imageLeft;
    private final Image imageRight;
    private final Image imageCenter;

    private final ThreeStripesPainter threeStripesPainter;

    private final ImageStripePainter leftImageStripePainter;
    private final ImageStripePainter rightImageStripePainter;

    @Inject
    public EuropeBackground(final ImageProvider imageProvider, final ColorScheme colorScheme) {
        imageLeft = Preconditions.checkNotNull(imageProvider.getImage(IMG_LEFT));
        imageRight = Preconditions.checkNotNull(imageProvider.getImage(IMG_RIGHT));
        imageCenter = Preconditions.checkNotNull(imageProvider.getImage(IMG_CENTER));
        final ThreeStripesPref pref = ThreeStripesPref.build()
                .setTopStripe(StripeDef.of(-280, colorScheme.getColor(GuiColors.SKY_1)))
                .setCenterStripe(StripeDef.of(30, colorScheme.getColor(GuiColors.SEA_1)))
                .setBottomStripe(StripeDef.of(290, colorScheme.getColor(GuiColors.DIRT_1)))
                .setCenterStripeHeight(460)
                .make();
        this.threeStripesPainter = new ThreeStripesPainter(pref);

        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        leftImageStripePainter = new ImageStripePainter(ImageStripePref.build().setImage(imageLeft)
                .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-312).make());
        rightImageStripePainter = new ImageStripePainter(
                ImageStripePref.build().setImage(imageRight)
                        .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-312).make());
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Point canvasSize = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        if (canvasSize.getX() > 10000 || canvasSize.getY() > 10000) {
            return;
        }
        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        paintBackground(gc, canvasSize, Color.WHITE);
        threeStripesPainter.paint(gc, canvasSize);
        leftImageStripePainter.paintLeft(gc, canvasSize);
        rightImageStripePainter.paintRight(gc, canvasSize);
        final Point diff = canvasSize.substract(centerImageSize).divide(2);
        gc.drawImage(imageCenter, diff.getX(), diff.getY());
    }

}
