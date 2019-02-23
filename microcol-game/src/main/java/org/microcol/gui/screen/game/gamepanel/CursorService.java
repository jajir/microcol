package org.microcol.gui.screen.game.gamepanel;

import javafx.scene.Node;

/**
 * Provide setting of correct cursor.
 * <p>
 * This functionality should be in separate service because in headless mode
 * work with cursor doesn't work. Problem is described in JDK bug <a href=
 * "https://bugs.openjdk.java.net/browse/JDK-8088837">https://bugs.openjdk.java.net/browse/JDK-8088837</a>
 * </p>
 */
public interface CursorService {

    void setMoveCursor(Node node);

    void setDefaultCursor(Node node);

}