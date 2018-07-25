package org.microcol.gui.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * For each map location holds pseudo random number. See
 * {@link WorldMap#getSeed()}.
 */
public final class WorldMapRandoms {

    /**
     * For each map location holds pseudo random number.
     */
    private final Map<Location, Integer> randoms;

    /**
     * Constructor from world map.
     *
     * @param map
     *            required world map
     */
    public WorldMapRandoms(final WorldMap map) {
        Preconditions.checkNotNull(map);
        final Map<Location, Integer> tmp = new HashMap<>();
        final Random random = new Random(map.getSeed());
        for (int x = 1; x <= map.getMaxX(); x++) {
            for (int y = 1; y <= map.getMaxY(); y++) {
                tmp.put(Location.of(x, y), random.nextInt());
            }
        }
        randoms = ImmutableMap.copyOf(tmp);
    }

    /**
     * For each place at map return unique pseudo random number. This number is
     * same for map and location.
     *
     * @param location
     *            required location
     * @return unique pseudo random number
     */
    public Integer getRandomAt(final Location location) {
        return randoms.get(Preconditions.checkNotNull(location));
    }

}
