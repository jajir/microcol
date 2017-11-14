package org.microcol.gui.util;

import javafx.scene.layout.VBox;

/**
 * Create yes/no or cancel/OK dialog.
 */
public abstract class AbstractDialog extends AbstractMessageWindow {

	private final VBox container = new VBox();
	
	
	public AbstractDialog(final ViewUtil viewUtil) {
		super(viewUtil);
		init(container);
	}

}
