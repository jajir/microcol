package org.microcol.gui.screen.editor;

import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class ScreenEditor implements GameScreen {

    public static final String STYLE_SHEET_EDITOR = ScreenEditor.class
            .getResource("/gui/Editor.css").toExternalForm();

    private final HBox mainPanel = new HBox();

    private final EditorPanel editorPanel;

    @Inject
    ScreenEditor(final TerrainPanel terrainPanel, final EditorPanel editorPanel) {
        this.editorPanel = Preconditions.checkNotNull(editorPanel);

        mainPanel.getStylesheets().add(STYLE_SHEET_EDITOR);
        mainPanel.getChildren().add(editorPanel.getContent());
        mainPanel.getChildren().add(terrainPanel.getContent());
    }

    /**
     * it's called when key is pressed on game screen.
     *
     * @param event
     *            required key event
     */
    void setOnKeyPressed(EventHandler<? super KeyEvent> event) {
        mainPanel.setOnKeyPressed(event);
    }

    /**
     * it's called when key is release on game screen.
     *
     * @param event
     *            required key event
     */
    void setOnKeyReleased(EventHandler<? super KeyEvent> event) {
        mainPanel.setOnKeyReleased(event);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        editorPanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void beforeShow() {
        editorPanel.beforeShow();
    }

    @Override
    public void beforeHide() {
        editorPanel.beforeHide();
    }

}
