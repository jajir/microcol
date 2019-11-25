package org.microcol.gui.image;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.MicroColException;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.image.Image;

/**
 * Provide image instances.
 */
@Singleton
public final class ImageProvider {

    private final Logger LOGGER = LoggerFactory.getLogger(ImageProvider.class);

    private final static Direction DEFAULT_DIRECTION_WHERE_UNIT_LOOK = Direction.east;

    public static final String IMG_CURSOR_GOTO = "cursor-goto.png";

    public static final String IMG_ICON_STEPS_25x25 = "icon-steps-25x25.png";

    public static final String IMG_ICON_STEPS_TURN_25x25 = "icon-steps-turn-25x25.png";

    public static final String IMG_ICON_STEPS_FIGHT_25x25 = "icon-steps-fight-25x25.png";

    public static final String IMG_ICON_STEPS_ANCHOR_25x25 = "icon-steps-anchor-25x25.png";

    public static final String IMG_ICON_STEPS_ANCHOR_TURN_25x25 = "icon-steps-anchor-turn-25x25.png";

    public static final String IMG_ICON_STEPS_FIGHT_TURN_25x25 = "icon-steps-fight-turn-25x25.png";

    public static final String IMG_CROSSED_SWORDS = "crossed-swords.png";

    public static final String IMG_CRATE_OPEN = "crate-open.png";

    public static final String IMG_CRATE_CLOSED = "crate-closed.png";

    private static final String BASE_PACKAGE = "images";

    private final ImageCache imageCache;

    private final GoodsImageProvider goodsImageProvider;

    private final UnitImageProvider unitImageProvider;

    private final TerrainImageProvider terrainImageProvider;

    @Inject
    public ImageProvider(final ImageCache imageCache, final ImageLoaderImpl imageLoaderImpl) {
        this.imageCache = Preconditions.checkNotNull(imageCache);
        this.unitImageProvider = Preconditions.checkNotNull(new UnitImageProvider(imageCache));
        this.terrainImageProvider = new TerrainImageProvider(imageCache);
        this.goodsImageProvider = new GoodsImageProvider(imageCache);
        Preconditions.checkNotNull(imageLoaderImpl);
        LOGGER.info("Loading image provider");
    }

    /**
     * See {@link ImageCache#getImageNames()}.
     * 
     * @return list of images names.
     */
    public List<String> getImageNames() {
        return imageCache.getImageNames();
    }

    /**
     * Load image for name. If image is not in cache it try to load it from
     * image store.
     * 
     * @param name
     *            required image name
     * @return loaded image
     */
    public Image getImage(final String name) {
        return imageCache.getImage(name);
    }

    /**
     * For given construction type find image.
     *
     * @param constructionType
     *            required construction type.
     * @return construction image or empty optional
     */
    public Optional<Image> getConstructionImage(final ConstructionType constructionType) {
        final String key = "building_" + constructionType.name();
        if (imageCache.containsImage(key)) {
            return Optional.of(imageCache.getImage(key));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Simplify loading image from resource. Path should look like: <code>
     * org/microcol/images/unit-60x60.gif
     * </code>. Class suppose that all images are in directory <i>images</i>.
     * 
     * @param rawPath
     *            path at classpath where is stored image
     * @return return {@link javafx.scene.image.Image} object
     */
    public static Image getRawImage(final String rawPath) {
        final String path = BASE_PACKAGE + "/" + rawPath;
        ClassLoader cl = ImageProvider.class.getClassLoader();
        final InputStream in = cl.getResourceAsStream(path);
        if (in == null) {
            throw new MicroColException("Unable to load file '" + path + "'.");
        } else {
            return new Image(cl.getResourceAsStream(path));
        }
    }

    /**
     * Allow to register new type of image.
     * 
     * @param name
     *            required unique image name
     * @param image
     *            required image object
     */
    public void registerImage(final String name, final Image image) {
        imageCache.registerImage(name, image);
    }

    /**
     * Allow to register image under new name. Image will be accessible under
     * both names. Image is physically still just one.
     * 
     * @param newImageName
     *            required new image name
     * @param imageName
     *            required old image name
     */
    public void registerImage(final String newImageName, final String imageName) {
        imageCache.registerImage(newImageName, imageName);
    }

    /**
     * For specific terrain type find corresponding image.
     * 
     * @param terrain
     *            required terrain type
     * @return image representing terrain image
     */
    public Image getTerrainImage(final TerrainType terrain) {
        return terrainImageProvider.getTerrainImage(terrain);
    }

    /**
     * For specific unit type find corresponding image.
     * <p>
     * Should be used just in cases when it's impossible to get unit instance.
     * For example for drawing units to buy in Europe.
     * </p>
     *
     * @param unitType
     *            required unit type
     * @return image representing ship image
     */
    public Image getUnitImage(final UnitType unitType) {
        return unitImageProvider.getUnitImage(unitType);
    }

    /**
     * For specific unit instance and orientation return appropriate image.
     *
     * @param unit
     *            required unit
     * @param orientation
     *            required orientation of unit
     * @return unit image instance
     */
    public Image getUnitImage(final Unit unit, final Direction orientation) {
        return unitImageProvider.getUnitImage(unit, orientation);
    }

    /**
     * For specific unit find corresponding image.
     * 
     * @param unit
     *            required unit
     * @return image representing ship image
     */
    public Image getUnitImage(final Unit unit) {
        if (unit.isAtPlaceLocation()) {
            return getUnitImage(unit, unit.getPlaceLocation().getOrientation());
        } else {
            return getUnitImage(unit, DEFAULT_DIRECTION_WHERE_UNIT_LOOK);
        }
    }

    /**
     * For specific good type find corresponding image.
     * 
     * @param goodsType
     *            required good type
     * @return image representing good type
     */
    public Image getGoodsTypeImage(final GoodsType goodsType) {
        return goodsImageProvider.getGoodsTypeImage(goodsType);
    }

    /**
     * For specific good amount find corresponding image.
     * 
     * @param goods
     *            required good amount
     * @return image representing good type
     */
    public Image getGoodsTypeImage(final Goods goods) {
        return getGoodsTypeImage(goods.getType());
    }

}
