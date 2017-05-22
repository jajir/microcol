package org.microcol.gui.util;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.Stage;

/**
 * 
 */
public class ViewUtil {

	/**
	 * All game dialogs, windows and UI features are children of this main
	 * {@link javafx.stage.Stage}.
	 */
	private final Stage parentFrame;

	@Inject
	public ViewUtil(final Stage parentFrame) {
		this.parentFrame = Preconditions.checkNotNull(parentFrame);
	}

	public Stage getParentFrame() {
		return parentFrame;
	}

}
