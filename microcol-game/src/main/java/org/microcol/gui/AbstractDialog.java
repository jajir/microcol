package org.microcol.gui;

import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplify dialog initialization.
 */
public class AbstractDialog {

	private final ViewUtil viewUtil;

	private final Stage dialog;

	AbstractDialog(final ViewUtil viewUtil) {
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		dialog = new Stage(StageStyle.UNDECORATED);
		init();
	}

	private final void init() {
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getPrimaryStage());

		/**
		 * Following code show dialog centered to parent window.
		 */
		getDialog().setOnShowing(e -> dialog.hide());

		dialog.setOnShown(ev -> {
			dialog.setX(getViewUtil().getPrimaryStageCenterXPosition() - dialog.getWidth() / 2d);
			dialog.setY(getViewUtil().getPrimaryStageCenterYPosition() - dialog.getHeight() / 2d);
			dialog.show();
		});

	}

	public ViewUtil getViewUtil() {
		return viewUtil;
	}

	public Stage getDialog() {
		return dialog;
	}

}
