package org.microcol.gui.event.model;

import java.util.function.Consumer;

import org.microcol.gui.DialogMessage;
import org.microcol.gui.gamepanel.AnimationIsDoneEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.OneTimeExecuter;
import org.microcol.gui.util.Text;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Class is called from mission instance when mission want's to say something to
 * player.
 *
 *
 * TODO change it interface and move to campaign package.
 */
@Listener
public final class MissionCallBack {

    private final Text text;

    private final DialogMessage dialogMessage;

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final OneTimeExecuter<Model> executor = new OneTimeExecuter<>();

    @Inject
    MissionCallBack(final Text text, final DialogMessage dialogMessage,
            final GameModelController gameModelController, final EventBus eventBus) {
        this.text = Preconditions.checkNotNull(text);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @SuppressWarnings("unused")
    @Subscribe
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

    /**
     * Execute given consumer in FX application thread.
     *
     * @param consumer
     *            required consumer
     */
    public void executeOnFrontEnd(final Consumer<CallBackContext> consumer) {
        Preconditions.checkNotNull(consumer);
        final CallBackContext callBackContext = new CallBackContext(dialogMessage, eventBus, text);
        Platform.runLater(() -> {
            consumer.accept(callBackContext);
        });
    }

    public void addCallWhenReady(final Consumer<Model> consumer) {
        executor.addCallWhenReady(consumer);
    }

}
