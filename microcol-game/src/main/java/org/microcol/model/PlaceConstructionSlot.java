package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Unit is placed in some construction slot.
 */
public class PlaceConstructionSlot extends AbstractPlace {

	private final ConstructionSlot constructionSlot;

	public PlaceConstructionSlot(final Unit unit, final ConstructionSlot constructionSlot) {
		super(unit);
		this.constructionSlot = Preconditions.checkNotNull(constructionSlot);
	}

	@Override
	public String getName() {
		return "Construction";
	}

	public ConstructionSlot getConstructionSlot() {
		return constructionSlot;
	}
	
	@Override
	public void destroy() {
		constructionSlot.clear();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PlaceConstructionSlot.class)
				.add("unit id", getUnit().getId())
				.add("constructionSlot", constructionSlot)
				.toString();
	}
	
}
