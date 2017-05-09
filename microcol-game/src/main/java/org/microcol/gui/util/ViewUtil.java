package org.microcol.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;

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

	/**
	 * Set common dialog features and show it.
	 * 
	 * @param dialog
	 *            required dialog to show
	 */
	public void showDialog(final JDialog dialog) {
		Preconditions.checkNotNull(dialog);
		dialog.setUndecorated(true);
		dialog.setResizable(false);
		dialog.setModal(true);
		dialog.pack();
//		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	/**
	 * Center component on screen. It's useful for dialogs.
	 * 
	 * @param component
	 *            required centered component
	 * @return point where should be placed to left corner
	 */
	public Point centerWindow(final Component component) {
		Rectangle window = component.getBounds();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		return new Point((screen.width - window.width) / 2, (screen.height - window.height) / 2);
	}

	public Stage getParentFrame() {
		return parentFrame;
	}

}
