package org.microcol.model;

public final class ContinentTool {

    /**
     * Find connected land masses in map encounter order.
     *
     * @param model required model
     * @return continents
     */
    public Continents findContinents(final Model model) {
        final WorldMap map = model.getMap();
        final Continents continents = new Continents();
        for (int x = 1; x <= model.getMap().getMaxLocationX(); x++) {
            for (int y = 1; y <= model.getMap().getMaxLocationY(); y++) {
                final Location loc = Location.of(x, y);
                final TerrainType terrainType = map.getTerrainTypeAt(loc);
                if (!terrainType.isSea()
                        && !continents.getForLocation(loc).isPresent()) {
                    final Continent continent = createContinent(loc, model);
                    continents.add(continent);
                }
            }
        }
        return continents;
    }

    private Continent createContinent(final Location loc, final Model model) {
        final Continent out = new Continent(model);
        out.add(loc);
        tryToAdd(loc, model.getMap(), out);
        return out;
    }

    private void tryToAdd(final Location location, final WorldMap map,
            final Continent continent) {
        Direction.getVectors().forEach(direction -> {
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
