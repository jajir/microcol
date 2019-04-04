package org.microcol.gui.image;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Images are not stored one per file but multiple images is in one file. PNG
 * file is split into square images. Class describe position and sizes of images
 * in main big image.
 */
class TileImageParams {

    /**
     * How many tiles will be loaded in each row.
     */
    private final int tileWidthCount;

    /**
     * How many tiles will be loaded in each column.
     */
    private final int tileHeightCount;

    /**
     * Tiles in background image are separated with strip. This strips prevent
     * interfering of tiles. Without it anti-aliasing mixed colors of adjacent
     * tiles together.
     */
    private final int tileBorderWidth;

    /**
     * Tile Width in pixels.
     */
    private final int tileWidth;

    static class TileImageParamsBuilder {

        private int tileWidthCount;
        private int tileHeightCount;
        private int tileBorderWidth;
        private int tileWidth;

        TileImageParams build() {
            return new TileImageParams(tileWidthCount, tileHeightCount, tileBorderWidth, tileWidth);
        }

        /**
         * @param tileWidthCount
         *            the tileWidthCount to set
         */
        public TileImageParamsBuilder setTileWidthCount(int tileWidthCount) {
            this.tileWidthCount = tileWidthCount;
            return this;
        }

        /**
         * @param tileHeightCount
         *            the tileHeightCount to set
         */
        public TileImageParamsBuilder setTileHeightCount(int tileHeightCount) {
            this.tileHeightCount = tileHeightCount;
            return this;
        }

        /**
         * @param tileBorderWidth
         *            the backgroundImageTileBorderInPx to set
         */
        public TileImageParamsBuilder setTileBorderWidth(int tileBorderWidth) {
            this.tileBorderWidth = tileBorderWidth;
            return this;
        }

        /**
         * @param tileWidth
         *            the tileWidth to set
         */
        public TileImageParamsBuilder setTileWidth(int tileWidth) {
            this.tileWidth = tileWidth;
            return this;
        }
    }

    public static TileImageParamsBuilder makeBuilder() {
        return new TileImageParamsBuilder();
    }

    TileImageParams(final int tileWidthCount, final int tileHeightCount, final int tileBorderWidth,
            final int tileWidth) {
        Preconditions.checkArgument(tileWidthCount != 0, "Parameter tileWidthCount can't be 0");
        Preconditions.checkArgument(tileHeightCount != 0, "Parameter tileHeightCount can't be 0");
        Preconditions.checkArgument(tileWidth != 0, "Parameter tileWidth can't be 0");
        this.tileWidthCount = tileWidthCount;
        this.tileHeightCount = tileHeightCount;
        this.tileBorderWidth = tileBorderWidth;
        this.tileWidth = tileWidth;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TileImageParams.class)
                .add("tileWidthCount", tileWidthCount).add("tileHeightCount", tileHeightCount)
                .add("tileBorderWidth", tileBorderWidth).add("tileWidth", tileWidth).toString();
    }

    /**
     * @return the tileWidthCount
     */
    public int getTileWidthCount() {
        return tileWidthCount;
    }

    /**
     * @return the tileHeightCount
     */
    public int getTileHeightCount() {
        return tileHeightCount;
    }

    /**
     * @return the backgroundImageTileBorderInPx
     */
    public int getTileBorderWidth() {
        return tileBorderWidth;
    }

    /**
     * @return the tileWidth
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Total with of tiles with border in background image;
     * 
     * @return
     */
    public int getTileWidthIncludingBorder() {
        return getTileWidth() + getTileBorderWidth();
    }

    /**
     * Expected background image width in pixels.
     * 
     * @return
     */
    public int getExpectedImageWidth() {
        return getTileWidthIncludingBorder() * getTileWidthCount();
    }

    /**
     * Expected background image height in pixels.
     * 
     * @return
     */
    public int getExpectedImageHeight() {
        return getTileWidthIncludingBorder() * getTileHeightCount();
    }

}