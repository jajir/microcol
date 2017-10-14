package org.microcol.model.store;

public class PlaceHighSeasPo extends PlacePo {
	
	private boolean isTravelToEurope;

	private int remainigTurns;

	public boolean isTravelToEurope() {
		return isTravelToEurope;
	}

	public void setTravelToEurope(boolean isTravelToEurope) {
		this.isTravelToEurope = isTravelToEurope;
	}

	public int getRemainigTurns() {
		return remainigTurns;
	}

	public void setRemainigTurns(int remainigTurns) {
		this.remainigTurns = remainigTurns;
	}
	
}
