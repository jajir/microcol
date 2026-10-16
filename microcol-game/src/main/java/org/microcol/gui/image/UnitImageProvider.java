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

        final ImmutableMap.Builder<UnitType, Image> images = ImmutableMap
                .<UnitType, Image>builder()
                .put(UnitType.ARTILLERY, imageCache.getImage("artillery.png"))
                .put(UnitType.CARAVEL,
                        imageCache.getImage(IMG_UNIT_SHIP_GALEON_EAST))
                .put(UnitType.MERCHANTMAN,
                        imageCache.getImage(IMG_UNIT_SHIP_GALEON_EAST))
                .put(UnitType.PRIVATEER,
                        imageCache.getImage(IMG_UNIT_SHIP_FRIGATE))
                .put(UnitType.MAN_OF_WAR,
                        imageCache.getImage(IMG_UNIT_SHIP_FRIGATE))
                .put(UnitType.GALLEON,
                        imageCache.getImage(IMG_UNIT_SHIP_GALEON_EAST))
                .put(UnitType.FRIGATE,
                        imageCache.getImage(IMG_UNIT_SHIP_FRIGATE))
                .put(UnitType.COLONIST,
                        imageCache.getImage(IMG_UNIT_FREE_COLONIST))
                .put(UnitType.WAGON,
                        imageCache.getImage(ImageLoaderUnit.IMG_UNIT_WAGON));
        UnitType.EUROPEAN_SPECIALISTS.forEach(type -> images.put(type,
                imageCache.getImage(IMG_UNIT_FREE_COLONIST)));
        UnitType.SCHOOL_ONLY_SPECIALISTS.forEach(type -> images.put(type,
                imageCache.getImage(IMG_UNIT_FREE_COLONIST)));
        images.put(UnitType.INDENTURED_SERVANT,
                imageCache.getImage(IMG_UNIT_FREE_COLONIST));
        images.put(UnitType.PETTY_CRIMINAL,
                imageCache.getImage(IMG_UNIT_FREE_COLONIST));
        unitImageMap = images.build();

        unitImageResolver = new ChainOfCommandStrategy<UnitImageRequest, Image>(
                Lists.newArrayList(request -> {
                    if (UnitType.GALLEON == request.getUnitType()
                            || UnitType.CARAVEL == request.getUnitType()
                            || UnitType.MERCHANTMAN == request.getUnitType()) {
                        if (Direction.west == request.getOrientation()) {
                            return imageCache.getImage(
                                    ImageLoaderUnit.IMG_UNIT_SHIP_GALEON_WEST);
                        } else {
                            return imageCache.getImage(
                                    ImageLoaderUnit.IMG_UNIT_SHIP_GALEON_EAST);
                        }
                    }
                    return null;
                }, request -> {
                    if (UnitType.COLONIST == request.getUnitType()
                            || UnitType.INDENTURED_SERVANT == request
                                    .getUnitType()
                            || UnitType.PETTY_CRIMINAL == request.getUnitType()
                            || UnitType.EUROPEAN_SPECIALISTS
                                    .contains(request.getUnitType())
                            || UnitType.SCHOOL_ONLY_SPECIALISTS
                                    .contains(request.getUnitType())) {
                        if (request.getUnit() instanceof UnitFreeColonist) {
                            final UnitFreeColonist fc = (UnitFreeColonist) request
                                    .getUnit();
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
                                    return imageCache.getImage(
                                            ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_TOOLS);
                                } else {
                                    return imageCache.getImage(
                                            ImageLoaderUnit.IMG_UNIT_FREE_COLONIST);
                                }
                            }
                        } else {
                            throw new IllegalStateException(
                                    "Colonist in not instace of UnitFreeColonist class");
                        }
                    }
                    return null;
                }, request -> {
                    if (UnitType.ARTILLERY == request.getUnitType()) {
                        final org.microcol.model.unit.UnitArtillery artillery = (org.microcol.model.unit.UnitArtillery) request
                                .getUnit();
                        return imageCache.getImage(
                                artillery.isDamaged() ? "artillery-damaged.png"
                                        : "artillery.png");
                    }
                    if (UnitType.WAGON == request.getUnitType()) {
                        return imageCache
                                .getImage(ImageLoaderUnit.IMG_UNIT_WAGON);
                    }
                    if (UnitType.FRIGATE == request.getUnitType()
                            || UnitType.PRIVATEER == request.getUnitType()
                            || UnitType.MAN_OF_WAR == request.getUnitType()) {
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
     * @param unitType required unit type
     * @return image representing ship image
     */
    Image getUnitImage(final UnitType unitType) {
        return unitImageMap.get(unitType);
    }

    /**
     * For specific unit instance and orientation return appropriate image.
     *
     * @param unit        required unit
     * @param orientation required orientation of unit
     * @return unit image instance
     */
    Image getUnitImage(final Unit unit, final Direction orientation) {
        return unitImageResolver.apply(new UnitImageRequest(unit, orientation));
    }

}
