package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;

/**
 * Provide setting of correct cursor.
 * <p>
 * This functionality should be in separate service because in headless mode
 * work with cursor doesn't work. Problem is described in JDK bug <a href=
 * "https://bugs.openjdk.java.net/browse/JDK-8088837">https://bugs.openjdk.java.net/browse/JDK-8088837</a>
 * </p>
 */
public class CursorServiceImpl implements CursorService {

    private final Cursor gotoModeCursor;

    @Inject
    CursorServiceImpl(final ImageProvider imageProvider) {
        gotoModeCursor = new ImageCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO), 1,
                1);
    }

    /* (non-Javadoc)
     * @see org.microcol.gui.gamepanel.CursorService2#setMoveCursor(javafx.scene.Node)
     */
    @Override
    public void setMoveCursor(final Node node) {
        Preconditions.checkNotNull(node).setCursor(gotoModeCursor);
    }

    /* (non-Javadoc)
     * @see org.microcol.gui.gamepanel.CursorService2#setDefaultCursor(javafx.scene.Node)
     */
    @Override
    public void setDefaultCursor(final Node node) {
        Preconditions.checkNotNull(node).setCursor(Cursor.DEFAULT);
    }

}
