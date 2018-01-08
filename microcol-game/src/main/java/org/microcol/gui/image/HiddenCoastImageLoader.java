package org.microcol.gui.image;

public class HiddenCoastImageLoader extends AbstractCoastImageLoader {
	
	
	@Override
	String getTypePrefix() {
		return "hidden-";
	}

	@Override
	String getBackgroundRow() {
		return "3";
	}

}
