package org.akropia.ai;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.akropia.model.Continent;
import org.akropia.model.Continents;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.collect.ImmutableList;

/** Royal army target selection over the shared map topology. */
final class ContinentTargets {

    private final Model model;

    ContinentTargets(final Model model) {
        this.model = model;
    }

    Optional<Location> getClosesEnemyCityToAttack(final Continent continent,
            final Location unitLocation, final Player enemyPlayer) {
        return continent.getLocations().stream().filter(
                loc -> model.getColoniesAt(loc, enemyPlayer).isPresent())
                .sorted(Comparator
                        .comparingInt(loc -> unitLocation.getDistance(loc)))
                .findFirst();
    }

    int getMilitaryImportance(final Continent continent,
            final Player enemyPlayer) {
        return continent.getLocations().stream()
                .map(loc -> model.getColoniesAt(loc, enemyPlayer)
                        .map(col -> col.getMilitaryForce()).orElse(0))
                .mapToInt(Integer::intValue).sum();
    }

    List<Continent> getContinentsToAttack(final Continents continents,
            final Player enemyPlayer) {
        return continents.getContinents().stream().filter(
                continent -> getMilitaryImportance(continent, enemyPlayer) > 0)
                .sorted(Comparator.comparingInt(
                        continent -> getMilitaryImportance(continent,
                                enemyPlayer)))
                .limit(3).collect(ImmutableList.toImmutableList());
    }
}
