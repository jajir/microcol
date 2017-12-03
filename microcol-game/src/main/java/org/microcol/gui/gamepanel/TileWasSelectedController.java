package org.microcol.gui.gamepanel;

import org.microcol.gui.util.AbstractEventController;

/**
 * Control event when some tile is focused.
 */
public class TileWasSelectedController extends AbstractEventController<TileWasSelectedEvent> {
	
	/**
	 * Make listeners synchronous by default.
	 */
	TileWasSelectedController(){
		super(false);
	}

}
