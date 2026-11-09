package org.akropia.gui.screen.editor;

import java.io.File;

import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

/**
 * Top menu of the map editor: New, Open, Save, Play and Back, with the name,
 * size and saved state of the draft.
 */
@Singleton
class EditorTopBar implements JavaFxComponent {

    static final String TITLE_ID = "editorTitle";

    private final HBox buttons = new HBox(8);

    private final VBox bar = new VBox(4);

    private final Button buttonNew = ActionButtons.button(ButtonIcon.ADD);

    private final Button buttonOpen = ActionButtons.button(ButtonIcon.FOLDER);

    private final Button buttonSave = ActionButtons.button(ButtonIcon.SAVE);

    private final Button buttonPlay = ActionButtons.button(ButtonIcon.PLAY);

    private final Button buttonBack = ActionButtons.button(ButtonIcon.BACK);

    private final Label title = new Label();

    private final ModelService modelService;

    private final I18n i18n;

    @Inject
    EditorTopBar(final EditorActions actions, final ModelService modelService,
            final I18n i18n) {
        this.modelService = Preconditions.checkNotNull(modelService);
        this.i18n = Preconditions.checkNotNull(i18n);
        buttonNew.setId("editorNew");
        buttonOpen.setId("editorOpen");
        buttonSave.setId("editorSave");
        buttonPlay.setId("editorPlay");
        buttonBack.setId("editorBack");
        title.setId(TITLE_ID);
        buttonNew.setOnAction(event -> actions.newMap());
        buttonOpen.setOnAction(event -> actions.open());
        buttonSave.setOnAction(event -> actions.save());
        buttonPlay.setOnAction(event -> actions.play());
        buttonBack.setOnAction(event -> actions.leave());
        bar.getStyleClass().add("editor-top");
        buttons.getChildren().addAll(buttonNew, buttonOpen, buttonSave,
                buttonPlay, buttonBack);
        // Commands keep their whole labels; the title wraps on its own line.
        for (final Button button : new Button[] { buttonNew, buttonOpen,
                buttonSave, buttonPlay, buttonBack }) {
            button.setMinWidth(Region.USE_PREF_SIZE);
        }
        title.setWrapText(true);
        title.setMinHeight(Region.USE_PREF_SIZE);
        bar.getChildren().addAll(buttons, title);
        modelService.addListener(this::refresh);
        updateLanguage();
    }

    void updateLanguage() {
        buttonNew.setText(i18n.get(EditorText.topNew));
        buttonOpen.setText(i18n.get(EditorText.topOpen));
        buttonSave.setText(i18n.get(EditorText.topSave));
        buttonPlay.setText(i18n.get(EditorText.topPlay));
        buttonBack.setText(i18n.get(EditorText.topBack));
        refresh();
    }

    private void refresh() {
        final String name = modelService.getFile().map(File::getName)
                .orElse(i18n.get(EditorText.untitled));
        title.setText(i18n.get(
                modelService.isDirty() ? EditorText.titleModified
                        : EditorText.titleSaved,
                name, modelService.getMapSize().getX(),
                modelService.getMapSize().getY()));
    }

    @Override
    public Region getContent() {
        return bar;
    }

}
