package org.akropia.gui.screen.game.gamepanel;

import org.akropia.model.TradeRoutePreview;

import javafx.scene.paint.Color;

/**
 * Stable colors of trade routes in the map planner and its panel list. A route
 * keeps the color of its ID; a route without carrier is grey, because its paths
 * are only possibilities.
 */
public final class TradeRouteColors {

    /** Bright inks that stay distinct on the darkened map. */
    private static final Color[] PALETTE = { Color.web("#F2C14E"),
            Color.web("#4FC3F7"), Color.web("#EF6F6C"), Color.web("#8BD17C"),
            Color.web("#C792EA"), Color.web("#FF9F5A"), Color.web("#5DE0D0"),
            Color.web("#F48FB1") };

    /** Ink of possible paths of a route without carrier. */
    public static final Color UNASSIGNED = Color.web("#C8C4BA");

    private TradeRouteColors() {
    }

    /**
     * @param preview required route preview
     * @return the route's ink
     */
    public static Color colorOf(final TradeRoutePreview preview) {
        if (preview.getCarrier().isEmpty()) {
            return UNASSIGNED;
        }
        return PALETTE[Math.floorMod(preview.getRoute().getId(),
                PALETTE.length)];
    }
}
