package org.microcol.gui.util;

import com.google.common.base.Preconditions;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplify dialog initialization.
 */
public class AbstractDialog {

	private final ViewUtil viewUtil;

	private final Stage dialog;

	private Scene scene;
	
	
	//TODO JJ main Vbox should be handled here

	public AbstractDialog(final ViewUtil viewUtil) {
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		dialog = new Stage(StageStyle.UNDECORATED);
		initInternal();
	}

	private final void initInternal() {
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

	public final void init(final Parent root) {
		scene = new Scene(root);
		scene.getStylesheets().add("gui/dialogs.css");
		dialog.setScene(scene);
	}

	public ViewUtil getViewUtil() {
		return viewUtil;
	}

	public Stage getDialog() {
		return dialog;
	}

	public Scene getScene() {
		return scene;
	}

}
