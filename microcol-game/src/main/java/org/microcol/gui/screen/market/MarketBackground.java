package org.microcol.gui.screen.market;

import org.microcol.gui.GuiColors;
import org.microcol.gui.Point;
import org.microcol.gui.background.AbstractAnimatedBackground;
import org.microcol.gui.background.ImageStripePainter;
import org.microcol.gui.background.ImageStripePref;
import org.microcol.gui.background.ThreeStripesPainter;
import org.microcol.gui.background.ThreeStripesPref;
import org.microcol.gui.background.ThreeStripesPref.StripeDef;
import org.microcol.gui.image.ImageLoaderExtra;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MarketBackground extends AbstractAnimatedBackground {

    private final static String IMG_TOP = "market-top.png";

    private final static String IMG_BOTTOM = "market-bottom.png";

    private final Image imageTop;

    private final Image imageBottom;

    private final Image imageCenter;

    private final ThreeStripesPainter threeStripesPainter;

    private final ImageStripePainter topImageStripePainter;

    private final ImageStripePainter bottomImageStripePainter;

    @Inject
    public MarketBackground(final ImageProvider imageProvider) {
        imageTop = Preconditions.checkNotNull(imageProvider.getImage(IMG_TOP));
        imageBottom = Preconditions.checkNotNull(imageProvider.getImage(IMG_BOTTOM));
        imageCenter = Preconditions.checkNotNull(imageProvider.getImage(ImageLoaderExtra.IMG_MARKET));
        final ThreeStripesPref pref = ThreeStripesPref.build()
                .setTopStripe(StripeDef.of(-215, GuiColors.SKY))
                .setCenterStripe(StripeDef.of(-130, GuiColors.OCEAN))
                .setBottomStripe(StripeDef.of(-50, GuiColors.GRASS))
                .setCenterStripeHeight(150)
                .make();
        this.threeStripesPainter = new ThreeStripesPainter(pref);

        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        topImageStripePainter = new ImageStripePainter(ImageStripePref.build().setImage(imageTop)
                .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-265).make());
        bottomImageStripePainter = new ImageStripePainter(
                ImageStripePref
                        .build()
                        .setImage(imageBottom)
                        .setCenterGap(centerImageSize.getX() - 10)
                        .setVerticalShift(-90)
                        .make());
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
        topImageStripePainter.paint(gc, canvasSize);
        bottomImageStripePainter.paint(gc, canvasSize);
        final Point diff = canvasSize.substract(centerImageSize).divide(2);
        gc.drawImage(imageCenter, diff.getX(), diff.getY());
    }

    @Override
    public void beforeHide() {
        super.beforeHide();
    }

    @Override
    public void beforeShow() {
        super.beforeShow();
    }

}
