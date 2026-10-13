package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents continent on map. Continent is connected list of locations.
 */
public class Continent {

    private final List<Location> locations = new ArrayList<>();

    private final Model model;

    /**
     * Default constructor.
     *
     * @param model required model
     */
    Continent(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    void add(final Location location) {
        locations.add(location);
    }

    boolean contains(final Location location) {
        Preconditions.checkNotNull(location);
        return locations.contains(location);
    }

    boolean contains(final Unit unit) {
        Preconditions.checkNotNull(unit);
        return contains(unit.getLocation());
    }

    public List<Location> getLocations() {
        return ImmutableList.copyOf(locations);
    }

    public int getDistance(final Location location) {
        return locations.stream()
                .sorted(Comparator
                        .comparingInt(loc -> location.getDistance(loc)))
                .findFirst().get().getDistance(location);
    }

    /**
     * World map usually have thin continents on the north and south of the map.
     * This two continents should represents Antarctic and Arctic.
     *
     * @return Return <code>true</code> when this continent is par of Arctic or
     *         Antarctic otherwise return <code>false</code>.
     */
    public boolean isMapBorder() {
        return locations.stream().filter(this::isLocationBorder).findAny()
                .isPresent();
    }

    private boolean isLocationBorder(final Location location) {
        return location.getY() == 1
                || location.getY() == model.getMap().getMaxLocationY() - 1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Continents.class)
                .add("locations", locations).toString();
    }

}
