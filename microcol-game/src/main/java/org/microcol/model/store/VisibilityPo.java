package org.microcol.model.store;

import java.util.HashSet;
import java.util.Set;

import org.microcol.model.Location;

public class VisibilityPo extends AbstractMapStore {

    /**
     * Hold information which parts of map are visible.
     */
    private String[] visible;

    /**
     * @return the visible
     */
    public String[] getVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(String[] visible) {
        this.visible = visible;
    }

    public void setVisibility(final Set<Location> vis, final int maxX, final int maxY) {
        visible = generateString(loc -> {
            final boolean isVisible = vis.contains(loc);
            if (isVisible) {
                return "-";
            } else {
                return "#";
            }
        }, maxX, maxY);
    }

    public Set<Location> getVisibilitySet() {
        final Set<Location> out = new HashSet<>();
        if (visible != null) {
            iterate(visible, (location, charCode) -> {
                if (charCode.equals("-")) {
                    out.add(location);
                }
            });
        }
        return out;
    }
}
