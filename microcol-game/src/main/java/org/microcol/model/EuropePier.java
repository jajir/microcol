package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * It's place where units stands before they sail to New World. Recruited units
 * appears here.
 */
public class EuropePier {

	private final Model model;

	EuropePier(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	public List<Unit> getUnits(final Player player) {
		return model.getAllUnits().stream().filter(unit -> unit.isAtEuropePier())
				.filter(unit -> unit.getOwner().equals(player)).collect(Collectors.toList());
	}

}
