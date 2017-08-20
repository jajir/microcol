package org.microcol.gui.util;

import com.google.common.base.Preconditions;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplify dialog initialization.
 */
public class AbstractDialog {

	protected final static String KEY_DIALOG_OK = "dialog.ok";
	
	private final ViewUtil viewUtil;

	private final Stage stageDialog;

	private Scene scene;

	// TODO JJ main Vbox should be handled here

	public AbstractDialog(final ViewUtil viewUtil) {
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		stageDialog = new Stage(StageStyle.UNDECORATED);
		initInternal();
	}

	private final void initInternal() {
		stageDialog.initModality(Modality.WINDOW_MODAL);
		stageDialog.initOwner(viewUtil.getPrimaryStage());

		/**
		 * Following code show dialog centered to parent window.
		 */
		getDialog().setOnShowing(e -> stageDialog.hide());

		stageDialog.setOnShown(ev -> {
			stageDialog.setX(getViewUtil().getPrimaryStageCenterXPosition() - stageDialog.getWidth() / 2d);
			stageDialog.setY(getViewUtil().getPrimaryStageCenterYPosition() - stageDialog.getHeight() / 2d);
			stageDialog.show();
		});

	}

	public final void init(final Parent root) {
		scene = new Scene(root);
		scene.getStylesheets().add("gui/dialogs.css");
		stageDialog.setScene(scene);
		/**
		 * Following code manually close dialog. If it's not manually closed JVM
		 * crash. According to bug
		 * https://bugs.openjdk.java.net/browse/JDK-8172847 it will be fixed.
		 */
		scene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				stageDialog.close();
			}
		});
	}

	public ViewUtil getViewUtil() {
		return viewUtil;
	}

	public Stage getDialog() {
		return stageDialog;
	}

	public Scene getScene() {
		return scene;
	}

}
