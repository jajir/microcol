package org.microcol.gui.screen.menu;

import static org.microcol.gui.screen.AbstractScreenMenu.STYLE_SHEET_GAME_MENU;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.model.Player;
import org.microcol.model.campaign.FreePlayMap;
import com.google.common.eventbus.EventBus;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Menu-styled setup with real map selection and a nation preview. */
@Singleton
public final class ScreenFreePlay extends AbstractScreenMenu
        implements GameScreen {

    private static final String ID_MAP = "freePlayMap";
    private static final String ID_NATION = "freePlayNation";
    private static final String ID_NATION_NOTE = "freePlayNationNote";
    private static final String ID_PLAYER_NAME = "freePlayPlayerName";
    private static final String ID_PLAYER_ERROR = "freePlayNameError";
    private static final String ID_START = "freePlayStart";
    private static final String ID_BACK = "freePlayBack";
    private static final String ID_FORM = "freePlaySetup";

    private final MenuHolderPanel holder;
    private final ApplicationController applicationController;
    private final EventBus eventBus;
    private final I18n i18n;

    private final VBox form;
    private final Label mapLabel;
    private final ChoiceBox<String> mapChoiceBox;
    private final Label nameLabel;
    private final Label nationLabel;
    private final ChoiceBox<String> nationChoiceBox;
    private final Label nationNoteLabel;
    private final TextField nameField;
    private final Label errorLabel;
    private final Button startButton;
    private final Button backButton;

    private boolean starting = false;

    @Inject
    public ScreenFreePlay(MenuHolderPanel holder,
            ApplicationController applicationController, EventBus eventBus,
            I18n i18n) {
        this.holder = holder;
        this.applicationController = applicationController;
        this.eventBus = eventBus;
        this.i18n = i18n;

        form = new VBox(10);
        form.setId(ID_FORM);
        form.getStyleClass().add("game-menu-inner");
        form.setPrefWidth(420);
        form.setPadding(new Insets(10));

        mapLabel = new Label();
        mapLabel.setLabelFor(mapChoiceBox = new ChoiceBox<>());
        mapChoiceBox.setId(ID_MAP);
        mapChoiceBox.getItems().addAll(i18n.get(GameMenu.setupMapOriginal),
                i18n.get(GameMenu.setupMapTwoIslands));
        mapChoiceBox.getSelectionModel().selectFirst();

        nationLabel = new Label();
        nationLabel.setLabelFor(nationChoiceBox = new ChoiceBox<>());
        nationChoiceBox.setId(ID_NATION);
        nationChoiceBox.getItems().addAll(i18n.get(GameMenu.setupNationDutch),
                i18n.get(GameMenu.setupNationFrench),
                i18n.get(GameMenu.setupNationEnglish),
                i18n.get(GameMenu.setupNationSpanish));
        nationChoiceBox.getSelectionModel().selectFirst();

        nationNoteLabel = new Label();
        nationNoteLabel.setId(ID_NATION_NOTE);
        nationNoteLabel.setWrapText(true);

        nameLabel = new Label();
        nameLabel.setLabelFor(nameField = new TextField());
        nameField.setId(ID_PLAYER_NAME);
        nameField.setText("Dutch");
        nameField.textProperty().addListener((obs, old, val) -> validateName());

        errorLabel = new Label();
        errorLabel.setId(ID_PLAYER_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(40);

        startButton = new Button();
        startButton.setId(ID_START);
        startButton.setDefaultButton(true);
        startButton.setDisable(true);
        startButton.setOnAction(e -> onStart());

        backButton = new Button();
        backButton.setId(ID_BACK);
        backButton.setCancelButton(true);
        backButton.setOnAction(e -> onBack());

        HBox buttonBox = new HBox(10, startButton, backButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        form.getChildren().addAll(mapLabel, mapChoiceBox, nationLabel,
                nationChoiceBox, nationNoteLabel, nameLabel, nameField,
                errorLabel, buttonBox);

        form.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                eventBus.post(new ShowScreenEvent(Screen.MENU));
                e.consume();
            }
        });

        updateLanguage(i18n);

        holder.setMenuPanel(() -> form);
        holder.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
    }

    @Override
    public Region getContent() {
        return holder.getContent();
    }

    @Override
    public void beforeShow() {
        starting = false;
        validateName();
        holder.beforeShow();
    }

    @Override
    public void beforeHide() {
        holder.beforeHide();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        int mapIndex = mapChoiceBox.getSelectionModel().getSelectedIndex();
        int nationIndex = nationChoiceBox.getSelectionModel()
                .getSelectedIndex();

        holder.setTitle(i18n.get(GameMenu.setupTitle));
        mapLabel.setText(i18n.get(GameMenu.setupMap));
        nationLabel.setText(i18n.get(GameMenu.setupNation));
        nameLabel.setText(i18n.get(GameMenu.setupPlayerName));
        nationNoteLabel.setText(i18n.get(GameMenu.setupNationNote));
        startButton.setText(i18n.get(GameMenu.setupStart));
        backButton.setText(i18n.get(GameMenu.buttonBack));

        mapChoiceBox.getItems().setAll(i18n.get(GameMenu.setupMapOriginal),
                i18n.get(GameMenu.setupMapTwoIslands));
        nationChoiceBox.getItems().setAll(i18n.get(GameMenu.setupNationDutch),
                i18n.get(GameMenu.setupNationFrench),
                i18n.get(GameMenu.setupNationEnglish),
                i18n.get(GameMenu.setupNationSpanish));

        mapChoiceBox.getSelectionModel().select(Math.max(0,
                Math.min(mapIndex, mapChoiceBox.getItems().size() - 1)));
        nationChoiceBox.getSelectionModel().select(Math.max(0,
                Math.min(nationIndex, nationChoiceBox.getItems().size() - 1)));

        validateName();
    }

    private boolean validateName() {
        String text = nameField.getText();
        try {
            Player.normalizeDisplayName(text);
            errorLabel.setText("");
            startButton.setDisable(starting);
            return true;
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(i18n.get(GameMenu.setupNameError));
            startButton.setDisable(true);
            return false;
        }
    }

    private void onStart() {
        if (starting || !validateName())
            return;
        starting = true;
        startButton.setDisable(true);
        FreePlayMap map = mapChoiceBox.getSelectionModel()
                .getSelectedIndex() == 0 ? FreePlayMap.ORIGINAL
                        : FreePlayMap.TWO_ISLANDS;
        applicationController.startNewFreeGame(map, nameField.getText());
    }

    private void onBack() {
        eventBus.post(new ShowScreenEvent(Screen.MENU));
    }
}
