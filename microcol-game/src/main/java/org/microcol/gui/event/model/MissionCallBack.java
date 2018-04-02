package org.microcol.gui.event.model;

import java.util.function.Consumer;

import org.microcol.gui.DialogMessage;
import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.gamepanel.AnimationIsDoneController;
import org.microcol.gui.gamepanel.AnimationIsDoneEvent;
import org.microcol.gui.util.OneTimeExecuter;
import org.microcol.gui.util.Text;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Class is called from mission instance when mission want's to say something to
 * player.
 *
 *
 * TODO change it interface and move to campaign package.
 */
public class MissionCallBack {

    private final Text text;

    private final DialogMessage dialogMessage;

    private final GameModelController gameModelController;

    private final MainPanelPresenter mainPanelPresenter;

    private final OneTimeExecuter<Model> executor = new OneTimeExecuter<>();

    @Inject
    MissionCallBack(final Text text, final DialogMessage dialogMessage,
            final AnimationIsDoneController animationIsDoneController,
            final GameModelController gameModelController,
            final MainPanelPresenter mainPanelPresenter) {
        this.text = Preconditions.checkNotNull(text);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.mainPanelPresenter = Preconditions.checkNotNull(mainPanelPresenter);
        animationIsDoneController.addListener(this::onAnimationIsDone);
    }

    @SuppressWarnings("unused")
    private void onAnimationIsDone(final AnimationIsDoneEvent event) {
        executor.fire(gameModelController.getModel());
    }

    public void showMessage(final String... messageKeys) {
        Platform.runLater(() -> {
            for (final String messageKey : messageKeys) {
                dialogMessage.setText(text.get(messageKey));
                dialogMessage.showAndWait();
            }
        });
    }

    public void addCallWhenReady(final Consumer<Model> consumer) {
        executor.addCallWhenReady(consumer);
    }

    /**
     * This call cause that front end skip to next mission in campaign. It could
     * be called when mission is over.
     */
    public void startDefaultMission() {
        // FIXME gameController can't be instantiated here because of circular
        // dependencies
    }

    public void goToGameMenu() {
        mainPanelPresenter.showPanel(MainPanelPresenter.PANEL_GAME_MENU);
    }

}
