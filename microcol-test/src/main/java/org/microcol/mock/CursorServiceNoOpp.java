package org.microcol.mock;

import org.microcol.gui.screen.game.gamepanel.CursorService;

import javafx.scene.Node;

/**
 * No operation implementation of cursor service.
 */
public class CursorServiceNoOpp implements CursorService {

    @Override
    public void setMoveCursor(final Node node) {
	// do nothing
    }

    @Override
    public void setDefaultCursor(final Node node) {
	// do nothing
    }

}
