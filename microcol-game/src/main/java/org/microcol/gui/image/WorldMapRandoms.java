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
public class WorldMapRandoms {

    /**
     * For each map location holds pseudo random number.
     */
    private final Map<Location, Integer> randoms;

    public WorldMapRandoms(final WorldMap map) {
        final Map<Location, Integer> tmp = new HashMap<>();
        final Random random = new Random(map.getSeed());
        // TODO this iteration could be done on list of locations ;-)
        for (int x = 1; x <= map.getMaxX(); x++) {
            for (int y = 1; y <= map.getMaxY(); y++) {
                tmp.put(Location.of(x, y), random.nextInt());
            }
        }
        randoms = ImmutableMap.copyOf(tmp);
    }

    public Integer getRandomAt(final Location location) {
        return randoms.get(Preconditions.checkNotNull(location));
    }

}
