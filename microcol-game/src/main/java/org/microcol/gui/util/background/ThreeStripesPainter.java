package org.microcol.gui.util.background;

import org.microcol.gui.Point;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Paint three horizontal stripes of different color and size. Stripes fill
 * whole screen width. Top and bottom stripes fill screen horizontally.
 */
public class ThreeStripesPainter {

    private final ThreeStripesPref pref;

    public ThreeStripesPainter(final ThreeStripesPref pref) {
        this.pref = Preconditions.checkNotNull(pref);
    }

    public void paint(final GraphicsContext gc, final Point canvasSize) {
        final Point canvasCenter = canvasSize.divide(2);

        // paint top
        final StripePainter sp1 = new StripePainter(gc, canvasSize.getX(),
                pref.getTopStripe().getColor());
        sp1.setStartAtY(0);
        sp1.setHeight(canvasCenter.getY() + pref.getTopStripe().getShift());
        sp1.paintStripe();

        // paint center
        final StripePainter sp2 = new StripePainter(gc, canvasSize.getX(),
                pref.getCenterStripe().getColor());
        sp2.setStartAtY(canvasCenter.getY() - pref.getCenterStripeHeight() / 2
                + pref.getCenterStripe().getShift());
        sp2.setHeight(pref.getCenterStripeHeight());
        sp2.paintStripe();

        // paint bottom
        final StripePainter sp3 = new StripePainter(gc, canvasSize.getX(),
                pref.getBottomStripe().getColor());
        sp3.setStartAtY(canvasCenter.getY() + pref.getBottomStripe().getShift());
        sp3.setHeight(canvasSize.getY() - canvasCenter.getY() + pref.getBottomStripe().getShift());
        sp3.paintStripe();
    }

}
