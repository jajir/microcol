package org.microcol.gui.screen.game.gamepanel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.Canvas;

/**
 * View for main game panel.
 */
@Singleton
final class MoveModeController {

    private final GamePanelComponent gamePanelComponent;

    private final CursorService cursorService;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    private final ModeController modeController;

    @Inject
    public MoveModeController(final GamePanelComponent gamePanelComponent,
            final ModeController modeController,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final CursorService cursorService) {
        this.gamePanelComponent = Preconditions.checkNotNull(gamePanelComponent);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        this.cursorService = Preconditions.checkNotNull(cursorService);
    }

    void setMoveModeOff() {
        oneTurnMoveHighlighter.setLocations(null);
        cursorService.setDefaultCursor(getCanvas());
        modeController.setMoveMode(false);
    }

    void setMoveModeOn() {
        cursorService.setMoveCursor(getCanvas());
        modeController.setMoveMode(true);
    }

    private Canvas getCanvas() {
        return gamePanelComponent.getCanvas();
    }

}
