package org.microcol.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.MicroColException;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Continents {

	private final List<Continent> continents = new ArrayList<>();

	public void add(final Continent continent) {
		continents.add(continent);
	}

	Optional<Continent> getForLocation(final Location location) {
		return continents.stream().filter(continent -> continent.contains(location)).findAny();
	}

	List<Continent> getContinentsToAttack() {
		return continents.stream().filter(continents -> continents.getColonyWeight() > 0)
				.sorted(Comparator.comparingInt(continent -> continent.getColonyWeight())).limit(3)
				.collect(ImmutableList.toImmutableList());
	}
	
	Continent getContinentWhereIsUnitPlaced(final Unit unit){
		Preconditions.checkNotNull(unit);
		return continents.stream().filter(continent -> continent.contains(unit)).findFirst().orElseThrow(
				() -> new MicroColException(String.format("Unable to find continent for unit (%s)", unit)));
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Continents.class).add("continents", continents).toString();
	}

}
