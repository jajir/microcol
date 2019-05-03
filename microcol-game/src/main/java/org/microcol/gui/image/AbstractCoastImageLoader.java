package org.microcol.gui.image;

/**
 * Define all map tiles.
 * <p>
 * Class should be singleton.
 * </p>
 */
public abstract class AbstractCoastImageLoader implements ImageLoader {

    abstract String getTypePrefix();

    abstract String getBackgroundRow();

    @Override
    public void preload(final ImageCache imageCache) {

        imageCache.registerImage(getTypePrefix() + "well", getImgInRow(imageCache, "0").get());

        // U-shape
        addCycle(imageCache, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0',
                '3', getImgInRow(imageCache, "1"));
        addCycle(imageCache, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '0',
                '2', getImgInRow(imageCache, "2"));
        addCycle(imageCache, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1',
                '3', getImg(imageCache, getTypePrefix() + "u-shapeNorth-02").getImageReverseRows());
        addCycle(imageCache, "u-shapeNorth-", "u-shapeEast-", "u-shapeSouth-", "u-shapeWest-", '1',
                '2', getImgInRow(imageCache, "3"));

        // L-shape
        addCycle(imageCache, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-",
                "l-shapeNorthEast-", '3', '9', getImgInRow(imageCache, "4"));
        addCycle(imageCache, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-",
                "l-shapeNorthEast-", '2', '9', getImgInRow(imageCache, "5"));
        addCycle(imageCache, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-",
                "l-shapeNorthEast-", '3', 'a',
                getImg(imageCache, getTypePrefix() + "l-shapeSouthEast-29").getImageTranspose());
        addCycle(imageCache, "l-shapeSouthEast-", "l-shapeSouthWest-", "l-shapeNorthWest-",
                "l-shapeNorthEast-", '2', 'a', getImgInRow(imageCache, "6"));

        // I-shape
        addCycle(imageCache, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0',
                '3', getImgInRow(imageCache, "7"));
        addCycle(imageCache, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", '0',
                '4', getImgInRow(imageCache, "8"));
        addCycle(imageCache, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b',
                '3', getImg(imageCache, getTypePrefix() + "i-shapeNorth-04").getImageReverseRows());
        addCycle(imageCache, "i-shapeNorth-", "i-shapeEast-", "i-shapeSouth-", "i-shapeWest-", 'b',
                '4', getImgInRow(imageCache, "9"));

        // II-shape
        generateII_shape(imageCache, Connector.of('0'), Connector.of('3'));
        generateII_shape(imageCache, Connector.of('0'), Connector.of('4'));
        generateII_shape(imageCache, Connector.of('b'), Connector.of('3'));
        generateII_shape(imageCache, Connector.of('b'), Connector.of('4'));
    }

    private void generateII_shape(final ImageCache imageCache, final Connector start,
            final Connector end) {
        final String name1 = getTypePrefix() + "ii-shapeNorthSouth-" + start.get() + end.get();
        final String name2 = getTypePrefix() + "ii-shapeEastWest-" + start.rotateRight().get()
                + end.rotateRight().get();
        final String imagName = getTypePrefix() + "i-shapeNorth-" + start.get() + end.get();

        TileDef.of(name1, Connector.of('6'), Connector.of('9'),
                getImg(imageCache, imagName)
                        .addImage(getImg(imageCache, getTypePrefix() + "i-shapeSouth-69")))
                .storeTo(imageCache).rotateRight(name2).storeTo(imageCache);
        TileDef.of(name1, Connector.of('5'), Connector.of('9'),
                getImg(imageCache, imagName)
                        .addImage(getImg(imageCache, getTypePrefix() + "i-shapeSouth-59")))
                .storeTo(imageCache).rotateRight(name2).storeTo(imageCache);
        TileDef.of(name1, Connector.of('6'), Connector.of('a'),
                getImg(imageCache, imagName)
                        .addImage(getImg(imageCache, getTypePrefix() + "i-shapeSouth-6a")))
                .storeTo(imageCache).rotateRight(name2).storeTo(imageCache);
        TileDef.of(name1, Connector.of('5'), Connector.of('a'),
                getImg(imageCache, imagName)
                        .addImage(getImg(imageCache, getTypePrefix() + "i-shapeSouth-5a")))
                .storeTo(imageCache).rotateRight(name2).storeTo(imageCache);
    }

    private void addCycle(final ImageCache imageCache, final String direction1,
            final String direction2, final String direction3, final String direction4,
            final char start, final char end, final ImageWrapper imageWrapper) {
        final String dir1 = getTypePrefix() + direction1;
        final String dir2 = getTypePrefix() + direction2;
        final String dir3 = getTypePrefix() + direction3;
        final String dir4 = getTypePrefix() + direction4;
        TileDef.of(dir1, Connector.of(start), Connector.of(end), imageWrapper).storeTo(imageCache)
                .rotateRight(dir2).storeTo(imageCache).rotateRight(dir3).storeTo(imageCache)
                .rotateRight(dir4).storeTo(imageCache);
    }

    private ImageWrapper getImg(final ImageCache imageCache, final String name) {
        return ImageWrapper.of(imageCache.getImage(name));
    }

    private ImageWrapper getImgInRow(final ImageCache imageCache, final String count) {
        return getImg(imageCache, "type_" + count + "_" + getBackgroundRow());
    }

}
