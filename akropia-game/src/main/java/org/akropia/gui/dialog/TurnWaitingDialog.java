package org.akropia.gui.dialog;

import java.util.concurrent.CountDownLatch;

import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Non-dismissable wait; the FX pulse keeps rowing while the model works. */
public final class TurnWaitingDialog extends AbstractMessageWindow {

    private final Timeline rowing = new Timeline();
    private final PauseTransition minimum = new PauseTransition(
            Duration.seconds(2));
    private final CountDownLatch closed = new CountDownLatch(1);
    private boolean minimumElapsed;
    private boolean workFinished;

    @Inject
    public TurnWaitingDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        final Image image = new Image(
                getClass().getResourceAsStream("/images/turn-rowers.png"));
        final ImageView view = new ImageView(image);
        view.setId("turn-wait-rowers");
        view.setFitWidth(588);
        view.setFitHeight(392);
        for (int frame = 0; frame < 6; frame++) {
            final Rectangle2D viewport = new Rectangle2D(frame % 3 * 627 + 2,
                    frame / 3 * 418 + 2, 623, 414);
            if (frame == 0) {
                view.setViewport(viewport);
            }
            rowing.getKeyFrames().add(new KeyFrame(Duration.millis(frame * 220),
                    event -> view.setViewport(viewport)));
        }
        rowing.getKeyFrames().add(new KeyFrame(Duration.millis(1320)));
        rowing.setCycleCount(Timeline.INDEFINITE);
        final Label message = new Label(i18n.get(Dialog.turnWait_message));
        message.setId("turn-wait-message");
        message.setWrapText(true);
        message.setMaxWidth(588);
        final VBox content = new VBox(16, view, message);
        content.setId("turn-wait");
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        init(content);
        setTitle(i18n.get(Dialog.turnWait_title));
        getScene().addEventFilter(KeyEvent.ANY, event -> event.consume());
        final Stage stage = (Stage) getScene().getWindow();
        stage.setOnCloseRequest(event -> event.consume());
        stage.setOnHidden(event -> {
            rowing.stop();
            minimum.stop();
            closed.countDown();
        });
        minimum.setOnFinished(event -> {
            minimumElapsed = true;
            closeWhenReady();
        });
    }

    /** Called on the FX thread, before starting any model work. */
    public void showWaiting() {
        ((Stage) getScene().getWindow()).show();
        rowing.playFromStart();
        minimum.playFromStart();
    }

    /** Called on the FX thread, including when model work failed. */
    public void finishWaiting() {
        workFinished = true;
        closeWhenReady();
    }

    private void closeWhenReady() {
        if (minimumElapsed && workFinished) {
            close();
        }
    }

    /** Only the model worker waits; never block the FX animation thread. */
    public void awaitClosed() throws InterruptedException {
        closed.await();
    }

    @Override
    protected void onCancelDialog() {
        // Escape cannot cancel a partially processed turn.
    }
}
