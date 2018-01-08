package org.microcol.gui.image;

import org.microcol.model.Location;

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
	public boolean isVoid(final Location infoHolder) {
		return getMap().isVisible(infoHolder);
	}

	@Override
	public boolean isMass(final Location infoHolder) {
		return !getMap().isVisible(infoHolder);
	}

	@Override
	public boolean skipp(final Location infoHolder){
		return !getMap().isVisible(infoHolder);		
	}
}
