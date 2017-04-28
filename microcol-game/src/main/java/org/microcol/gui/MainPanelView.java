package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.microcol.gui.panelview.GamePanelView;

import com.google.inject.Inject;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Panel hold whole game screen without status bar.
 */
public class MainPanelView extends JPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final VBox box;;

	@Inject
	public MainPanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
			final RightPanelView rightPanelView) {
		//TODO JJ remove swing classes
		this.setLayout(new GridBagLayout());
		JScrollPane scrollPaneGamePanel = new JScrollPane(gamePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneGamePanel.setBorder(BorderFactory.createEmptyBorder());

		add(scrollPaneGamePanel, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		add(rightPanelView, new GridBagConstraints(1, 0, 1, 1, 0.0D, 1.0D, GridBagConstraints.NORTH,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

		add(statusBar, new GridBagConstraints(0, 1, 2, 1, 1.0D, 0.0D, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		box = new VBox();
		HBox hBox = new HBox();
		hBox.getChildren().addAll(gamePanel.getCanvas());
		box.getChildren().addAll(hBox, statusBar.getBox());
	}

	public VBox getBox() {
		return box;
	}

}
