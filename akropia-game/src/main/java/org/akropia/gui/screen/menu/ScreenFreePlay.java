package org.akropia.gui.screen.menu;

import static org.akropia.gui.screen.AbstractScreenMenu.STYLE_SHEET_GAME_MENU;
import java.util.List;
import java.util.UUID;
import org.akropia.gui.Loc;
import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.dialog.ApplicationController;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.AbstractScreenMenu;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.ActionButtons;
import org.akropia.i18n.I18n;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Player;
import org.akropia.model.campaign.FreePlayMap;
import org.akropia.model.generation.GeneratedLandLayout;
import org.akropia.model.generation.GeneratedMapSize;
import org.akropia.model.generation.GeneratedWorld;
import org.akropia.model.generation.GeneratedWorldSettings;
import org.akropia.model.store.ModelPo;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Two-step setup; only a completed, validated world replaces the active game.
 */
@Singleton
public final class ScreenFreePlay extends AbstractScreenMenu
        implements GameScreen {
    static final String ID_MAP = "freePlayMap";
    static final int GENERATED_INDEX = FreePlayMap.NORTH_AMERICA.ordinal();
    private static final int NORTH_AMERICA_INDEX = GENERATED_INDEX + 1;
    private final MenuHolderPanel holder;
    private final ApplicationController applicationController;
    private final EventBus eventBus;
    private final I18n i18n;
    private final LocalizationHelper localizationHelper;
    private final VBox form = new VBox(12);
    private final VBox world = new VBox(12);
    private final VBox player = new VBox(12);
    private final VBox generated = new VBox(12);
    private final ChoiceBox<String> maps = new ChoiceBox<>();
    private final ChoiceBox<GeneratedMapSize> sizes = new ChoiceBox<>();
    private final ChoiceBox<GeneratedLandLayout> layouts = new ChoiceBox<>();
    private final ChoiceBox<Difficulty> difficulties = new ChoiceBox<>();
    private final ChoiceBox<String> nations = new ChoiceBox<>();
    private final TextField seed = new TextField();
    private final TextField name = new TextField();
    private final Label steps = new Label();
    private final Label summary = new Label();
    private final Label error = new Label();
    private final Label nationNote = new Label();
    private final Button next = ActionButtons.button(ButtonIcon.PLAY);
    private final Button start = ActionButtons.button(ButtonIcon.PLAY);
    private final Button back = ActionButtons.button(ButtonIcon.BACK);
    private final Button newSeed = ActionButtons.button(ButtonIcon.REPLACE);
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Label mapLabel = new Label();
    private final Label sizeLabel = new Label();
    private final Label layoutLabel = new Label();
    private final Label seedLabel = new Label();
    private final Label difficultyLabel = new Label();
    private final Label nationLabel = new Label();
    private final Label nameLabel = new Label();
    private boolean secondStep;
    private boolean starting;
    private Task<ModelPo> generation;

    @Inject
    public ScreenFreePlay(MenuHolderPanel holder,
            ApplicationController applicationController, EventBus eventBus,
            I18n i18n, LocalizationHelper localizationHelper) {
        this.holder = holder;
        this.applicationController = applicationController;
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.localizationHelper = localizationHelper;
        form.setId("freePlaySetup");
        form.getStyleClass().add("game-menu-inner");
        form.setPrefWidth(600);
        maps.setId(ID_MAP);
        maps.getItems().setAll(mapNames(i18n));
        maps.getSelectionModel().select(GENERATED_INDEX);
        sizes.setId("freePlaySize");
        sizes.getItems().setAll(GeneratedMapSize.values());
        sizes.setValue(GeneratedMapSize.SMALL);
        layouts.setId("freePlayLayout");
        layouts.getItems().setAll(GeneratedLandLayout.values());
        layouts.setValue(GeneratedLandLayout.BIG_ISLANDS);
        seed.setId("freePlaySeed");
        newSeed.setId("freePlayNewSeed");
        newSeed.setOnAction(e -> seed.setText(randomSeed()));
        generated.setId("freePlayGenerated");
        generated.getChildren().addAll(row(sizeLabel, sizes),
                row(layoutLabel, layouts),
                row(seedLabel, new HBox(8, seed, newSeed)));
        world.getChildren().addAll(row(mapLabel, maps), generated);
        difficulties.setId("freePlayDifficulty");
        difficulties.getItems().setAll(Difficulty.values());
        difficulties.setValue(Difficulty.CONQUISTADOR);
        nations.setId("freePlayNation");
        nations.getItems().setAll(nationNames());
        nations.getSelectionModel().selectFirst();
        nationNote.setId("freePlayNationNote");
        nationNote.setWrapText(true);
        name.setId("freePlayPlayerName");
        summary.setId("freePlayGeneratedPreview");
        summary.setWrapText(true);
        player.getChildren().addAll(summary, row(difficultyLabel, difficulties),
                row(nationLabel, nations), nationNote, row(nameLabel, name));
        error.setId("freePlayNameError");
        error.setWrapText(true);
        next.setId("freePlayNext");
        next.setOnAction(e -> {
            if (validWorld()) {
                secondStep = true;
                refresh();
                name.requestFocus();
            }
        });
        start.setId("freePlayStart");
        start.setOnAction(e -> startGame());
        back.setId("freePlayBack");
        back.setCancelButton(true);
        back.setOnAction(e -> goBack());
        progress.setMaxSize(28, 28);
        form.getChildren().addAll(steps, world, player, error, progress,
                new HBox(12, back, next, start));
        maps.valueProperty().addListener((o, a, b) -> {
            updateNation();
            refresh();
        });
        sizes.valueProperty().addListener((o, a, b) -> refresh());
        layouts.valueProperty().addListener((o, a, b) -> refresh());
        seed.textProperty().addListener((o, a, b) -> refresh());
        name.textProperty().addListener((o, a, b) -> refresh());
        form.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                goBack();
                e.consume();
            }
        });
        updateLanguage(i18n);
        updateNation();
        holder.setMenuPanel(() -> form);
        holder.stackTitleAboveContent();
        holder.getContent().getStyleClass().add("free-play-screen");
        holder.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
    }

    private static HBox row(Label label, Region control) {
        label.setMinWidth(170);
        label.setLabelFor(control);
        final HBox row = new HBox(12, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static void show(Region node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String randomSeed() {
        return UUID.randomUUID().toString().substring(0, 18);
    }

    @Override
    public Region getContent() {
        return holder.getContent();
    }

    @Override
    public void beforeShow() {
        starting = false;
        secondStep = false;
        seed.setText(randomSeed());
        refresh();
        holder.beforeShow();
    }

    @Override
    public void beforeHide() {
        if (generation != null && generation.isRunning()) {
            generation.cancel();
        }
        holder.beforeHide();
    }

    private boolean isGenerated() {
        return maps.getSelectionModel().getSelectedIndex() == GENERATED_INDEX;
    }

    private FreePlayMap selectedMap() {
        final int index = maps.getSelectionModel().getSelectedIndex();
        return index == NORTH_AMERICA_INDEX ? FreePlayMap.NORTH_AMERICA
                : FreePlayMap.values()[Math.max(0,
                        Math.min(index, GENERATED_INDEX - 1))];
    }

    private void updateNation() {
        final boolean selectable = isGenerated()
                || selectedMap().isNationSelectable();
        nations.setDisable(!selectable);
        if (!selectable) {
            nations.getSelectionModel().selectFirst();
        }
        nationNote
                .setText(i18n.get(selectable ? GameMenu.setupNationReferenceNote
                        : GameMenu.setupNationNote));
    }

    private GeneratedWorldSettings settings() {
        return new GeneratedWorldSettings(seed.getText(), sizes.getValue(),
                layouts.getValue());
    }

    private boolean validWorld() {
        try {
            if (isGenerated()) {
                settings();
            }
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private boolean validName() {
        try {
            Player.normalizeDisplayName(name.getText());
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private void refresh() {
        show(world, !secondStep);
        show(player, secondStep);
        show(generated, isGenerated());
        show(next, !secondStep);
        show(start, secondStep);
        show(progress, starting);
        steps.setText(i18n.get(secondStep ? GameMenu.setupPlayerStep
                : GameMenu.setupWorldStep));
        summary.setText(isGenerated() ? i18n.get(GameMenu.setupSummary,
                sizeName(sizes.getValue()), layoutName(layouts.getValue()))
                : maps.getValue());
        world.setDisable(starting);
        player.setDisable(starting);
        back.setDisable(starting);
        next.setDefaultButton(!secondStep);
        start.setDefaultButton(secondStep);
        next.setDisable(starting || !validWorld());
        start.setDisable(starting || !validWorld() || !validName());
        error.setText(starting ? i18n.get(GameMenu.setupGenerating)
                : !validWorld() ? i18n.get(GameMenu.setupGeneratedSeedError)
                        : secondStep && !validName()
                                ? i18n.get(GameMenu.setupNameError)
                                : "");
    }

    private void goBack() {
        if (starting) {
            return;
        }
        if (secondStep) {
            secondStep = false;
            refresh();
        } else {
            eventBus.post(new ShowScreenEvent(Screen.MENU));
        }
    }

    private void startGame() {
        if (starting || !secondStep || !validWorld() || !validName()) {
            return;
        }
        final String playerName = Player.normalizeDisplayName(name.getText());
        final EuropeanNation nation = EuropeanNation.values()[nations
                .getSelectionModel().getSelectedIndex()];
        final Difficulty difficulty = difficulties.getValue();
        if (!isGenerated()) {
            applicationController.startNewFreeGame(selectedMap(), playerName,
                    difficulty, nation);
            return;
        }
        final GeneratedWorldSettings settings = settings();
        starting = true;
        refresh();
        generation = new Task<ModelPo>() {
            @Override
            protected ModelPo call() {
                return GeneratedWorld.create(settings, nation, difficulty);
            }
        };
        generation.setOnSucceeded(e -> {
            try {
                applicationController.startNewGeneratedGame(
                        generation.getValue(), playerName);
            } catch (IllegalArgumentException | IllegalStateException failure) {
                generationFailed();
            }
        });
        generation.setOnFailed(e -> generationFailed());
        final Thread thread = new Thread(generation, "world-generation");
        thread.setDaemon(true);
        thread.start();
    }

    private void generationFailed() {
        starting = false;
        refresh();
        error.setText(i18n.get(GameMenu.setupGeneratedFailed));
    }

    static List<String> mapNames(I18n i18n) {
        return List.of(i18n.get(GameMenu.setupMapOriginal),
                i18n.get(GameMenu.setupMapTwoIslands),
                i18n.get(GameMenu.setupMapRivalExpedition),
                i18n.get(GameMenu.setupMapReference),
                i18n.get(GameMenu.setupMapGenerated),
                i18n.get(GameMenu.setupMapNorthAmerica));
    }

    private List<String> nationNames() {
        return List.of(i18n.get(GameMenu.setupNationDutch),
                i18n.get(GameMenu.setupNationFrench),
                i18n.get(GameMenu.setupNationEnglish),
                i18n.get(GameMenu.setupNationSpanish));
    }

    private String sizeName(GeneratedMapSize size) {
        if (size == null) {
            return "";
        }
        return i18n
                .get(new GameMenu[] { GameMenu.setupSmall, GameMenu.setupMedium,
                        GameMenu.setupLarge, GameMenu.setupHuge }[size
                                .ordinal()])
                + " · " + size.getWidth() + " × " + size.getHeight();
    }

    private String layoutName(GeneratedLandLayout layout) {
        return layout == null ? ""
                : i18n.get(new GameMenu[] { GameMenu.setupOneLand,
                        GameMenu.setupBigIslands,
                        GameMenu.setupArchipelago }[layout.ordinal()]);
    }

    private static <T> StringConverter<T> converter(
            java.util.function.Function<T, String> label) {
        return new StringConverter<T>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : label.apply(value);
            }

            @Override
            public T fromString(String value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void updateLanguage(I18n language) {
        final int mapIndex = maps.getSelectionModel().getSelectedIndex();
        final int nationIndex = nations.getSelectionModel().getSelectedIndex();
        maps.getItems().setAll(mapNames(language));
        maps.getSelectionModel().select(Math.max(0, mapIndex));
        nations.getItems().setAll(nationNames());
        nations.getSelectionModel().select(Math.max(0, nationIndex));
        holder.setTitle(language.get(GameMenu.setupTitle));
        mapLabel.setText(language.get(GameMenu.setupMap));
        sizeLabel.setText(language.get(GameMenu.setupSize));
        layoutLabel.setText(language.get(GameMenu.setupLayout));
        seedLabel.setText(language.get(GameMenu.setupGeneratedSeed));
        difficultyLabel.setText(language.get(Loc.difficulty));
        nationLabel.setText(language.get(GameMenu.setupNation));
        nameLabel.setText(language.get(GameMenu.setupPlayerName));
        name.setPromptText(language.get(GameMenu.setupNamePrompt));
        next.setText(language.get(GameMenu.setupNext));
        start.setText(language.get(GameMenu.setupStart));
        back.setText(language.get(GameMenu.buttonBack));
        newSeed.setText(language.get(GameMenu.setupNewSeed));
        sizes.setConverter(converter(this::sizeName));
        layouts.setConverter(converter(this::layoutName));
        difficulties
                .setConverter(converter(localizationHelper::getDifficultyName));
        updateNation();
        refresh();
    }
}
