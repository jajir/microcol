package org.microcol.gui.screen.menu;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import static org.microcol.gui.screen.AbstractScreenMenu.STYLE_SHEET_GAME_MENU;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.Loc;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Player;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.generation.GeneratedClimate;
import org.microcol.model.generation.GeneratedLandShare;
import org.microcol.model.generation.GeneratedWorld;
import org.microcol.model.generation.GeneratedWorldSettings;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Menu setup for authored maps, selectable reference expeditions and generated
 * New Worlds. The generated choices are shown only for the generated map.
 */
@Singleton
public final class ScreenFreePlay extends AbstractScreenMenu
        implements GameScreen {

    private static final String ID_MAP = "freePlayMap";
    private static final String ID_GENERATED = "freePlayGenerated";
    private static final String ID_CLIMATE = "freePlayClimate";
    private static final String ID_LAND_SHARE = "freePlayLandShare";
    private static final String ID_SEED = "freePlaySeed";
    private static final String ID_PREVIEW = "freePlayGeneratedPreview";
    private static final String ID_DIFFICULTY = "freePlayDifficulty";
    private static final String ID_NATION = "freePlayNation";
    private static final String ID_NATION_NOTE = "freePlayNationNote";
    private static final String ID_PLAYER_NAME = "freePlayPlayerName";
    private static final String ID_PLAYER_ERROR = "freePlayNameError";
    private static final String ID_START = "freePlayStart";
    private static final String ID_BACK = "freePlayBack";
    private static final String ID_FORM = "freePlaySetup";

    /** Index of the generated choice, after the bundled maps. */
    private static final int GENERATED_INDEX = FreePlayMap.values().length;

    private static final String DEFAULT_SEED = "1";

    private final MenuHolderPanel holder;
    private final ApplicationController applicationController;
    private final EventBus eventBus;
    private final I18n i18n;
    private final LocalizationHelper localizationHelper;

    private final VBox form;
    private final Label mapLabel;
    private final ChoiceBox<String> mapChoiceBox;
    private final VBox generatedBox;
    private final Label climateLabel;
    private final ChoiceBox<GeneratedClimate> climateChoiceBox;
    private final Label landShareLabel;
    private final ChoiceBox<GeneratedLandShare> landShareChoiceBox;
    private final Label seedLabel;
    private final TextField seedField;
    private final Label previewLabel;
    private final Label difficultyLabel;
    private final ChoiceBox<Difficulty> difficultyChoiceBox;
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
            I18n i18n, LocalizationHelper localizationHelper) {
        this.holder = holder;
        this.applicationController = applicationController;
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.localizationHelper = localizationHelper;

        form = new VBox(10);
        form.setId(ID_FORM);
        form.getStyleClass().add("game-menu-inner");
        form.setPrefWidth(500);
        form.setPadding(new Insets(10));

        mapLabel = new Label();
        mapLabel.setLabelFor(mapChoiceBox = new ChoiceBox<>());
        mapChoiceBox.setId(ID_MAP);
        mapChoiceBox.getItems().addAll(mapNames(i18n));
        mapChoiceBox.getSelectionModel().selectFirst();

        climateLabel = new Label();
        climateLabel.setLabelFor(climateChoiceBox = new ChoiceBox<>());
        climateChoiceBox.setId(ID_CLIMATE);
        climateChoiceBox.getItems().addAll(GeneratedClimate.values());
        climateChoiceBox.getSelectionModel().select(GeneratedClimate.TEMPERATE);
        climateChoiceBox.setConverter(new StringConverter<GeneratedClimate>() {
            @Override
            public String toString(final GeneratedClimate value) {
                return value == null ? "" : climateName(value);
            }

            @Override
            public GeneratedClimate fromString(final String text) {
                throw new UnsupportedOperationException();
            }
        });

        landShareLabel = new Label();
        landShareLabel.setLabelFor(landShareChoiceBox = new ChoiceBox<>());
        landShareChoiceBox.setId(ID_LAND_SHARE);
        landShareChoiceBox.getItems().addAll(GeneratedLandShare.values());
        landShareChoiceBox.getSelectionModel()
                .select(GeneratedLandShare.MEDIUM);
        landShareChoiceBox
                .setConverter(new StringConverter<GeneratedLandShare>() {
                    @Override
                    public String toString(final GeneratedLandShare value) {
                        return value == null ? "" : landShareName(value);
                    }

                    @Override
                    public GeneratedLandShare fromString(final String text) {
                        throw new UnsupportedOperationException();
                    }
                });

        seedLabel = new Label();
        seedLabel.setLabelFor(seedField = new TextField(DEFAULT_SEED));
        seedField.setId(ID_SEED);

        previewLabel = new Label();
        previewLabel.setId(ID_PREVIEW);
        previewLabel.setWrapText(true);

        generatedBox = new VBox(6, fieldRow(climateLabel, climateChoiceBox),
                fieldRow(landShareLabel, landShareChoiceBox),
                fieldRow(seedLabel, seedField), previewLabel);
        generatedBox.setId(ID_GENERATED);

        difficultyLabel = new Label();
        difficultyLabel.setLabelFor(difficultyChoiceBox = new ChoiceBox<>());
        difficultyChoiceBox.setId(ID_DIFFICULTY);
        difficultyChoiceBox.getItems().addAll(Difficulty.values());
        difficultyChoiceBox.getSelectionModel().select(Difficulty.CONQUISTADOR);
        difficultyChoiceBox.setConverter(new StringConverter<Difficulty>() {
            @Override
            public String toString(final Difficulty value) {
                return value == null ? ""
                        : localizationHelper.getDifficultyName(value);
            }

            @Override
            public Difficulty fromString(final String text) {
                throw new UnsupportedOperationException();
            }
        });

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
        nameField.textProperty()
                .addListener((obs, old, val) -> validateInput());

        errorLabel = new Label();
        errorLabel.setId(ID_PLAYER_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(40);

        startButton = ActionButtons.button(ButtonIcon.PLAY);
        startButton.setId(ID_START);
        startButton.setDefaultButton(true);
        startButton.setDisable(true);
        startButton.setOnAction(e -> onStart());

        backButton = ActionButtons.button(ButtonIcon.BACK);
        backButton.setId(ID_BACK);
        backButton.setCancelButton(true);
        backButton.setOnAction(e -> onBack());

        HBox buttonBox = new HBox(10, startButton, backButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        form.getChildren().addAll(fieldRow(mapLabel, mapChoiceBox),
                generatedBox, fieldRow(difficultyLabel, difficultyChoiceBox),
                fieldRow(nationLabel, nationChoiceBox), nationNoteLabel,
                fieldRow(nameLabel, nameField), errorLabel, buttonBox);

        mapChoiceBox.getSelectionModel().selectedIndexProperty()
                .addListener((obs, old, value) -> onMapChanged());
        seedField.textProperty()
                .addListener((obs, old, value) -> validateInput());
        climateChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> updatePreview());
        landShareChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> updatePreview());

        form.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                eventBus.post(new ShowScreenEvent(Screen.MENU));
                e.consume();
            }
        });

        // Bundled maps are the default, so the generated controls start hidden.
        updateGeneratedControls();
        updateLanguage(i18n);

        holder.setMenuPanel(() -> form);
        holder.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
    }

    private static HBox fieldRow(final Label label, final Region control) {
        label.setMinWidth(180);
        final HBox row = new HBox(10, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @Override
    public Region getContent() {
        return holder.getContent();
    }

    @Override
    public void beforeShow() {
        starting = false;
        validateInput();
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
        climateLabel.setText(i18n.get(GameMenu.setupGeneratedClimate));
        landShareLabel.setText(i18n.get(GameMenu.setupGeneratedLand));
        seedLabel.setText(i18n.get(GameMenu.setupGeneratedSeed));
        nationLabel.setText(i18n.get(GameMenu.setupNation));
        difficultyLabel.setText(i18n.get(Loc.difficulty));
        final Difficulty selectedDifficulty = difficultyChoiceBox.getValue();
        difficultyChoiceBox.getSelectionModel().clearSelection();
        difficultyChoiceBox.getSelectionModel().select(selectedDifficulty);
        nameLabel.setText(i18n.get(GameMenu.setupPlayerName));
        startButton.setText(i18n.get(GameMenu.setupStart));
        backButton.setText(i18n.get(GameMenu.buttonBack));

        mapChoiceBox.getItems().setAll(mapNames(i18n));
        nationChoiceBox.getItems().setAll(i18n.get(GameMenu.setupNationDutch),
                i18n.get(GameMenu.setupNationFrench),
                i18n.get(GameMenu.setupNationEnglish),
                i18n.get(GameMenu.setupNationSpanish));

        mapChoiceBox.getSelectionModel().select(Math.max(0,
                Math.min(mapIndex, mapChoiceBox.getItems().size() - 1)));
        nationChoiceBox.getSelectionModel().select(Math.max(0,
                Math.min(nationIndex, nationChoiceBox.getItems().size() - 1)));
        // Re-select so the converted labels follow the new language.
        final GeneratedClimate climate = climateChoiceBox.getValue();
        climateChoiceBox.getSelectionModel().clearSelection();
        climateChoiceBox.getSelectionModel().select(climate);
        final GeneratedLandShare landShare = landShareChoiceBox.getValue();
        landShareChoiceBox.getSelectionModel().clearSelection();
        landShareChoiceBox.getSelectionModel().select(landShare);
        updateNationAvailability();
        updatePreview();
        validateInput();
    }

    private List<String> mapNames(final I18n i18n) {
        final List<String> names = new ArrayList<>();
        names.add(i18n.get(GameMenu.setupMapOriginal));
        names.add(i18n.get(GameMenu.setupMapTwoIslands));
        names.add(i18n.get(GameMenu.setupMapRivalExpedition));
        names.add(i18n.get(GameMenu.setupMapReference));
        names.add(i18n.get(GameMenu.setupMapGenerated));
        return names;
    }

    private void onMapChanged() {
        updateNationAvailability();
        updateGeneratedControls();
        validateInput();
    }

    private boolean isGeneratedSelected() {
        return mapChoiceBox.getSelectionModel()
                .getSelectedIndex() == GENERATED_INDEX;
    }

    private void updateGeneratedControls() {
        final boolean generated = isGeneratedSelected();
        generatedBox.setVisible(generated);
        generatedBox.setManaged(generated);
        updatePreview();
    }

    private void updateNationAvailability() {
        // Generated worlds use the same four expeditions as the reference one.
        final boolean reference = isGeneratedSelected()
                || mapChoiceBox.getSelectionModel()
                        .getSelectedIndex() == FreePlayMap.REFERENCE.ordinal();
        nationChoiceBox.setDisable(!reference);
        if (!reference) {
            nationChoiceBox.getSelectionModel().selectFirst();
        }
        nationNoteLabel
                .setText(i18n.get(reference ? GameMenu.setupNationReferenceNote
                        : GameMenu.setupNationNote));
    }

    private void updatePreview() {
        final GeneratedLandShare landShare = landShareChoiceBox.getValue();
        final Integer seed = parseSeed();
        // I18n formats the message itself, so the values are its arguments.
        previewLabel.setText(i18n.get(GameMenu.setupGeneratedPreview,
                GeneratedWorld.WIDTH, GeneratedWorld.HEIGHT,
                climateName(climateChoiceBox.getValue()),
                landShare == null ? 0 : landShare.getPercent(),
                seed == null ? "" : String.valueOf(seed)));
    }

    /**
     * @return seed typed in the field, or null when the text is not a valid
     *         seed
     */
    private Integer parseSeed() {
        try {
            return GeneratedWorldSettings.parseSeed(seedField.getText());
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    private String climateName(final GeneratedClimate climate) {
        if (climate == GeneratedClimate.COLD) {
            return i18n.get(GameMenu.setupClimateCold);
        }
        if (climate == GeneratedClimate.WARM) {
            return i18n.get(GameMenu.setupClimateWarm);
        }
        return i18n.get(GameMenu.setupClimateTemperate);
    }

    private String landShareName(final GeneratedLandShare landShare) {
        if (landShare == GeneratedLandShare.LOW) {
            return i18n.get(GameMenu.setupLandLow);
        }
        if (landShare == GeneratedLandShare.HIGH) {
            return i18n.get(GameMenu.setupLandHigh);
        }
        return i18n.get(GameMenu.setupLandMedium);
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

    /**
     * Checks the player name and, for the generated map, the seed. Start stays
     * disabled until every visible field is valid.
     */
    private boolean validateInput() {
        final boolean nameValid = validateName();
        if (!nameValid || !isGeneratedSelected()) {
            return nameValid;
        }
        updatePreview();
        if (parseSeed() == null) {
            errorLabel.setText(i18n.get(GameMenu.setupGeneratedSeedError));
            startButton.setDisable(true);
            return false;
        }
        return true;
    }

    private void onStart() {
        if (starting || !validateInput())
            return;
        starting = true;
        startButton.setDisable(true);
        final EuropeanNation nation = EuropeanNation.values()[nationChoiceBox
                .getSelectionModel().getSelectedIndex()];
        if (isGeneratedSelected()) {
            final GeneratedWorldSettings settings = new GeneratedWorldSettings(
                    parseSeed(), climateChoiceBox.getValue(),
                    landShareChoiceBox.getValue());
            if (!applicationController.startNewGeneratedGame(settings,
                    nameField.getText(), difficultyChoiceBox.getValue(),
                    nation)) {
                starting = false;
                errorLabel.setText(i18n.get(GameMenu.setupGeneratedFailed));
                startButton.setDisable(false);
            }
            return;
        }
        FreePlayMap map = FreePlayMap.values()[mapChoiceBox.getSelectionModel()
                .getSelectedIndex()];
        applicationController.startNewFreeGame(map, nameField.getText(),
                difficultyChoiceBox.getValue(), nation);
    }

    private void onBack() {
        eventBus.post(new ShowScreenEvent(Screen.MENU));
    }
}
