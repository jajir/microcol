package org.microcol.gui;


import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBarView implements StatusBarPresenter.Display {

	private final HBox box;

	private final Label statusBarDescription;

	private final Label labelEra;

	@Inject
	public StatusBarView() {

		statusBarDescription = new Label();
		labelEra = new Label();
		
		//TODO JJ clean up class
		
//		add(statusBarDescription, new GridBagConstraints(0, 0, 1, 1, 1D, 0D, GridBagConstraints.CENTER,
//				GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 3), 0, 0));
//
//		add(new JSeparator(SwingConstants.VERTICAL), new GridBagConstraints(1, 0, 1, 1, 0D, 1D,
//				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 3, 0, 15), 0, 0));
//
//		add(labelEra, new GridBagConstraints(2, 0, 1, 1, 0D, 0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 3, 0, 15), 0, 0));
//		Border border = BorderFactory.createBevelBorder(EtchedBorder.LOWERED);
//		setBorder(border);
		
		box = new HBox();
		box.getChildren().addAll(statusBarDescription,labelEra);

	}

	@Override
	public Label getStatusBarDescription() {
		return statusBarDescription;
	}

	@Override
	public Label getLabelEra() {
		return labelEra;
	}

	public HBox getBox() {
		return box;
	}
}
