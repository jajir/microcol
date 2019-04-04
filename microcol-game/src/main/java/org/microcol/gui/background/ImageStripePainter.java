package org.microcol.gui.background;

import org.microcol.gui.Point;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Paint horizontal stripe from repeating images. Stripe skip central pixels.
 */
public class ImageStripePainter {

    private final ImageStripePref pref;

    public ImageStripePainter(final ImageStripePref pref) {
        this.pref = Preconditions.checkNotNull(pref);
    }

    public void paint(final GraphicsContext gc, final Point canvasSize) {
        final Point center = canvasSize.divide(2);
        final int y = center.getY() + pref.getVerticalShift();
        final int widthToPaint = center.getX() - pref.getCenterGap() / 2;
        final int paintedImageWidth = pref.getImageWidth();

        for (int cx = 0; cx < widthToPaint; cx += paintedImageWidth) {

            // left part of stripe
            final int leftEdgeImageStart = center.getX() - pref.getCenterGap() / 2
                    - paintedImageWidth;
            final int leftX = leftEdgeImageStart - cx;
            gc.drawImage(pref.getImage(), leftX, y);

            // right part of stripe
            final int rightEdgeImageStart = center.getX() + pref.getCenterGap() / 2;
            final int rightX = rightEdgeImageStart + cx;
            gc.drawImage(pref.getImage(), rightX, y);
        }

    }

    public void paintLeft(final GraphicsContext gc, final Point canvasSize) {
        final Point center = canvasSize.divide(2);
        final int y = center.getY() + pref.getVerticalShift();
        final int widthToPaint = center.getX() - pref.getCenterGap() / 2;
        final int paintedImageWidth = pref.getImageWidth();

        for (int cx = 0; cx < widthToPaint; cx += paintedImageWidth) {
            // left part of stripe
            final int leftEdgeImageStart = center.getX() - pref.getCenterGap() / 2
                    - paintedImageWidth;
            final int leftX = leftEdgeImageStart - cx;
            gc.drawImage(pref.getImage(), leftX, y);

        }

    }

    public void paintRight(final GraphicsContext gc, final Point canvasSize) {
        final Point center = canvasSize.divide(2);
        final int y = center.getY() + pref.getVerticalShift();
        final int widthToPaint = center.getX() - pref.getCenterGap() / 2;
        final int paintedImageWidth = pref.getImageWidth();

        for (int cx = 0; cx < widthToPaint; cx += paintedImageWidth) {
            // right part of stripe
            final int rightEdgeImageStart = center.getX() + pref.getCenterGap() / 2;
            final int rightX = rightEdgeImageStart + cx;
            gc.drawImage(pref.getImage(), rightX, y);
        }

    }
    
}
