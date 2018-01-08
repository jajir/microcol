package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

class InfoHolder {
	
	private final Location loc;
	private final TerrainType tt;

	public InfoHolder(final Location loc, final AbstractCoastMapGenerator generator) {
		this.loc = loc;
		this.tt = generator.getTerrainTypeAt(loc);
	}

	/**
	 * @return the loc
	 */
	public Location loc() {
		return loc;
	}

	/**
	 * @return the tt
	 */
	public TerrainType tt() {
		return tt;
	}
}