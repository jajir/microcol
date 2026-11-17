package org.akropia.gui.util;

import java.util.List;

import org.akropia.gui.MainStageBuilder;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplify any dialog or message window.
 */
public abstract class AbstractMessageWindow implements UpdatableLanguage {

    private final ViewUtil viewUtil;

    private final Stage stageDialog;

    private Scene scene;

    private final I18n i18n;

    public AbstractMessageWindow(final ViewUtil viewUtil, final I18n i18n) {
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.i18n = Preconditions.checkNotNull(i18n);
        stageDialog = new Stage(StageStyle.TRANSPARENT);
        stageDialog.initModality(Modality.WINDOW_MODAL);
        stageDialog.initOwner(viewUtil.getPrimaryStage());
        // A reopened native window must exist before its position is assigned.
        stageDialog.setOnShown(event -> javafx.application.Platform
                .runLater(() -> centerStage(stageDialog, stageDialog.getWidth(),
                        stageDialog.getHeight())));
    }

    @Override
    public void updateLanguage(final I18n i18n) {

    }

    public void showAndWait() {
        updateLanguage(i18n);
        stageDialog.showAndWait();
    }

    private void centerStage(final Stage stage, final double width,
            final double height) {
        placeStageAt(stage,
                getViewUtil().getPrimaryStageCenterXPosition() - width / 2,
                getViewUtil().getPrimaryStageCenterYPosition() - height / 2);
    }

    /**
     * Places the dialog and keeps it inside the usable area of the screen that
     * holds the main window, so a tall dialog is never centered half off it.
     */
    private void placeStageAt(final Stage stage, final double x,
            final double y) {
        final Stage owner = getViewUtil().getPrimaryStage();
        final List<Screen> screens = Screen.getScreensForRectangle(owner.getX(),
                owner.getY(), Math.max(1, owner.getWidth()),
                Math.max(1, owner.getHeight()));
        final Rectangle2D area = (screens.isEmpty() ? Screen.getPrimary()
                : screens.get(0)).getVisualBounds();
        stage.setX(Math.max(area.getMinX(),
                Math.min(x, area.getMaxX() - stage.getWidth())));
        stage.setY(Math.max(area.getMinY(),
                Math.min(y, area.getMaxY() - stage.getHeight())));
    }

    public final void init(final Parent root) {
        scene = new Scene(new ModalFrame(root), Color.TRANSPARENT);
        viewUtil.installSceneShortcuts(scene);
        scene.getStylesheets().add(MainStageBuilder.STYLE_SHEET_DIALOGS);
        stageDialog.setScene(scene);
        // Handle cancellation before a focused control consumes Escape.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                for (Node node = scene
                        .getFocusOwner(); node != null; node = node
                                .getParent()) {
                    if (node instanceof ComboBoxBase<?>
                            && ((ComboBoxBase<?>) node).isShowing()
                            || node instanceof ChoiceBox<?>
                                    && ((ChoiceBox<?>) node).isShowing()
                            || node instanceof MenuButton
                                    && ((MenuButton) node).isShowing()) {
                        return;
                    }
                }
                final Button cancel = findCancelButton(root);
                if (cancel == null) {
                    onCancelDialog();
                } else {
                    cancel.fire();
                }
                event.consume();
            }
        });
    }

    private Button findCancelButton(final Node node) {
        if (!node.isVisible() || node.isDisabled()) {
            return null;
        }
        if (node instanceof Button && ((Button) node).isCancelButton()) {
            return (Button) node;
        }
        if (node instanceof Parent) {
            for (final Node child : ((Parent) node).getChildrenUnmodifiable()) {
                final Button cancel = findCancelButton(child);
                if (cancel != null) {
                    return cancel;
                }
            }
        }
        return null;
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
