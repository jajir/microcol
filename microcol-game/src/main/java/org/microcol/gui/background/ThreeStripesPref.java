package org.microcol.gui.background;

import com.google.common.base.Preconditions;

import javafx.scene.paint.Color;

public class ThreeStripesPref {

    private final StripeDef topStripe;
    private final StripeDef bottomStripe;
    private final StripeDef centerStripe;
    private final int centerStripeHeight;

    private ThreeStripesPref(final StripeDef topStripe, final StripeDef bottomStripe,
            final StripeDef centerStripe, final int centerStripeHeight) {
        this.topStripe = Preconditions.checkNotNull(topStripe);
        this.bottomStripe = Preconditions.checkNotNull(bottomStripe);
        this.centerStripe = Preconditions.checkNotNull(centerStripe);
        this.centerStripeHeight = centerStripeHeight;
    }

    public static ThreeStripesPrefBuilder build() {
        return new ThreeStripesPrefBuilder();
    }

    public static class ThreeStripesPrefBuilder {

        private StripeDef topStripe;
        private StripeDef bottomStripe;
        private StripeDef centerStripe;
        private int centerStripeHeight;

        public ThreeStripesPref make() {
            final ThreeStripesPref pref = new ThreeStripesPref(topStripe, bottomStripe,
                    centerStripe, centerStripeHeight);
            return pref;
        }

        /**
         * @param topStripe
         *            the topStripe to set
         * @return builder itself
         */
        public ThreeStripesPrefBuilder setTopStripe(StripeDef topStripe) {
            this.topStripe = topStripe;
            return this;
        }

        /**
         * @param bottomStripe
         *            the bottomStripe to set
         * @return builder itself
         */
        public ThreeStripesPrefBuilder setBottomStripe(StripeDef bottomStripe) {
            this.bottomStripe = bottomStripe;
            return this;
        }

        /**
         * @param centerStripe
         *            the centerStripe to set
         * @return builder itself
         */
        public ThreeStripesPrefBuilder setCenterStripe(StripeDef centerStripe) {
            this.centerStripe = centerStripe;
            return this;
        }

        /**
         * @param centerStripeHeight
         *            the centerStripeHeight to set
         * @return builder itself
         */
        public ThreeStripesPrefBuilder setCenterStripeHeight(int centerStripeHeight) {
            this.centerStripeHeight = centerStripeHeight;
            return this;
        }

    }

    /**
     * Define stripe color and vertical shift from center of the canvas.
     */
    public static class StripeDef {

        private final int shift;

        private final Color color;

        public static StripeDef of(final int shift, final Color color) {
            return new StripeDef(shift, color);
        }

        private StripeDef(final int shift, final Color color) {
            this.shift = shift;
            this.color = Preconditions.checkNotNull(color);
        }

        /**
         * @return the shift
         */
        public int getShift() {
            return shift;
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }

    }

    /**
     * @return the topStripe
     */
    public StripeDef getTopStripe() {
        return topStripe;
    }

    /**
     * @return the bottomStripe
     */
    public StripeDef getBottomStripe() {
        return bottomStripe;
    }

    /**
     * @return the centerStripe
     */
    public StripeDef getCenterStripe() {
        return centerStripe;
    }

    /**
     * @return the centerStripeHeight
     */
    public int getCenterStripeHeight() {
        return centerStripeHeight;
    }

}
