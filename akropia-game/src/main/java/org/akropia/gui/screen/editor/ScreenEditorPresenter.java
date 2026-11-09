package org.akropia.gui.screen.editor;

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Keyboard of the map editor: Escape leaves through the unsaved-changes guard,
 * the editor save shortcut saves, arrows move the selected square and Enter or
 * Space applies the chosen tool to it.
 */
@Singleton
public class ScreenEditorPresenter {

    private final EditorActions actions;

    private final ModelService modelService;

    private final MouseOperationManager mouseOperationManager;

    private final KeyboardShortcuts shortcuts;

    @Inject
    ScreenEditorPresenter(final ScreenEditor screenEditor,
            final EditorActions actions, final ModelService modelService,
            final MouseOperationManager mouseOperationManager,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.actions = Preconditions.checkNotNull(actions);
        this.modelService = Preconditions.checkNotNull(modelService);
        this.mouseOperationManager = Preconditions
                .checkNotNull(mouseOperationManager);
        screenEditor.getContent().setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        if (KeyCode.ESCAPE == event.getCode()) {
            event.consume();
            actions.leave();
        } else if (shortcuts.matches(ShortcutAction.EDITOR_SAVE, event)) {
            event.consume();
            actions.save();
        } else if (!(event.getTarget() instanceof TextInputControl)
                && event.getTarget() instanceof javafx.scene.canvas.Canvas) {
            onMapKey(event);
        }
    }

    private void onMapKey(final KeyEvent event) {
        final Location at = modelService.getSelected()
                .orElse(Location.of(1, 1));
        final Location next;
        switch (event.getCode()) {
        case LEFT:
            next = at.add(Location.of(-1, 0));
            break;
        case RIGHT:
            next = at.add(Location.of(1, 0));
            break;
        case UP:
            next = at.add(Location.of(0, -1));
            break;
        case DOWN:
            next = at.add(Location.of(0, 1));
            break;
        case ENTER:
        case SPACE:
            event.consume();
            modelService.select(at);
            mouseOperationManager.getMouseOperation()
                    .ifPresent(operation -> operation.execute(at));
            return;
        default:
            return;
        }
        event.consume();
        if (modelService.getDraft().contains(next)) {
            modelService.select(next);
        }
    }

}
