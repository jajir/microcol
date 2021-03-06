package org.microcol.gui.util;

import org.microcol.gui.MainStageBuilder;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplify any dialog or message window.
 */
public abstract class AbstractMessageWindow implements UpdatableLanguage {

    private final static double OFFSCREEN_X = 10000;

    private final static double OFFSCREEN_Y = 10000;

    private final ViewUtil viewUtil;

    private final Stage stageDialog;

    private Scene scene;

    private final I18n i18n;

    public AbstractMessageWindow(final ViewUtil viewUtil, final I18n i18n) {
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.i18n = Preconditions.checkNotNull(i18n);
        stageDialog = new Stage(StageStyle.UNDECORATED);
        stageDialog.initModality(Modality.WINDOW_MODAL);
        stageDialog.initOwner(viewUtil.getPrimaryStage());
    }

    @Override
    public void updateLanguage(final I18n i18n) {

    }

    public void showAndWait() {
        updateLanguage(i18n);
        /**
         * Following code show dialog centered to parent window.
         * <p>
         * By default are dialogs centered on main screen.
         * </p>
         */
        if (Double.isNaN(stageDialog.getWidth()) || Double.isNaN(stageDialog.getHeight())) {
            placeStageAt(stageDialog, OFFSCREEN_X, OFFSCREEN_Y);
            stageDialog.show();
            stageDialog.hide();
        }
        centerStage(stageDialog, stageDialog.getWidth(), stageDialog.getHeight());
        stageDialog.showAndWait();
    }

    private void centerStage(final Stage stage, final double width, final double height) {
        placeStageAt(stage, getViewUtil().getPrimaryStageCenterXPosition() - width / 2,
                getViewUtil().getPrimaryStageCenterYPosition() - height / 2);
    }

    private void placeStageAt(final Stage stage, final double x, final double y) {
        stage.setX(x);
        stage.setY(y);
    }

    public final void init(final Parent root) {
        scene = new Scene(root);
        scene.getStylesheets().add(MainStageBuilder.STYLE_SHEET_DIALOGS);
        stageDialog.setScene(scene);
        /**
         * Following code manually close dialog. If it's not manually closed JVM
         * crash. According to bug
         * https://bugs.openjdk.java.net/browse/JDK-8172847 it will be fixed.
         */
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                onCancelDialog();
            }
        });
    }

    protected void onCancelDialog() {
        stageDialog.close();
    }

    public ViewUtil getViewUtil() {
        return viewUtil;
    }

    public void setTitle(final String title) {
        stageDialog.setTitle(title);
    }

    public void close() {
        stageDialog.close();
    }

    public Scene getScene() {
        return scene;
    }

}
