package org.microcol.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

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
     * @param model
     *            required model
     * @param enemyPlayer
     *            required enemyPlayer
     */
    Continent(final Model model, final Player enemyPlayer) {
        this.model = Preconditions.checkNotNull(model);
    }

    public void add(final Location location) {
        locations.add(location);
    }

    public boolean contains(final Location location) {
        Preconditions.checkNotNull(location);
        return locations.contains(location);
    }

    public boolean contains(final Unit unit) {
        Preconditions.checkNotNull(unit);
        return contains(unit.getLocation());
    }

    public Optional<Location> getClosesEnemyCityToAttack(final Location unitLocation,
            final Player enemyPlayer) {
        return locations.stream().filter(loc -> model.getColoniesAt(loc, enemyPlayer).isPresent())
                .sorted(Comparator.comparingInt(loc -> unitLocation.getDistance(loc))).findFirst();
    }

    public List<Location> getLocations() {
        return ImmutableList.copyOf(locations);
    }

    public int getDistance(final Location location) {
        return locations.stream().sorted(Comparator.comparingInt(loc -> location.getDistance(loc)))
                .findFirst().get().getDistance(location);
    }

    /**
     * Return score how much interesting to conquer is this continent. Score is
     * sum of military force of all colonies.
     * 
     * @param enemyPlayer
     *            required player to attack
     * @return score
     */
    public int getMilitaryImportance(final Player enemyPlayer) {
        return locations
                .stream().map(loc -> model.getColoniesAt(loc, enemyPlayer)
                        .map(col -> col.getMilitaryForce()).orElse(0))
                .mapToInt(Integer::intValue).sum();
    }

    /**
     * World map usually have thin continents on the north and south of the map.
     * This two continents should represents Antarctic and Arctic.
     *
     * @return Return <code>true</code> when this continent is par of Arctic or
     *         Antarctic otherwise return <code>false</code>.
     */
    public boolean isMapBorder() {
        if (locations.stream().filter(loc -> loc.getY() == 1).findAny().isPresent()) {
            return true;
        }
        if (locations.stream().filter(loc -> loc.getY() == model.getMap().getMaxY() - 1).findAny()
                .isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Continents.class).add("locations", locations).toString();
    }

}
