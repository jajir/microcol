package org.microcol.gui.image;

import static org.microcol.gui.image.ImageLoaderUnit.IMG_UNIT_FREE_COLONIST;
import static org.microcol.gui.image.ImageLoaderUnit.IMG_UNIT_SHIP_FRIGATE;
import static org.microcol.gui.image.ImageLoaderUnit.IMG_UNIT_SHIP_GALEON_EAST;

import java.util.Map;

import javax.inject.Singleton;

import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Direction;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javafx.scene.image.Image;

/**
 * For specific unit type provide correct image. Image consider direction where
 * unit is turned to. It allows unit to loot at movement direction.
 */
@Singleton
public class UnitImageProvider {

    private final Map<UnitType, Image> unitImageMap;

    private final ChainOfCommandStrategy<UnitImageRequest, Image> unitImageResolver;

    @Inject
    UnitImageProvider(final ImageCache imageCache) {
        Preconditions.checkNotNull(imageCache);

        unitImageMap = ImmutableMap.<UnitType, Image>builder()
                .put(UnitType.GALLEON, imageCache.getImage(IMG_UNIT_SHIP_GALEON_EAST))
                .put(UnitType.FRIGATE, imageCache.getImage(IMG_UNIT_SHIP_FRIGATE))
                .put(UnitType.COLONIST, imageCache.getImage(IMG_UNIT_FREE_COLONIST)).build();

        unitImageResolver = new ChainOfCommandStrategy<UnitImageRequest, Image>(
                Lists.newArrayList(request -> {
                    if (UnitType.GALLEON == request.getUnitType()) {
                        if (Direction.west == request.getOrientation()) {
                            return imageCache.getImage(ImageLoaderUnit.IMG_UNIT_SHIP_GALEON_WEST);
                        } else if (Direction.east == request.getOrientation()) {
                            return imageCache.getImage(ImageLoaderUnit.IMG_UNIT_SHIP_GALEON_EAST);
                        }
                    }
                    return null;
                }, request -> {
                    if (UnitType.COLONIST == request.getUnitType()) {
                        if (request.getUnit() instanceof UnitFreeColonist) {
                            final UnitFreeColonist fc = (UnitFreeColonist) request.getUnit();
                            if (fc.isMounted()) {
                                if (fc.isHoldingGuns()) {
                                    return imageCache.getImage(
                                            ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED_MUSKETS);
                                } else {
                                    return imageCache.getImage(
                                            ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED);
                                }
                            } else {
                                if (fc.isHoldingGuns()) {
                                    return imageCache.getImage(
                                            ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MUSKETS);
                                } else if (fc.isHoldingTools()) {
                                    return imageCache
                                            .getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_TOOLS);
                                } else {
                                    return imageCache
                                            .getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST);
                                }
                            }
                        } else {
                            throw new IllegalStateException(
                                    "Colonist in not instace of UnitFreeColonist class");
                        }
                    }
                    return null;
                }, request -> {
                    if (UnitType.FRIGATE == request.getUnitType()) {
                        return imageCache.getImage(IMG_UNIT_SHIP_FRIGATE);
                    }
                    return null;
                }));
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
    Image getUnitImage(final UnitType unitType) {
        return unitImageMap.get(unitType);
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
    Image getUnitImage(final Unit unit, final Direction orientation) {
        return unitImageResolver.apply(new UnitImageRequest(unit, orientation));
    }

}
