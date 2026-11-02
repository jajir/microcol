package org.akropia.gui;

import org.akropia.i18n.MessageKeyResource;
import org.akropia.model.ResourceOverlay;

import com.google.common.base.Preconditions;

/**
 * Define keys for special-resource names shown when a known square is
 * inspected.
 */
enum ResourceOverlayNames implements MessageKeyResource {

    SILVER_name, ORE_name, MIXED_MINERALS_name, CLAY_PIT_name, GAME_name,
    OASIS_name, PRIME_WOOL_name, PRIME_GRAPES_name, PRIME_OLIVES_name,
    PRIME_TIMBER_name, PRIME_FOOD_name, FISHERY_name;

    private static final String NAME_SUFFIX = "_name";

    static ResourceOverlayNames getName(final ResourceOverlay overlay) {
        Preconditions.checkArgument(overlay != ResourceOverlay.NONE,
                "An empty square has no resource name.");
        return valueOf(overlay.name() + NAME_SUFFIX);
    }

}
