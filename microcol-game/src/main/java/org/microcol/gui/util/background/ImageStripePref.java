package org.microcol.gui.util.background;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;

public class ImageStripePref {

    private final Image image;

    private final int centerGap;

    /**
     * How much is image stripe vertically moved from center of area.
     */
    private final int verticalShift;

    public static class ImageStripeBuilder {

        private Image image;
        private int centerGap;
        private int verticalShift;

        public ImageStripePref make() {
            return new ImageStripePref(image, centerGap, verticalShift);
        }

        /**
         * @param image
         *            the image to set
         */
        public ImageStripeBuilder setImage(Image image) {
            this.image = image;
            return this;
        }

        /**
         * @param centerGap
         *            the centerGap to set
         */
        public ImageStripeBuilder setCenterGap(int centerGap) {
            this.centerGap = centerGap;
            return this;
        }

        /**
         * @param verticalShift
         *            the verticalShift to set
         */
        public ImageStripeBuilder setVerticalShift(int verticalShift) {
            this.verticalShift = verticalShift;
            return this;
        }

    }

    public static ImageStripeBuilder build() {
        return new ImageStripeBuilder();
    }

    private ImageStripePref(final Image image, final int centerGap, final int verticalShift) {
        this.image = Preconditions.checkNotNull(image);
        this.centerGap = centerGap;
        this.verticalShift = verticalShift;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    public int getImageWidth() {
        return (int) image.getWidth();
    }

    /**
     * @return the centerImageWidth
     */
    public int getCenterGap() {
        return centerGap;
    }

    /**
     * @return the verticalShift
     */
    public int getVerticalShift() {
        return verticalShift;
    }

}
