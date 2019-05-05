package org.microcol.gui.screen.editor;

import java.io.File;

import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class ScreenEditor implements GameScreen {

    public static final String STYLE_SHEET_EDITOR = ScreenEditor.class
            .getResource("/gui/Editor.css").toExternalForm();

    private final HBox mainPanel = new HBox();

    private final EditorPanel editorPanel;

    private final ModelService modelService;

    @Inject
    ScreenEditor(final TerrainPanel terrainPanel, final ModelService modelService,
            final EditorPanel editorPanel) {
        this.editorPanel = Preconditions.checkNotNull(editorPanel);
        this.modelService = Preconditions.checkNotNull(modelService);

        mainPanel.getStylesheets().add(STYLE_SHEET_EDITOR);
        mainPanel.getChildren().add(editorPanel.getContent());
        mainPanel.getChildren().add(terrainPanel.getContent());
    }

    public void loadSaveFile(final File file) {
        Preconditions.checkNotNull(file);
        modelService.load(file);
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
