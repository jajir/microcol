package org.microcol.gui.image;

import com.google.inject.Inject;

public class HiddenCoastMapGenerator extends AbstractCoastMapGenerator {

	@Inject
	HiddenCoastMapGenerator(final ImageProvider imageProvider) {
		super(imageProvider);
	}

	@Override
	public String getPrefix() {
		return "hidden-";
	}

	@Override
	public boolean isVoid(final InfoHolder infoHolder) {
		return getMap().isVisible(infoHolder.loc());
	}

	@Override
	public boolean isMass(final InfoHolder infoHolder) {
		return !getMap().isVisible(infoHolder.loc());
	}

	@Override
	public boolean skipp(final InfoHolder infoHolder) {
		return !getMap().isVisible(infoHolder.loc());
	}
}
