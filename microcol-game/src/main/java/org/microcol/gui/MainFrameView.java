package org.microcol.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * MicroCol's main frame.
 */
public class MainFrameView extends JFrame implements MainFramePresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final GamePreferences gamePreferences;

	private final CardLayout cardLayout;

	private final Scene scene;
	
	private final VBox box;

	@Inject
	public MainFrameView(final MainPanelView mainPanelView, final StartPanelView startPanelView,
			final MainMenuView mainMenu, final GamePreferences gamePreferences) {
		super("MicroCol");
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		// setJMenuBar(mainMenu);
		cardLayout = new CardLayout();
		getContentPane().setLayout(cardLayout);
		getContentPane().add(mainPanelView, MainFramePresenter.MAIN_GAME_PANEL);
		getContentPane().add(startPanelView, MainFramePresenter.START_PANEL);
		cardLayout.show(getContentPane(), MainFramePresenter.MAIN_GAME_PANEL);
		loadPreferences();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		box = new VBox();
		scene = new Scene(box);
		
		HBox buttons =new HBox();
		box.getChildren().add(buttons);
		
		Button buttonCurrent = new Button("New game!!");
		buttons.getChildren().add(buttonCurrent);
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
			setBounds(getDefaultWindowSizeAndPosition());
			setLocationRelativeTo(null);
		}
	}

	/**
	 * When screen resolution is higher that 800 x 600 than create window of 2 /
	 * 3 of visible monitor size and center it.
	 * 
	 * @return default monitor size
	 */
	private Rectangle getDefaultWindowSizeAndPosition() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		width = width * 2 / 3;
		height = height * 2 / 3;
		if (width < 800) {
			width = 800;
		}
		if (height < 600) {
			height = 600;
		}
		int positonX = (int) ((screenSize.getWidth() - width) / 2);
		int positonY = (int) ((screenSize.getHeight() - height) / 2);
		return new Rectangle(positonX, positonY, (int) width, (int) height);
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
	public void showPanel(final String panelName) {
		cardLayout.show(getContentPane(), Preconditions.checkNotNull(panelName));
	}

	@Override
	public JFrame getFrame() {
		return this;
	}

	public Scene getScene() {
		return scene;
	}

	public VBox getBox() {
		return box;
	}

}
