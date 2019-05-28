package org.microcol.gui.buttonpanel;

import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

/**
 * One button from top on screen buttons.
 *
 * @param <T>
 *            message key that will shown in status bar when mouse is over
 *            button
 */
class ButtonImage<T extends Enum<T> & MessageKeyResource> implements JavaFxComponent {

    private final Source source;
    private final EventBus eventBus;
    private final I18n i18n;
    private final Button button = new Button();
    private final T buttonKey;

    protected ButtonImage(final EventBus eventBus, final Source source, final I18n i18n,
            final T buttonKey, final Image image) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.buttonKey = Preconditions.checkNotNull(buttonKey);

        final BackgroundImage backgroundImage = new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.RIGHT, 0.5, true, Side.TOP, 0.5, true),
                BackgroundSize.DEFAULT);

        button.setBackground(new Background(backgroundImage));
        button.setOnMouseEntered(this::onMouseEntered);
        button.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(buttonKey), source));
    }

    private void onMouseExited(@SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(source));
    }

    @Override
    public Button getContent() {
        return button;
    }

}
