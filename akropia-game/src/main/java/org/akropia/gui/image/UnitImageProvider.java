package org.akropia.gui.image;

import javax.inject.Singleton;

import org.akropia.model.Direction;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;
import org.akropia.model.unit.UnitArtillery;
import org.akropia.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.Image;

/**
 * Selects shared unit artwork; actual equipment and damage override profession.
 */
@Singleton
public class UnitImageProvider {

    private final ImageCache imageCache;

    @Inject
    UnitImageProvider(final ImageCache imageCache) {
        this.imageCache = Preconditions.checkNotNull(imageCache);
    }

    Image getUnitImage(final UnitType unitType) {
        return imageCache.getImage(ImageLoaderUnit.key(unitType));
    }

    Image getUnitImage(final Unit unit, final Direction orientation) {
        Preconditions.checkNotNull(orientation);
        final UnitType type = Preconditions.checkNotNull(unit).getType();
        if (type == UnitType.COLONIST || type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL
                || UnitType.EUROPEAN_SPECIALISTS.contains(type)
                || UnitType.SCHOOL_ONLY_SPECIALISTS.contains(type)) {
            if (!(unit instanceof UnitFreeColonist)) {
                throw new IllegalStateException(
                        "Unit is not instance of UnitFreeColonist");
            }
            final UnitFreeColonist colonist = (UnitFreeColonist) unit;
            if (colonist.isMounted()) {
                return imageCache.getImage(colonist.isHoldingWeapons()
                        ? ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED_WEAPONS
                        : ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED);
            }
            if (colonist.isHoldingWeapons()) {
                return imageCache.getImage(
                        ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_WEAPONS);
            }
            if (colonist.isHoldingTools()) {
                return imageCache
                        .getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_TOOLS);
            }
        }
        if (type == UnitType.ARTILLERY && ((UnitArtillery) unit).isDamaged()) {
            return imageCache.getImage("artillery-damaged.png");
        }
        if (orientation == Direction.west && (type == UnitType.GALLEON
                || type == UnitType.CARAVEL || type == UnitType.MERCHANTMAN)) {
            return imageCache.getImage(ImageLoaderUnit.key(type) + "-west");
        }
        return getUnitImage(type);
    }
}
