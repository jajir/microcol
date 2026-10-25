package org.microcol.gui.image;

/** The 07B toolbar shares exactly the same geometry in both seasons. */
final class ImageLoaderGameToolbar extends AbstractTiledImageLoader {
    ImageLoaderGameToolbar() {
        super(TileImageParams.makeBuilder().setTileWidthCount(12)
                .setTileHeightCount(2).setTileWidth(96).build(),
                "game-toolbar.png", "gameToolbar_");
    }

    @Override
    public void preload(final ImageCache imageCache) {
        super.preload(imageCache);
        final String[] ids = { "coloniesMenu", "congress", "center",
                "statistics", "turnReport", "goals", "help", "europe",
                "nextTurn", "retire", "exit", "declareIndependence" };
        for (int column = 0; column < ids.length; column++) {
            for (int row = 0; row < 2; row++) {
                imageCache.registerImage(
                        "gameToolbar_" + ids[column]
                                + (row == 1 ? "_winter" : "_summer"),
                        getImagePrefix() + column + "_" + row);
            }
        }
    }
}
