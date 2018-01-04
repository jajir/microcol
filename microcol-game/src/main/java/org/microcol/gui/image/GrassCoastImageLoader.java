package org.microcol.gui.image;

public class GrassCoastImageLoader extends AbstractCoastImageLoader {

	@Override
	String getTypePrefix() {
		return "grass-";
	}

	@Override
	String getBackgroundRow() {
		return "1";
	}

}
