package org.akropia.gui.screen.editor;

import java.io.File;

import org.akropia.gui.screen.GameScreen;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/**
 * Developer map editor (R-0435): top menu, map view and right editing panel.
 */
@Singleton
public class ScreenEditor implements GameScreen {

    public static final String STYLE_SHEET_EDITOR = ScreenEditor.class
            .getResource("/gui/Editor.css").toExternalForm();

    private final BorderPane mainPanel = new BorderPane();

    private final EditorPanel editorPanel;

    private final EditorRightPanel rightPanel;

    private final EditorTopBar topBar;

    private final EditorActions actions;

    @Inject
    ScreenEditor(final EditorRightPanel rightPanel, final EditorTopBar topBar,
            final EditorPanel editorPanel, final EditorActions actions) {
        this.editorPanel = Preconditions.checkNotNull(editorPanel);
        this.rightPanel = Preconditions.checkNotNull(rightPanel);
        this.topBar = Preconditions.checkNotNull(topBar);
        this.actions = Preconditions.checkNotNull(actions);

        mainPanel.setId("mapEditor");
        mainPanel.getStylesheets().add(STYLE_SHEET_EDITOR);
        mainPanel.setTop(topBar.getContent());
        mainPanel.setCenter(editorPanel.getContent());
        mainPanel.setRight(rightPanel.getContent());
        mainPanel.setFocusTraversable(true);
    }

    /**
     * Open an authored scenario chosen by the developer load route; a rejected
     * file keeps the current draft.
     *
     * @param file required scenario file
     */
    public void loadFile(final File file) {
        Preconditions.checkNotNull(file);
        actions.openFile(file);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        topBar.updateLanguage();
        rightPanel.updateLanguage();
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
