package org.akropia.gui.screen.editor;

import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenEditorPresenter {

    private final EventBus eventBus;

    private final ModelService modelService;

    private final KeyboardShortcuts shortcuts;

    @Inject
    ScreenEditorPresenter(final ScreenEditor screenEditor,
            final EventBus eventBus, final ModelService modelService,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.modelService = Preconditions.checkNotNull(modelService);
        screenEditor.getContent().setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.MENU));
        }

        if (shortcuts.matches(ShortcutAction.EDITOR_SAVE, event)) {
            modelService.save();
        }
    }

}
