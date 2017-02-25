package org.microcol.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.microcol.gui.event.GameEventController;
import org.microcol.gui.event.KeyController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * MicroCol's main frame.
 */
public class MainFramePresenter {

	public interface Display {

		JFrame getFrame();
	}

	private final Logger logger = Logger.getLogger(MainFramePresenter.class);

	private Rectangle lastNormalBounds;

	private final GamePreferences gamePreferences;

	private final MainFramePresenter.Display display;

	@Inject
	public MainFramePresenter(final MainFramePresenter.Display display, final KeyController keyController,
			final GamePreferences gamePreferences, final GameEventController gameEventController) {
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.display = Preconditions.checkNotNull(display);
		display.getFrame().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				keyController.fireKeyWasPressed(e);
			}
		});

		display.getFrame().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent event) {
				if (display.getFrame().getExtendedState() == JFrame.NORMAL) {
					lastNormalBounds = display.getFrame().getBounds();
				}
			}

			@Override
			public void componentResized(ComponentEvent event) {
				if (display.getFrame().getExtendedState() == JFrame.NORMAL) {
					lastNormalBounds = display.getFrame().getBounds();
				}
			}
		});

		display.getFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				logger.debug("Event windowClosing");
				savePreferences();
				gameEventController.fireGameExit();
			}
		});
	}

	private void savePreferences() {
		final Rectangle normalBounds = display.getFrame().getExtendedState() == JFrame.NORMAL
				? display.getFrame().getBounds() : lastNormalBounds;
		gamePreferences.setMainFramePosition(normalBounds);
		gamePreferences.setMainFrameState(display.getFrame().getExtendedState());
	}

}
