package org.microcol.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * MicroCol's main frame.
 */
public class MainFrame extends JFrame {
	private static final String PREFERENCES_PATH = "microcol/main-frame";

	private static final String PREFERENCES_STATE = "state";
	private static final String PREFERENCES_X = "x";
	private static final String PREFERENCES_Y = "y";
	private static final String PREFERENCES_WIDTH = "width";
	private static final String PREFERENCES_HEIGHT = "height";

	private Rectangle lastNormalBounds;

	private JScrollPane scrollPaneGamePanel;

	public MainFrame() {
		super("MicroCol");

		init();
	}

	private void init() {
		GamePanel gamePanel = new GamePanel(this);

		scrollPaneGamePanel = new JScrollPane(gamePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPaneGamePanel);

		loadPreferences();

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent event) {
				if (getExtendedState() == NORMAL) {
					lastNormalBounds = getBounds();
				}
			}

			@Override
			public void componentResized(ComponentEvent event) {
				if (getExtendedState() == NORMAL) {
					lastNormalBounds = getBounds();
				}
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				savePreferences();
			}
		});
	}

	public void mouseDragged(final int x, final int y) {
		scrollPaneGamePanel.getVerticalScrollBar().setValue(scrollPaneGamePanel.getVerticalScrollBar().getValue() - y);
		scrollPaneGamePanel.getHorizontalScrollBar().setValue(scrollPaneGamePanel.getHorizontalScrollBar().getValue() - x);
	}

	private void loadPreferences() {
		final Preferences prefs = getPreferences();

		final int state = prefs.getInt(PREFERENCES_STATE, Integer.MIN_VALUE);
		final int x = prefs.getInt(PREFERENCES_X, Integer.MIN_VALUE);
		final int y = prefs.getInt(PREFERENCES_Y, Integer.MIN_VALUE);
		final int width = prefs.getInt(PREFERENCES_WIDTH, Integer.MIN_VALUE);
		final int height = prefs.getInt(PREFERENCES_HEIGHT, Integer.MIN_VALUE);

		if (state != Integer.MIN_VALUE && x != Integer.MIN_VALUE && y != Integer.MIN_VALUE
			&& width != Integer.MIN_VALUE && height != Integer.MIN_VALUE) {
			setBounds(x, y, width, height);
			if ((state & ICONIFIED) != ICONIFIED) {
				setExtendedState(state);
			}
		} else {
			pack();
			setLocationRelativeTo(null);
		}
	}

	private void savePreferences() {
		final Preferences prefs = getPreferences();

		final Rectangle normalBounds = getExtendedState() == NORMAL ? getBounds() : lastNormalBounds;

		prefs.putInt(PREFERENCES_STATE, getExtendedState());
		prefs.putInt(PREFERENCES_X, normalBounds.x);
		prefs.putInt(PREFERENCES_Y, normalBounds.y);
		prefs.putInt(PREFERENCES_WIDTH, normalBounds.width);
		prefs.putInt(PREFERENCES_HEIGHT, normalBounds.height);
	}

	private Preferences getPreferences() {
		return Preferences.userRoot().node(PREFERENCES_PATH);
	}
}
