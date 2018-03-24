package org.microcol.ai;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

public class ContinentTool {

    /**
     * Find top 3 continents with biggest military forces in cities owned by
     * enemy player.
     * 
     * @param model
     *            required model
     * @param enemyPlayer
     *            required enemy player
     * @return continents
     */
    public Continents findContinents(final Model model, final Player enemyPlayer) {
        final WorldMap map = model.getMap();
        final Continents continents = new Continents();
        for (int x = 1; x <= model.getMap().getMaxX(); x++) {
            for (int y = 1; y <= model.getMap().getMaxY(); y++) {
                final Location loc = Location.of(x, y);
                final TerrainType terrainType = map.getTerrainTypeAt(loc);
                if (!terrainType.isSea() && !continents.getForLocation(loc).isPresent()) {
                    final Continent continent = createContinent(loc, model, enemyPlayer);
                    continents.add(continent);
                }
            }
        }
        return continents;
    }

    private Continent createContinent(final Location loc, final Model model,
            final Player enemyPlayer) {
        final Continent out = new Continent(model, enemyPlayer);
        tryToAdd(loc, model.getMap(), out);
        return out;
    }

    private void tryToAdd(final Location location, final WorldMap map, final Continent continent) {
        Location.DIRECTIONS.forEach(direction -> {
            final Location loc = location.add(direction);
            if (map.isValid(loc)) {
                final TerrainType terrainType = map.getTerrainTypeAt(loc);
                if (!terrainType.isSea() && !continent.contains(loc)) {
                    continent.add(loc);
                    tryToAdd(loc, map, continent);
                }
            }
        });
    }

}
