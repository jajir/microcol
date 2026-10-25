package org.microcol.gui.screen.game.components;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.screen.congress.CongressDialog;
import org.microcol.gui.screen.congress.CongressText;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.buttonpanel.NextTurnEvent;
import org.microcol.gui.event.ShowHelpEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.DeclareIndependenceEvent;
import org.microcol.gui.event.ExitGameEvent;
import org.microcol.gui.event.BuildRoadEvent;
import org.microcol.gui.event.ClearForestEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.AccessibleAction;
import java.util.function.Consumer;
import org.microcol.model.Unit;
import org.microcol.gui.UnitTypes;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import java.util.ArrayList;
import java.util.List;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.gui.event.SelectNextUnitEvent;
import org.microcol.gui.event.SkipUnitTurnEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class ButtonsGamePanel extends AbstractButtonsPanel
        implements UpdatableLanguage {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ButtonsGamePanel.class);

    public final static String BUTTON_NEXT_TURN_ID = "nextTurn";

    public final static String BUTTON_HELP_ID = "help";

    private final static String BUTTON_CENTER_ID = "center";

    private final static String BUTTON_STATISTICS_ID = "statistics";

    private final static String BUTTON_EXIT_ID = "exit";

    private final static String BUTTON_GOALS_ID = "goals";

    public final static String BUTTON_TURN_REPORT_ID = "turnReport";

    public final static String BUTTON_EUROPE_ID = "europe";

    private final static String BUTTON_MOVE_ID = "move";

    private final static String BUTTON_PLOW_FIELD_ID = "plowField";

    private final static String BUTTON_CANCEL_PLOW_FIELD_ID = "cancelPlowField";

    private final static String BUTTON_CLEAR_FOREST_ID = "clearForest";

    private final static String BUTTON_CANCEL_CLEAR_FOREST_ID = "cancelClearForest";

    private final static String BUTTON_BUILD_ROAD_ID = "buildRoad";

    private final static String BUTTON_CANCEL_BUILD_ROAD_ID = "cancelBuildRoad";

    private final static String BUTTON_BUILD_COLONY_ID = "buildColony";

    public final static String BUTTON_DECLARE_INDEPENDENCE_ID = "declareIndependence";

    private final ContextMenu unitActions = new ContextMenu();
    private final MenuItem unitContextTitle = new MenuItem();
    private final Menu unitChoices = new Menu();
    private final List<Button> unitCommands = new ArrayList<>();

    private final Button buttonSentry = new Button();
    private final Tooltip sentryTooltip = new Tooltip();
    private boolean sentryActive;
    private final Button buttonFortify = new Button();
    private final Tooltip fortifyTooltip = new Tooltip();
    private boolean fortifying;
    private boolean fortified;
    private final Button buttonGoTo = new Button();
    private final Tooltip goToTooltip = new Tooltip();
    private final Button buttonGoToColony = new Button();
    private final Tooltip goToColonyTooltip = new Tooltip();
    private final Button buttonDisband = new Button();
    private final Tooltip disbandTooltip = new Tooltip();
    private final Button buttonJettison = new Button();
    private final Tooltip jettisonTooltip = new Tooltip();
    private final Label goToStatus = new Label();
    private boolean goToOrdered;
    private boolean goToPaused;
    private String goToDestination;
    private I18n currentI18n;
    private final ImageProvider toolbarImages;
    private final EventBus toolbarEvents;
    private final Map<Labeled, Buttons> toolbarLabels = new LinkedHashMap<>();
    private final Map<Labeled, Buttons> toolbarExplanations = new LinkedHashMap<>();

    private final Button buttonCongress = new Button();

    private final Button buttonRetire = new Button();
    private final Tooltip retireTooltip = new Tooltip();

    private final Button buttonWait = new Button();
    private final Button buttonSkipUnitTurn = new Button();
    private final Tooltip waitTooltip = new Tooltip();
    private final Tooltip skipTooltip = new Tooltip();
    private final Tooltip buildRoadTooltip = new Tooltip();

    private final Button buttonCenter;
    private final Button buttonHelp;
    private final Button buttonStatistics;
    private final Button buttonExit;

    private final Button buttonGoals;
    private final Button buttonTurnReport;
    private final Button buttonNextTurn;
    private final Button buttonEurope;
    private final Button buttonDeclareIndependence;

    /**
     * Unit related buttons
     */
    private final Button buttonMove;
    private final Button buttonPlowField;
    private final Button buttonCancelPlowField;
    private final Button buttonClearForest;
    private final Button buttonCancelClearForest;
    private final Button buttonBuildRoad;
    private final Button buttonCancelBuildRoad;
    private final Button buttonBuildColony;

    @Inject
    ButtonsGamePanel(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n, final ColoniesMenu coloniesMenu,
            final CongressDialog congressDialog) {
        super(imageProvider, eventBus, Source.GAME, i18n);
        currentI18n = i18n;
        toolbarImages = imageProvider;
        toolbarEvents = eventBus;

        buttonCongress.setId("congress");
        buttonCongress.setText(i18n.get(CongressText.title));
        buttonCongress.setOnAction(event -> congressDialog.showCongress());
        buttonFortify.setId("fortifyUnit");
        buttonFortify.setTooltip(fortifyTooltip);
        buttonFortify.setDisable(true);
        buttonGoTo.setId("goToUnit");
        buttonGoTo.setTooltip(goToTooltip);
        buttonGoTo.setDisable(true);
        buttonGoToColony.setId("goToColonyUnit");
        buttonGoToColony.setTooltip(goToColonyTooltip);
        buttonGoToColony.setDisable(true);
        buttonDisband.setId("disbandUnit");
        buttonDisband.setTooltip(disbandTooltip);
        buttonDisband.setDisable(true);
        buttonDisband.setVisible(false);
        buttonDisband.setManaged(false);
        buttonJettison.setId("jettisonUnit");
        buttonJettison.setTooltip(jettisonTooltip);
        buttonJettison.setDisable(true);
        buttonJettison.setVisible(false);
        buttonJettison.setManaged(false);
        goToStatus.setId("goToStatus");
        goToStatus.setVisible(false);
        goToStatus.setManaged(false);
        buttonSentry.setId("sentryUnit");
        buttonSentry.setTooltip(sentryTooltip);
        buttonSentry.setDisable(true);
        buttonWait.setId("waitUnit");
        buttonWait.setTooltip(waitTooltip);
        buttonWait
                .setOnAction(event -> eventBus.post(new SelectNextUnitEvent()));
        buttonSkipUnitTurn.setId("skipUnitTurn");
        buttonSkipUnitTurn.setTooltip(skipTooltip);
        buttonSkipUnitTurn
                .setOnAction(event -> eventBus.post(new SkipUnitTurnEvent()));
        setUnitOrdersEnabled(false, false);
        updateLanguage(i18n);

        buttonCenter = makeButon(ImageLoaderButtons.BUTTON_CENTER,
                Buttons.buttonCenter);
        buttonCenter.setOnAction(event -> eventBus.post(new CenterViewEvent()));
        buttonCenter.setId(BUTTON_CENTER_ID);
        buttonHelp = makeButon(ImageLoaderButtons.BUTTON_HELP,
                Buttons.buttonHelp);
        buttonHelp.setOnAction(event -> eventBus.post(new ShowHelpEvent()));
        buttonHelp.setId(BUTTON_HELP_ID);
        buttonStatistics = makeButon(ImageLoaderButtons.BUTTON_STATISTICS,
                Buttons.buttonStatistics);
        buttonStatistics.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.STATISTICS)));
        buttonStatistics.setId(BUTTON_STATISTICS_ID);
        buttonExit = makeButon(ImageLoaderButtons.BUTTON_EXIT,
                Buttons.buttonExit);
        buttonExit.setOnAction(event -> eventBus.post(new ExitGameEvent()));
        buttonExit.setId(BUTTON_EXIT_ID);
        buttonGoals = makeButon(ImageLoaderButtons.BUTTON_GOALS,
                Buttons.buttonGoals);
        buttonGoals.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.GOALS)));
        buttonGoals.setId(BUTTON_GOALS_ID);
        buttonTurnReport = makeButon(ImageLoaderButtons.BUTTON_TURN_REPORT,
                Buttons.buttonTurnReport);
        buttonTurnReport.setOnAction(event -> eventBus
                .post(new ShowScreenEvent(Screen.TURN_REPORT)));
        buttonTurnReport.setId(BUTTON_TURN_REPORT_ID);
        buttonEurope = makeButon(ImageLoaderButtons.BUTTON_EUROPE,
                Buttons.buttonEurope);
        buttonEurope.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.EUROPE)));
        buttonEurope.setId(BUTTON_EUROPE_ID);
        buttonNextTurn = makeButon(ImageLoaderButtons.BUTTON_NEXT_TURN,
                Buttons.buttonNextTurn);
        buttonNextTurn.setId(BUTTON_NEXT_TURN_ID);
        buttonNextTurn.setOnAction(event -> {
            buttonNextTurn.setDisable(true);
            eventBus.post(new NextTurnEvent());
        });
        buttonMove = makeButon(ImageLoaderButtons.BUTTON_MOVE,
                Buttons.buttonMove);
        buttonMove.setOnAction(event -> eventBus.post(new StartMoveEvent()));
        buttonMove.setId(BUTTON_MOVE_ID);
        buttonPlowField = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonPlowField);
        buttonPlowField
                .setOnAction(event -> eventBus.post(new PlowFieldEvent()));
        buttonPlowField.setId(BUTTON_PLOW_FIELD_ID);
        buttonCancelPlowField = makeButon(ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonCancelPlowField);
        buttonCancelPlowField.setId(BUTTON_CANCEL_PLOW_FIELD_ID);
        buttonClearForest = makeButon(ImageLoaderButtons.BUTTON_CUT_TREE,
                Buttons.buttonClearForest);
        buttonClearForest
                .setOnAction(event -> eventBus.post(new ClearForestEvent()));
        buttonClearForest.setId(BUTTON_CLEAR_FOREST_ID);
        buttonCancelClearForest = makeButon(ImageLoaderButtons.BUTTON_CUT_TREE,
                Buttons.buttonCancelClearForest);
        buttonCancelClearForest.setId(BUTTON_CANCEL_CLEAR_FOREST_ID);
        buttonBuildRoad = makeButon(ImageLoaderButtons.BUTTON_BUILD_ROAD,
                Buttons.buttonBuildRoad);
        buttonBuildRoad
                .setOnAction(event -> eventBus.post(new BuildRoadEvent()));
        buttonBuildRoad.setId(BUTTON_BUILD_ROAD_ID);
        buttonBuildRoad.setTooltip(buildRoadTooltip);
        buttonCancelBuildRoad = makeButon(ImageLoaderButtons.BUTTON_BUILD_ROAD,
                Buttons.buttonCancelBuildRoad);
        buttonCancelBuildRoad.setId(BUTTON_CANCEL_BUILD_ROAD_ID);
        buttonBuildColony = makeButon(ImageLoaderButtons.BUTTON_BUILD_COLONY,
                Buttons.buttonBuildColony);
        buttonBuildColony
                .setOnAction(event -> eventBus.post(new BuildColonyEvent()));
        buttonBuildColony.setId(BUTTON_BUILD_COLONY_ID);
        buttonDeclareIndependence = makeButon(
                ImageLoaderButtons.BUTTON_PLOW_FIELD,
                Buttons.buttonDeclareIndependence);
        buttonDeclareIndependence.setOnAction(
                event -> eventBus.post(new DeclareIndependenceEvent()));
        buttonDeclareIndependence.setId(BUTTON_DECLARE_INDEPENDENCE_ID);
        buttonRetire.setId("retire");
        buttonRetire.setTooltip(retireTooltip);
        buttonRetire.setVisible(false);
        buttonRetire.setManaged(false);
        buttonRetire.setDisable(true);

        unitActions.setId("unitActionsMenu");
        unitActions.setConsumeAutoHidingEvents(false);
        // Act on release so Space cannot also reach the map's skip-turn
        // handler.
        unitActions.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
        });
        unitActions.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                final Node focused = unitActions.getScene().getFocusOwner();
                if (focused != null) {
                    focused.executeAccessibleAction(AccessibleAction.FIRE);
                }
                event.consume();
            }
        });
        unitActions.getStyleClass().add("unit-actions-menu");
        unitContextTitle.setId("unitContextTitle");
        unitContextTitle.setDisable(true);
        unitChoices.setId("unitChoices");
        unitActions.getItems().addAll(unitContextTitle, unitChoices,
                new SeparatorMenuItem());
        addUnitCommand(buttonMove, Buttons.buttonMove);
        addUnitCommand(buttonGoTo, null);
        addUnitCommand(buttonGoToColony, null);
        addUnitCommand(buttonWait, null);
        addUnitCommand(buttonSkipUnitTurn, null);
        unitActions.getItems().add(new SeparatorMenuItem());
        addUnitCommand(buttonSentry, null);
        addUnitCommand(buttonFortify, null);
        unitActions.getItems().add(new SeparatorMenuItem());
        addUnitCommand(buttonBuildColony, Buttons.buttonBuildColony);
        addUnitCommand(buttonPlowField, Buttons.buttonPlowField);
        addUnitCommand(buttonCancelPlowField, Buttons.cancelPlowingLabel);
        addUnitCommand(buttonClearForest, Buttons.buttonClearForest);
        addUnitCommand(buttonCancelClearForest, Buttons.cancelClearingLabel);
        addUnitCommand(buttonBuildRoad, Buttons.buttonBuildRoad);
        addUnitCommand(buttonCancelBuildRoad, Buttons.cancelRoadLabel);
        unitActions.getItems().add(new SeparatorMenuItem());
        addUnitCommand(buttonJettison, null);
        addUnitCommand(buttonDisband, null);

        getButtonPanel().getChildren().add(buttonDeclareIndependence);
        getButtonPanel().getChildren().add(coloniesMenu.getContent());
        getButtonPanel().getChildren().add(buttonCongress);
        getButtonPanel().getChildren().add(buttonCenter);
        getButtonPanel().getChildren().add(buttonStatistics);
        getButtonPanel().getChildren().add(buttonTurnReport);
        getButtonPanel().getChildren().add(buttonGoals);
        getButtonPanel().getChildren().add(buttonHelp);
        getButtonPanel().getChildren().add(buttonEurope);
        getButtonPanel().getChildren().add(buttonNextTurn);
        getButtonPanel().getChildren().add(buttonRetire);
        getButtonPanel().getChildren().add(buttonExit);
        addToolbarControl(coloniesMenu.getContent(), Buttons.toolbarColonies,
                Buttons.coloniesExplanation);
        addToolbarControl(buttonCongress, Buttons.toolbarCongress,
                Buttons.congressExplanation);
        addToolbarControl(buttonCenter, Buttons.toolbarCenter,
                Buttons.buttonCenter);
        addToolbarControl(buttonStatistics, Buttons.toolbarStatistics,
                Buttons.buttonStatistics);
        addToolbarControl(buttonTurnReport, Buttons.toolbarTurnReport,
                Buttons.buttonTurnReport);
        addToolbarControl(buttonGoals, Buttons.toolbarGoals,
                Buttons.buttonGoals);
        addToolbarControl(buttonHelp, Buttons.toolbarHelp, Buttons.buttonHelp);
        addToolbarControl(buttonEurope, Buttons.toolbarEurope,
                Buttons.buttonEurope);
        addToolbarControl(buttonNextTurn, Buttons.toolbarNextTurn,
                Buttons.nextTurnExplanation);
        addToolbarControl(buttonRetire, Buttons.toolbarRetire,
                Buttons.retireExplanation);
        addToolbarControl(buttonExit, Buttons.toolbarExit, Buttons.buttonExit);
        addToolbarControl(buttonDeclareIndependence,
                Buttons.toolbarIndependence, Buttons.buttonDeclareIndependence);
        updateToolbarSeason(false);
    }

    private void addToolbarControl(final Labeled control, final Buttons label,
            final Buttons explanation) {
        toolbarLabels.put(control, label);
        toolbarExplanations.put(control, explanation);
        control.getStyleClass().remove("image-button");
        control.getStyleClass().add("game-toolbar-control");
        control.setBackground(null);
        control.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        control.setContentDisplay(control instanceof Button ? ContentDisplay.TOP
                : ContentDisplay.LEFT);
        control.setText(currentI18n.get(label));
        final ImageView icon = new ImageView();
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setMouseTransparent(true);
        control.setGraphic(icon);
        control.setOnMouseEntered(event -> refreshToolbarExplanation(control));
        control.setOnMouseExited(event -> refreshToolbarExplanation(null));
        control.focusedProperty().addListener(
                (observable, oldValue, newValue) -> refreshToolbarExplanation(
                        newValue ? control : null));
    }

    void updateToolbarSeason(final boolean winter) {
        for (final Labeled control : toolbarLabels.keySet()) {
            ((ImageView) control.getGraphic()).setImage(
                    toolbarImages.getImage("gameToolbar_" + control.getId()
                            + (winter ? "_winter" : "_summer")));
        }
    }

    private void refreshToolbarExplanation(final Labeled preferred) {
        final Labeled active = preferred != null ? preferred
                : toolbarLabels.keySet().stream().filter(
                        control -> control.isFocused() && !control.isDisabled())
                        .findFirst().orElse(null);
        toolbarEvents.post(active == null
                ? new StatusBarMessageEvent(Source.GAME)
                : new StatusBarMessageEvent(
                        currentI18n.get(toolbarExplanations.get(active)),
                        Source.GAME));
    }

    private void addUnitCommand(final Button button, final Buttons labelKey) {
        unitCommands.add(button);
        button.getStyleClass().remove("image-button");
        button.setBackground(Background.EMPTY);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(Region.USE_PREF_SIZE);
        if (labelKey != null) {
            button.setText(currentI18n.get(labelKey));
            button.getProperties().put("unitLabel", labelKey);
        }
        button.setText(button.getText().replace('\n', ' '));
        final Label caption = new Label();
        caption.getStyleClass().add("unit-action-caption");
        caption.setWrapText(true);
        final Label shortcut = new Label();
        shortcut.getStyleClass().add("unit-action-shortcut");
        shortcut.setMinWidth(Region.USE_PREF_SIZE);
        shortcut.managedProperty().bind(shortcut.visibleProperty());
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final HBox face = new HBox(8, caption, spacer, shortcut);
        face.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        // Fixed menu column, independent of the button's first layout pass.
        face.setPrefWidth(266);
        face.setMouseTransparent(true);
        button.setGraphic(face);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // Advertise the graphic's horizontal content bias to the menu layout.
        button.setWrapText(true);
        button.textProperty().addListener((observable, oldText,
                newText) -> updateUnitCaption(newText, caption, shortcut));
        updateUnitCaption(button.getText(), caption, shortcut);
        button.addEventFilter(ActionEvent.ACTION, event -> unitActions.hide());
        // Stop the menu skin forwarding the button's action back into its menu
        // item.
        button.addEventHandler(ActionEvent.ACTION, event -> event.consume());
        final Label explanation = new Label();
        explanation.getStyleClass().add("unit-action-explanation");
        explanation.setWrapText(true);
        explanation.setMaxWidth(290);
        if (button.getTooltip() != null) {
            explanation.textProperty().bind(button.getTooltip().textProperty());
        }
        explanation.visibleProperty().bind(button.disabledProperty()
                .and(explanation.textProperty().isNotEmpty()));
        explanation.managedProperty().bind(explanation.visibleProperty());
        final VBox row = new VBox(2, button, explanation);
        row.setPrefWidth(290);
        // JavaFX custom rows focus themselves but leave sibling submenus open.
        row.setOnMouseEntered(event -> unitChoices.hide());
        final CustomMenuItem item = new CustomMenuItem(row, true);
        // The embedded button handles its click; the menu item handles keyboard
        // selection.
        button.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> event.consume());
        item.getStyleClass().add("unit-command-item");
        item.setOnAction(event -> button.fire());
        item.disableProperty().bind(button.disableProperty());
        item.visibleProperty().bind(button.visibleProperty());
        unitActions.getItems().add(item);
    }

    private void updateUnitCaption(final String text, final Label caption,
            final Label shortcut) {
        // Existing translations own both the action and its displayed shortcut.
        final String label = text.replace('\n', ' ');
        final int keyStart = label.lastIndexOf(" (");
        final boolean hasKey = keyStart >= 0 && label.endsWith(")");
        caption.setText(hasKey ? label.substring(0, keyStart) : label);
        shortcut.setText(
                hasKey ? label.substring(keyStart + 2, label.length() - 1)
                        : "");
        shortcut.setVisible(hasKey);
    }

    /** Keep the selected unit's route visible independently of its menu. */
    public void addUnitStatus(final VBox card) {
        final Label route = new Label();
        route.setId("goToStatus");
        route.setWrapText(true);
        route.textProperty().bind(goToStatus.textProperty());
        route.visibleProperty().bind(goToStatus.visibleProperty());
        route.managedProperty().bind(route.visibleProperty());
        card.getChildren().add(route);
    }

    public void showUnitActions(final Node owner, final double screenX,
            final double screenY, final List<Unit> units, final Unit selected,
            final Consumer<Unit> selectUnit) {
        unitContextTitle.setText(
                currentI18n.get(UnitTypes.getUnitName(selected.getType())));
        unitChoices.setText(currentI18n.get(Buttons.chooseUnit));
        unitChoices.getItems().clear();
        unitChoices.setVisible(units.size() > 1);
        for (int i = 0; i < units.size(); i++) {
            final Unit unit = units.get(i);
            final RadioMenuItem choice = new RadioMenuItem(
                    currentI18n.get(UnitTypes.getUnitName(unit.getType()))
                            + " (" + (i + 1) + ")");
            choice.setId("selectUnit-" + unit.getId());
            choice.setSelected(unit.equals(selected));
            choice.setOnAction(event -> selectUnit.accept(unit));
            unitChoices.getItems().add(choice);
        }
        boolean hasAction = false;
        SeparatorMenuItem separator = null;
        for (final MenuItem item : unitActions.getItems()) {
            if (item instanceof SeparatorMenuItem) {
                separator = (SeparatorMenuItem) item;
                separator.setVisible(false);
            } else if (item.isVisible()) {
                if (separator != null) {
                    separator.setVisible(hasAction);
                    separator = null;
                }
                hasAction = true;
            }
        }
        unitActions.show(owner, screenX, screenY);
        unitActions.getScene().getStylesheets().setAll(
                MainStageBuilder.STYLE_SHEET_MICROCOL,
                MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);
    }

    public void hideUnitActions() {
        if (Platform.isFxApplicationThread()) {
            unitActions.hide();
        } else {
            Platform.runLater(unitActions::hide);
        }
    }

    void disableAllButtons() {
        LOGGER.debug("Disabling buttons bar");
        hideUnitActions();
        setDisable(true);
    }

    void disableTurnCommands() {
        disableAllButtons();
        // Recovery must remain reachable if turn preparation fails.
        buttonExit.setDisable(false);
    }

    void allowPendingImmigrationNavigation() {
        disableTurnCommands();
        buttonEurope.setDisable(false);
    }

    /**
     * While spring 1800 waits for the player's choice, the statistics screen
     * stays reachable: its base score tab reopens the choice (R-0193).
     */
    void allowScoreBoundaryNavigation() {
        disableTurnCommands();
        buttonStatistics.setDisable(false);
    }

    void enableAllButtons() {
        LOGGER.debug("Enabling buttons bar");
        setDisable(false);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        currentI18n = i18n;
        for (final Button command : unitCommands) {
            final Buttons key = (Buttons) command.getProperties()
                    .get("unitLabel");
            if (key != null) {
                command.setText(i18n.get(key));
            }
        }
        buttonCongress.setText(i18n.get(CongressText.title));
        updateSentryText();
        updateFortifyText();
        updateGoToText();
        buttonWait.setText(i18n.get(Buttons.buttonWait).replace('\n', ' '));
        buttonSkipUnitTurn.setText(
                i18n.get(Buttons.buttonSkipUnitTurn).replace('\n', ' '));
        waitTooltip.setText(i18n.get(Buttons.waitExplanation));
        skipTooltip.setText(i18n.get(Buttons.skipUnitTurnExplanation));
        buttonDisband.setText(i18n.get(Buttons.buttonDisband));
        disbandTooltip.setText(i18n.get(Buttons.disbandExplanation));
        buttonJettison.setText(i18n.get(Buttons.buttonJettison));
        jettisonTooltip.setText(i18n.get(Buttons.jettisonExplanation));
        buildRoadTooltip.setText(i18n.get(Buttons.buildRoadExplanation));
        buttonRetire.setText(i18n.get(Buttons.buttonRetire));
        retireTooltip.setText(i18n.get(Buttons.retireExplanation));
        toolbarLabels.forEach((control, key) -> control.setText(i18n.get(key)));
    }

    void setRetireAction(final Runnable action) {
        buttonRetire.setOnAction(event -> action.run());
    }

    /**
     * The control is shown only where the model offers a retirement. It stays
     * clickable while the human may act, so a refusal can explain itself.
     *
     * @param visible whether the model offers the retirement now
     * @param enabled whether the human may use the control now
     */
    void setRetireOrder(final boolean visible, final boolean enabled) {
        buttonRetire.setVisible(visible);
        buttonRetire.setManaged(visible);
        buttonRetire.setDisable(!enabled);
    }

    void setUnitOrdersEnabled(final boolean wait, final boolean skip) {
        buttonWait.setDisable(!wait);
        buttonSkipUnitTurn.setDisable(!skip);
        waitTooltip.setText(currentI18n
                .get(wait ? Buttons.waitExplanation : Buttons.waitUnavailable));
        skipTooltip
                .setText(currentI18n.get(skip ? Buttons.skipUnitTurnExplanation
                        : Buttons.skipUnavailable));
    }

    void setSentryAction(final Runnable action) {
        buttonSentry.setOnAction(event -> action.run());
    }

    void setSentryOrder(final boolean active, final boolean enabled) {
        sentryActive = active;
        buttonSentry.setDisable(!enabled);
        updateSentryText();
    }

    private void updateSentryText() {
        buttonSentry.setText(
                currentI18n.get(sentryActive ? Buttons.buttonActivateSentry
                        : Buttons.buttonSentry));
        sentryTooltip.setText(currentI18n
                .get(buttonSentry.isDisabled() ? Buttons.sentryUnavailable
                        : sentryActive ? Buttons.activateSentryExplanation
                                : Buttons.sentryExplanation));
    }

    /**
     * Starts a go-to order with the map pick, or cancels the order shown.
     *
     * @param action required action of the button
     */
    void setGoToAction(final Runnable action) {
        buttonGoTo.setOnAction(event -> action.run());
    }

    /**
     * Opens the list of the model's friendly colonies as go-to destinations.
     *
     * @param action required action of the button
     */
    void setGoToColonyAction(final Runnable action) {
        buttonGoToColony.setOnAction(event -> action.run());
    }

    /**
     * Shows the go-to state of the selected unit. A paused order keeps its
     * destination visible and says so.
     *
     * @param ordered       whether the unit has a go-to order
     * @param paused        whether the order is paused for lack of a route
     * @param enabled       whether the button may be used now
     * @param colonyEnabled whether the model offers at least one colony
     * @param destination   destination name, or {@code null} without an order
     */
    void setGoToOrder(final boolean ordered, final boolean paused,
            final boolean enabled, final boolean colonyEnabled,
            final String destination) {
        goToOrdered = ordered;
        goToPaused = paused;
        goToDestination = destination;
        buttonGoTo.setDisable(!enabled);
        buttonGoToColony.setDisable(!colonyEnabled);
        buttonGoToColony.setVisible(!ordered);
        buttonGoToColony.setManaged(!ordered);
        updateGoToText();
    }

    private void updateGoToText() {
        goToStatus.setVisible(goToOrdered);
        goToStatus.setManaged(goToOrdered);
        goToStatus.setText(
                goToOrdered
                        ? currentI18n.get(
                                goToPaused ? Buttons.goToStatusPaused
                                        : Buttons.goToStatusActive,
                                goToDestination)
                        : "");
        buttonGoTo.setText(currentI18n.get(
                goToOrdered ? Buttons.buttonCancelGoTo : Buttons.buttonGoTo));
        buttonGoToColony.setText(currentI18n.get(Buttons.buttonGoToColony));
        goToTooltip.setText(
                currentI18n.get(goToOrdered ? Buttons.goToCancelExplanation
                        : Buttons.goToExplanation));
        goToColonyTooltip.setText(currentI18n.get(
                buttonGoToColony.isDisabled() ? Buttons.goToColonyUnavailable
                        : Buttons.goToColonyExplanation));
    }

    void setDisbandAction(final Runnable action) {
        buttonDisband.setOnAction(event -> action.run());
    }

    /**
     * The control is shown for the selected own unit. It stays clickable while
     * the human may act, so a refusal can explain its reason.
     */
    void setDisbandOrder(final boolean visible, final boolean enabled) {
        // The button stays in the panel, like the go-to status; the shared
        // setVisibleNode only adds or removes a node and never shows it.
        buttonDisband.setVisible(visible);
        buttonDisband.setManaged(visible);
        buttonDisband.setDisable(!enabled);
    }

    void setJettisonAction(final Runnable action) {
        buttonJettison.setOnAction(event -> action.run());
    }

    /**
     * The control is shown for an owned ship that may offer goods at sea. It
     * stays clickable while the human may act, so a refusal can explain itself.
     */
    void setJettisonOrder(final boolean visible, final boolean enabled) {
        buttonJettison.setVisible(visible);
        buttonJettison.setManaged(visible);
        buttonJettison.setDisable(!enabled);
    }

    void setFortifyAction(final Runnable action) {
        buttonFortify.setOnAction(event -> action.run());
    }

    void setFortifyOrder(final boolean ordered, final boolean effective,
            final boolean enabled) {
        fortifying = ordered;
        fortified = effective;
        buttonFortify.setDisable(!enabled);
        updateFortifyText();
    }

    private void updateFortifyText() {
        buttonFortify.setText(currentI18n.get(fortifying
                ? (fortified ? Buttons.buttonCancelFortified
                        : Buttons.buttonCancelFortifying)
                : Buttons.buttonFortify));
        fortifyTooltip.setText(currentI18n
                .get(buttonFortify.isDisabled() ? Buttons.fortifyUnavailable
                        : fortifying
                                ? (fortified ? Buttons.fortifiedExplanation
                                        : Buttons.fortifyingExplanation)
                                : Buttons.fortifyExplanation));
    }

    private void setDisable(final boolean disable) {
        buttonCongress.setDisable(disable);
        buttonSentry.setDisable(disable);
        buttonFortify.setDisable(disable);
        buttonGoTo.setDisable(disable);
        buttonGoToColony.setDisable(disable);
        buttonDisband.setDisable(disable);
        buttonJettison.setDisable(disable);
        buttonWait.setDisable(disable);
        buttonSkipUnitTurn.setDisable(disable);
        buttonCenter.setDisable(disable);
        buttonHelp.setDisable(disable);
        buttonStatistics.setDisable(disable);
        buttonRetire.setDisable(disable);
        buttonExit.setDisable(disable);

        buttonGoals.setDisable(disable);
        buttonTurnReport.setDisable(disable);
        buttonNextTurn.setDisable(disable);
        buttonEurope.setDisable(disable);

        buttonMove.setDisable(disable);
        buttonPlowField.setDisable(disable);
        buttonCancelPlowField.setDisable(disable);
        buttonClearForest.setDisable(disable);
        buttonCancelClearForest.setDisable(disable);
        buttonBuildRoad.setDisable(disable);
        buttonCancelBuildRoad.setDisable(disable);
        buttonBuildColony.setDisable(disable);
        buttonDeclareIndependence.setDisable(disable);
    }

    public void setVisibleButtonMove(final boolean isVisible) {
        setVisibleNode(buttonMove, isVisible);
    }

    public void setVisibleButtonPlowField(final boolean isVisible) {
        setVisibleNode(buttonPlowField, isVisible);
    }

    void setCancelPlowFieldAction(final Runnable action) {
        buttonCancelPlowField.setOnAction(event -> action.run());
    }

    public void setVisibleButtonCancelPlowField(final boolean isVisible) {
        setVisibleNode(buttonCancelPlowField, isVisible);
    }

    public void setVisibleButtonClearForest(final boolean isVisible) {
        setVisibleNode(buttonClearForest, isVisible);
    }

    void setCancelClearForestAction(final Runnable action) {
        buttonCancelClearForest.setOnAction(event -> action.run());
    }

    public void setVisibleButtonCancelClearForest(final boolean isVisible) {
        setVisibleNode(buttonCancelClearForest, isVisible);
    }

    public void setVisibleButtonBuildRoad(final boolean isVisible) {
        setVisibleNode(buttonBuildRoad, isVisible);
    }

    void setCancelBuildRoadAction(final Runnable action) {
        buttonCancelBuildRoad.setOnAction(event -> action.run());
    }

    public void setVisibleButtonCancelBuildRoad(final boolean isVisible) {
        setVisibleNode(buttonCancelBuildRoad, isVisible);
    }

    public void setVisibleButtonBuildColony(final boolean isVisible) {
        setVisibleNode(buttonBuildColony, isVisible);
    }

    public void setVisibleButtonDeclareIndependence(final boolean isVisible) {
        setVisibleNode(buttonDeclareIndependence, isVisible);
    }

    private void setVisibleNode(final Node node, final boolean isVisible) {
        Platform.runLater(() -> {
            if (unitCommands.contains(node)) {
                node.setVisible(isVisible);
                node.setManaged(isVisible);
                return;
            }
            if (isContaining(node)) {
                if (!isVisible) {
                    remove(node);
                }
            } else {
                if (isVisible) {
                    getButtonPanel().getChildren().add(0, node);
                }
            }
        });
    }

    /**
     * @return the buttonNextTurn
     */
    public Button getButtonNextTurn() {
        return buttonNextTurn;
    }

}
