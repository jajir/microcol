package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * It's a place where are units traveling from colonies to Europe and from
 * Europe to colonies.
 */
public final class HighSea {

    private final Model model;

    HighSea(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    List<PlaceHighSea> getHighSeasAll() {
        return model.getAllUnits().stream().filter(unit -> unit.getPlace() instanceof PlaceHighSea)
                .map(unit -> (PlaceHighSea) unit.getPlace()).collect(Collectors.toList());
    }

    public List<Unit> getUnitsTravelingTo(final Player player, final boolean isItToEurope) {
        return getHighSeasAll().stream()
                .filter(phs -> isItToEurope && phs.isTravelToEurope()
                        || !isItToEurope && !phs.isTravelToEurope())
                .map(PlaceHighSea::getUnit).filter(unit -> unit.getOwner().equals(player))
                .collect(Collectors.toList());
    }

    /**
     * Compute list of locations where ship could arrive from Europe.
     * 
     * @param player
     *            required ship's owner
     * @param countFromEast
     *            When it's <code>true</code> than ship will appears at east
     *            part of map otherwise appears at west.
     * @return list of suitable locations
     */
    public List<Location> getSuitablePlaceForShipCommingFromEurope(final Player player,
            final boolean countFromEast) {
        final WorldMap map = model.getMap();
        final int range = map.getMaxLocationY() / 4;
        final int start = range;
        final int stop = start + range * 2;
        final List<Location> out = new ArrayList<>();
        for (int indexY = start; indexY <= stop; indexY++) {
            out.add(findFirstSuitableLocation(player, countFromEast, indexY));
        }
        return out;
    }

    private Location findFirstSuitableLocation(final Player player, final boolean countFromEast,
            final int indexY) {
        if (countFromEast) {
            for (int indexX = model.getMap().getMaxLocationX() - 1; indexX >= 0; indexX--) {
                final Location location = Location.of(indexX, indexY);
                if (player.isPossibleToSailAt(location)) {
                    return Location.of(indexX + 1, indexY);
                }
            }
        } else {
            throw new IllegalArgumentException("NYI");
        }
        throw new IllegalStateException("Unable to find suitable place for ship at column '"
                + indexY + "', counted from east '" + countFromEast + "' for player '" + player
                + "'");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode()).toString();
    }

}
