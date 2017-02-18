package org.microcol.gui;

import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * MicroCol's main frame.
 */
public class MainFrameView extends JFrame implements MainFramePresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final GamePreferences gamePreferences;

	@Inject
	public MainFrameView(final MainPanelView mainPanel, final MainMenuView mainMenu,
			final GamePreferences gamePreferences) {
		super("MicroCol");
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		setJMenuBar(mainMenu);
		add(mainPanel);
		loadPreferences();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	private void loadPreferences() {
		final int state = gamePreferences.getMainFrameState();
		final Rectangle bound = gamePreferences.getMainFramePosition();

		if (areValuesSet(state, bound.x, bound.y, bound.width, bound.height)) {
			setBounds(bound);
			if ((state & ICONIFIED) != ICONIFIED) {
				setExtendedState(state);
			}
		} else {
			pack();
			setLocationRelativeTo(null);
		}
	}

	private boolean areValuesSet(int... ints) {
		for (int i = 0; i < ints.length; i++) {
			if (ints[i] == Integer.MIN_VALUE) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JFrame getFrame() {
		return this;
	}

}
