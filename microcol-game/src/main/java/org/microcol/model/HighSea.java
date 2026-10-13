package org.microcol.model;

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
        return model.getAllUnits().stream()
                .filter(unit -> unit.getPlace() instanceof PlaceHighSea)
                .map(unit -> (PlaceHighSea) unit.getPlace())
                .collect(Collectors.toList());
    }

    public List<Unit> getUnitsTravelingTo(final Player player,
            final boolean isItToEurope) {
        return getHighSeasAll().stream()
                .filter(phs -> isItToEurope && phs.isTravelToEurope()
                        || !isItToEurope && !phs.isTravelToEurope())
                .map(PlaceHighSea::getUnit)
                .filter(unit -> unit.getOwner().equals(player))
                .collect(Collectors.toList());
    }

    /**
     * Returns the direction the unit should depart from {@code origin} based on
     * the horizontal position of the location relative to the map width.
     *
     * @param origin the departure location
     * @return {@code Direction.east} or {@code Direction.west}
     */
    public Direction getDepartureDirection(final Location origin) {
        Preconditions.checkNotNull(origin, "origin");
        WorldMap map = model.getMap();
        Preconditions.checkArgument(map.isValid(origin),
                "Invalid origin location");
        Preconditions.checkArgument(
                map.getTerrainTypeAt(origin) == TerrainType.HIGH_SEA,
                "Terrain at origin must be HIGH_SEA");
        int maxX = map.getMaxLocationX();
        return (origin.getX() - 1 < maxX - origin.getX()) ? Direction.west
                : Direction.east;
    }

    /**
     * Determines whether the move from the unit's current location to
     * {@code target} represents a valid departure move along the high sea
     * coastline.
     *
     * @param unit   the unit attempting the move
     * @param target the destination location
     * @return {@code true} if the move is a valid departure move, {@code false}
     *         otherwise
     */
    public boolean isDepartureMove(final Unit unit, final Location target) {
        Preconditions.checkNotNull(unit, "unit");
        Preconditions.checkNotNull(target, "target");
        if (!unit.isShip() || !unit.isAtPlaceLocation()) {
            return false;
        }
        Location source = unit.getLocation();
        WorldMap map = model.getMap();
        if (!map.isValid(source) || !map.isValid(target)) {
            return false;
        }
        if (!source.isNeighbor(target)) {
            return false;
        }
        if (map.getTerrainTypeAt(source) != TerrainType.HIGH_SEA
                || map.getTerrainTypeAt(target) != TerrainType.HIGH_SEA) {
            return false;
        }
        Direction depDir = getDepartureDirection(source);
        if (depDir == Direction.east && target.getX() > source.getX()) {
            return true;
        }
        if (depDir == Direction.west && target.getX() < source.getX()) {
            return true;
        }
        return false;
    }

    /**
     * Finds a default arrival location for a unit leaving the high sea.
     *
     * <p>
     * The algorithm scans the map for high sea locations that:
     * <ul>
     * <li>Have departure direction {@code EAST}</li>
     * <li>Have at least one neighbouring location that is {@code OCEAN}</li>
     * </ul>
     * The best candidate is the one with the minimal value of
     * {@code |2*y - (maxY + 1)|}. Ties are broken by lower {@code y} first,
     * then lower {@code x}. If no candidate exists, an empty
     * {@link java.util.Optional} is returned.
     *
     * @return an {@link java.util.Optional} containing the default arrival
     *         location, if any
     */
    public java.util.Optional<Location> getDefaultArrivalLocation() {
        WorldMap map = model.getMap();
        int maxX = map.getMaxLocationX();
        int maxY = map.getMaxLocationY();
        Location best = null;
        long bestDist = Long.MAX_VALUE;
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX; x++) {
                Location loc = Location.of(x, y);
                if (!map.isValid(loc)) {
                    continue;
                }
                if (map.getTerrainTypeAt(loc) != TerrainType.HIGH_SEA) {
                    continue;
                }
                if (getDepartureDirection(loc) != Direction.east) {
                    continue;
                }
                boolean hasOceanNeighbor = false;
                for (Location n : loc.getNeighbors()) {
                    if (map.isValid(n)
                            && map.getTerrainTypeAt(n) == TerrainType.OCEAN) {
                        hasOceanNeighbor = true;
                        break;
                    }
                }
                if (!hasOceanNeighbor) {
                    continue;
                }
                long dist = Math.abs(2L * y - (maxY + 1L));
                if (dist < bestDist) {
                    bestDist = dist;
                    best = loc;
                }
            }
        }
        return java.util.Optional.ofNullable(best);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .toString();
    }

}
