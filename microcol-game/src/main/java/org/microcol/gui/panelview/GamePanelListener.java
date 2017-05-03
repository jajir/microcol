package org.microcol.gui.panelview;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.google.common.base.Preconditions;

/**
 * Provide information about component events (resize) to game panel.
 */
public class GamePanelListener implements ComponentListener {

	private final GamePanelPresenter.Display display;

	/**
	 * Default constructor.
	 * 
	 * @param display
	 *            required reference to game panel view.
	 */
	public GamePanelListener(final GamePanelPresenter.Display display) {
		this.display = Preconditions.checkNotNull(display);
	}

	@Override
	public void componentShown(final ComponentEvent e) {
		/**
		 * Intentionally empty
		 */
	}

	@Override
	public void componentResized(final ComponentEvent e) {
		//TODO JJ should be called on presenter
		display.getGamePanelView().onViewPortResize();
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
		/**
		 * Intentionally empty
		 */
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
		/**
		 * Intentionally empty
		 */
	}
}
