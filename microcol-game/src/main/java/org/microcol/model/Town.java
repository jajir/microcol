package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Town {

	/**
	 * Town unique name.
	 */
	private String name;

	private final Player owner;

	private final Location location;

	private final List<TownField> townSection;

	private final List<Construction> constructions;

	private Model model;

	public Town(final String name, final Player owner, final Location location,
			final List<Construction> constructions) {
		this.name = Preconditions.checkNotNull(name);
		this.owner = Preconditions.checkNotNull(owner, "owner is null");
		this.location = Preconditions.checkNotNull(location);
		townSection = new ArrayList<>();
		location.getNeighbors().forEach(loc -> townSection.add(new TownField(loc)));
		this.constructions = Preconditions.checkNotNull(constructions);
		checkConstructions();
	}

	/**
	 * Verify that constructions are consistent. It verify:
	 * <ul>
	 * <li>That all construction types are at list just one.</li>
	 * <li>That each good is produces by just one construction.</li>
	 * <li>That no building and it's upgrade are at constructions list.</li>
	 * </ul>
	 */
	private void checkConstructions() {
		Preconditions.checkState(townSection.size() == 8,
				String.format("Incorrect town filed number '%s'", townSection.size()));
		Map<ConstructionType, Long> l1 = constructions.stream()
				.collect(Collectors.groupingBy(Construction::getType, Collectors.counting()));
		l1.forEach((constructionType, count) -> {
			if (count > 1) {
				throw new IllegalStateException(
						String.format("Construction type '%s' is duplicated", constructionType.name()));
			}
		});

		final Map<GoodType, Long> l2 = constructions.stream()
				.filter(construction -> construction.getType().getProduce().isPresent()).collect(Collectors
						.groupingBy(construction -> construction.getType().getProduce().get(), Collectors.counting()));
		l2.forEach((goodType, count) -> {
			if (count != 1) {
				throw new IllegalStateException(
						String.format("Good type type '%s' is prodecen in more than one building", goodType.name()));
			}
		});

		constructions.forEach(constructio -> {
			if (constructio.getType().getUpgradeTo().isPresent()) {
				final ConstructionType upgradeTo = constructio.getType().getUpgradeTo().get();
				constructions.stream().filter(cc -> cc.getType().equals(upgradeTo)).findAny().ifPresent(cc -> {
					throw new IllegalStateException(String.format("There is building type '%s' and it's upgrade '%s'",
							constructio.getType(), cc));
				});
			}
		});

	}
	
	public Construction getConstructionByType(final ConstructionType constructionType) {
		return constructions.stream().filter(construction -> construction.getType().equals(constructionType)).findAny()
				.orElseThrow(() -> new IllegalStateException(
						String.format("No such construction type (%s) in town (%s)", constructionType, getName())));
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public Player getOwner() {
		return owner;
	}

	public List<TownField> getTownSection() {
		return townSection;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Town.class).add("name", name).add("location", location).toString();
	}

	public List<Construction> getConstructions() {
		return constructions;
	}
}
