package org.microcol.gui.image;

public class CloudCoastImageLoader extends AbstractCoastImageLoader {

	@Override
	String getTypePrefix() {
		return "cloud-";
	}

	@Override
	String getBackgroundRow() {
		return "3";
	}

}
