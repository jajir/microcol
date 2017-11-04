package org.microcol.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Continents {

	private final List<Continent> continents = new ArrayList<>();

	private final Model model;

	private final Player enemyPlayer;

	public Continents(final Model model, final Player enemyPlayer) {
		this.model = Preconditions.checkNotNull(model);
		this.enemyPlayer = Preconditions.checkNotNull(enemyPlayer);
	}

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

	public int size() {
		return continents.size();
	}

	public void print() {
		continents.forEach(continent -> {
			System.out.println(continent.toString());
		});
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Continents.class).add("continents", continents).toString();
	}

}
