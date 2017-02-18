package org.microcol.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * 
 */
public class ViewUtil {

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

}
