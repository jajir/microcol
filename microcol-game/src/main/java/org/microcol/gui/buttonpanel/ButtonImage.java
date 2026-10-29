package org.microcol.gui.buttonpanel;

import org.microcol.gui.Loc;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * One button from top on screen buttons.
 *
 * @param <T> message key that will shown in status bar when mouse is over
 *            button
 */
class ButtonImage<T extends Enum<T> & MessageKeyResource>
        implements JavaFxComponent {

    private final Source source;
    private final EventBus eventBus;
    private final I18n i18n;
    private final Button button;
    private final T buttonKey;

    protected ButtonImage(final EventBus eventBus, final Source source,
            final I18n i18n, final T buttonKey, final ButtonIcon icon) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.buttonKey = Preconditions.checkNotNull(buttonKey);
        button = ActionButtons.button(icon);
        if (buttonKey.equals(Loc.backToGame)) {
            button.getStyleClass().add("back-to-game-button");
        }
        updateAccessibleName();
        button.sceneProperty().addListener(
                (observable, oldScene, newScene) -> updateAccessibleName());
        button.setOnMouseEntered(this::onMouseEntered);
        button.setOnMouseExited(this::onMouseExited);
    }

    private void updateAccessibleName() {
        final String label = i18n.get(buttonKey);
        if (buttonKey.equals(Loc.backToGame)) {
            button.setText(label);
        }
        // Full toolbars own their translated label and its accessibility
        // binding.
        if (!button.accessibleTextProperty().isBound()) {
            button.setAccessibleText(label);
        }
    }

    private void onMouseEntered(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(buttonKey), source));
    }

    private void onMouseExited(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(source));
    }

    @Override
    public Button getContent() {
        return button;
    }

}
