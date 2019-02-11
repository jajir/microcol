package org.microcol.test;

import org.microcol.gui.gamepanel.CursorService;

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
