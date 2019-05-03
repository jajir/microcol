package org.microcol.gui.screen.editor;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenEditorPresenter {

    private final EventBus eventBus;

    @Inject
    ScreenEditorPresenter(final ScreenEditor screenEditor, final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        screenEditor.getContent().setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.MENU));
        }
    }

}
