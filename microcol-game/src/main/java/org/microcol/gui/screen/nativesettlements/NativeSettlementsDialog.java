package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.NativeAdvisorReport;
import org.microcol.model.NativeMissionView;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeRelationView;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeTeachingCourse;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Keyboard-reachable native advisor (R-0155, R-0201): the nations the player
 * knows, and the details of the native settlements the player has learned. Each
 * opening reads the report from the model, so the dialog never shows a stale
 * value and never changes a relation. Centering the map on a settlement is
 * allowed only while the model still knows that settlement.
 */
public final class NativeSettlementsDialog extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeSettlementsContent";

    public static final String LIST_ID = "nativeSettlementsList";

    public static final String NATIONS_ID = "nativeSettlementsNations";

    public static final String ATTITUDE_ID = "nativeSettlementAttitude";

    public static final String ALARM_CHANGE_ID = "nativeSettlementAlarmChange";

    public static final String RIVAL_ID = "nativeSettlementRival";

    public static final String CHIEF_ID = "nativeSettlementChief";

    public static final String NONE_ID = "nativeSettlementsNone";

    public static final String MISSION_ID = "nativeSettlementMission";

    public static final String TEACHING_ID = "nativeSettlementTeaching";

    public static final String STOP_TEACHING_ID = "nativeSettlementStopTeaching";

    public static final String SHOW_ON_MAP_ID = "nativeSettlementShowOnMap";

    public static final String UNAVAILABLE_ID = "nativeSettlementUnavailable";

    private final I18n i18n;

    private final GameModelController gameModelController;

    private final StatusBarMessageService statusBarMessageService;

    private final SelectedTileManager selectedTileManager;

    private final ButtonBarOk buttonBar;

    private final ObservableList<NativeAdvisorReport.Settlement> settlements = FXCollections
            .observableArrayList();

    private final ListView<NativeAdvisorReport.Settlement> settlementList = new ListView<>(
            settlements);

    private final Label nations = new Label();

    private final Label none = new Label();

    private final Label attitude = new Label();

    private final Label alarmChange = new Label();

    private final Label rival = new Label();

    private final Label chief = new Label();

    private final Label mission = new Label();

    private final Label teaching = new Label();

    private final Button stopTeaching = ActionButtons.button(ButtonIcon.STOP);

    private final Button showOnMap = ActionButtons.button(ButtonIcon.CENTER);

    private final Label unavailable = new Label();

    /** The colonist of the shown course, when it is the player's own. */
    private Optional<Unit> learner = Optional.empty();

    /** The nations of the report read at the last opening. */
    private List<NativeNation> knownNations = List.of();

    /** The model the last opening read, to refuse navigation after a change. */
    private Model shownModel;

    @Inject
    public NativeSettlementsDialog(final I18n i18n, final ViewUtil viewUtil,
            final GameModelController gameModelController,
            final StatusBarMessageService statusBarMessageService,
            final SelectedTileManager selectedTileManager) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.gameModelController = gameModelController;
        this.statusBarMessageService = statusBarMessageService;
        this.selectedTileManager = selectedTileManager;
        buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        settlementList.setId(LIST_ID);
        settlementList.setPrefSize(300, 170);
        settlementList.setCellFactory(
                list -> new ListCell<NativeAdvisorReport.Settlement>() {

                    @Override
                    protected void updateItem(
                            final NativeAdvisorReport.Settlement row,
                            final boolean empty) {
                        super.updateItem(row, empty);
                        setText(empty || row == null ? null
                                : statusBarMessageService.nativeSettlementText(
                                        row.getSettlement()));
                    }

                });
        settlementList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldRow, newRow) -> showDetails(newRow));

        nations.setId(NATIONS_ID);
        nations.setWrapText(true);
        none.setId(NONE_ID);
        none.setWrapText(true);
        attitude.setId(ATTITUDE_ID);
        attitude.setWrapText(true);
        alarmChange.setId(ALARM_CHANGE_ID);
        alarmChange.setWrapText(true);
        rival.setId(RIVAL_ID);
        rival.setWrapText(true);
        chief.setId(CHIEF_ID);
        chief.setWrapText(true);
        mission.setId(MISSION_ID);
        mission.setWrapText(true);
        teaching.setId(TEACHING_ID);
        teaching.setWrapText(true);
        stopTeaching.setId(STOP_TEACHING_ID);
        stopTeaching.setOnAction(e -> {
            learner.ifPresent(gameModelController::cancelNativeTeaching);
            close();
        });
        showOnMap.setId(SHOW_ON_MAP_ID);
        showOnMap.setOnAction(e -> Optional
                .ofNullable(
                        settlementList.getSelectionModel().getSelectedItem())
                .ifPresent(this::centerOnSettlement));
        unavailable.setId(UNAVAILABLE_ID);
        unavailable.setWrapText(true);
        final VBox details = new VBox(8, none, attitude, alarmChange, rival,
                chief, mission, teaching, stopTeaching, showOnMap, unavailable);
        details.setPrefWidth(300);
        final HBox main = new HBox(20, settlementList, details);
        final VBox root = new VBox(10, nations, main, buttonBar.getContent());
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        // Handle Escape before the focused list consumes it.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
        init(root);
    }

    /**
     * Each opening reads the advisor report again and puts keyboard focus on
     * the list, so arrow keys and Escape work without a mouse.
     */
    @Override
    public void showAndWait() {
        shownModel = gameModelController.getModel();
        final NativeAdvisorReport report = shownModel
                .getNativeAdvisorReport(gameModelController.getHumanPlayer());
        knownNations = report.getKnownNations();
        settlements.setAll(report.getSettlements());
        showNations();
        if (settlements.isEmpty()) {
            showDetails(null);
        } else {
            settlementList.getSelectionModel().selectFirst();
        }
        Platform.runLater(settlementList::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Loc.nativeSettlementsTitle));
        buttonBar.setButtonText(i18n.get(Loc.ok));
        none.setText(i18n.get(Loc.nativeSettlementsNone));
        stopTeaching.setText(i18n.get(Loc.nativeTeachingStop));
        showOnMap.setText(i18n.get(Loc.nativeAdvisorShowOnMap));
        settlementList.refresh();
        showNations();
        showDetails(settlementList.getSelectionModel().getSelectedItem());
    }

    /**
     * The nations the player knows, named in words. The line stays hidden while
     * no nation is known.
     */
    private void showNations() {
        final boolean shown = !knownNations.isEmpty();
        nations.setVisible(shown);
        nations.setManaged(shown);
        nations.setText(i18n.get(Loc.nativeAdvisorNations,
                knownNations.stream()
                        .map(statusBarMessageService::nativeNationName)
                        .collect(Collectors.joining(", "))));
    }

    /**
     * Attitude, rate and provoker are three separate lines, each read from the
     * relation view of the selected settlement in the report; nothing is
     * computed here.
     */
    private void showDetails(final NativeAdvisorReport.Settlement row) {
        final boolean empty = row == null;
        none.setVisible(empty);
        none.setManaged(empty);
        unavailable.setVisible(false);
        unavailable.setManaged(false);
        final Optional<NativeRelationView> view = empty ? Optional.empty()
                : Optional.of(row.getRelation());
        final boolean shown = view.isPresent();
        attitude.setVisible(shown);
        attitude.setManaged(shown);
        alarmChange.setVisible(shown);
        alarmChange.setManaged(shown);
        rival.setVisible(shown);
        rival.setManaged(shown);
        showOnMap.setVisible(shown);
        showOnMap.setManaged(shown);
        view.ifPresent(relation -> {
            attitude.setText(
                    statusBarMessageService.nativeAttitudeText(relation));
            alarmChange.setText(
                    statusBarMessageService.nativeAlarmChangeText(relation));
            rival.setText(statusBarMessageService.nativeRivalText(relation));
        });
        showChief(Optional.ofNullable(row));
        showMission(empty ? Optional.empty()
                : gameModelController.getModel().getNativeMissionView(
                        gameModelController.getHumanPlayer(),
                        row.getSettlement()));
        showTeaching(empty ? Optional.empty()
                : gameModelController.getModel()
                        .getNativeTeachingCourse(row.getSettlement()));
    }

    /**
     * What the chief has told about the selected settlement, or that the chief
     * has told nothing yet. The line is hidden with no settlement selected.
     */
    private void showChief(final Optional<NativeAdvisorReport.Settlement> row) {
        final boolean shown = row.isPresent();
        chief.setVisible(shown);
        chief.setManaged(shown);
        row.ifPresent(settlement -> chief.setText(settlement.getChiefFacts()
                .map(statusBarMessageService::nativeChiefFactsText)
                .orElseGet(() -> i18n.get(Loc.nativeAdvisorChiefNotLearned))));
    }

    /**
     * Owner, expertise, the owner's attitude and the progress of the mission,
     * read from the model. The line stays while the missionary is stationed,
     * even when it is not on the map.
     */
    private void showMission(final Optional<NativeMissionView> view) {
        final boolean shown = view.isPresent();
        mission.setVisible(shown);
        mission.setManaged(shown);
        view.ifPresent(terms -> mission.setText(i18n.get(
                Loc.nativeMissionSummary, terms.getOwnerName(),
                i18n.get(DialogNativeMission
                        .expertName(terms.isEffectiveExpert())),
                i18n.get(DialogNativeChief.moodName(terms.getHostility())),
                terms.getCompletedUpdates(),
                NativeMissionView.REQUIRED_UPDATES)));
    }

    /**
     * Progress of the player's own course at this settlement, with the control
     * that stops it. Another player's course is not shown here.
     */
    private void showTeaching(final Optional<NativeTeachingCourse> course) {
        final Player human = gameModelController.getHumanPlayer();
        learner = course
                .flatMap(shown -> gameModelController.getModel()
                        .tryGetUnitById(shown.getPupilId()))
                .filter(unit -> unit.getOwner() == human);
        final boolean learning = learner.isPresent();
        teaching.setVisible(learning);
        teaching.setManaged(learning);
        stopTeaching.setVisible(learning);
        stopTeaching.setManaged(learning);
        course.ifPresent(shown -> {
            final String skill = i18n
                    .get(UnitTypes.getUnitName(shown.getSkill()));
            // A finished or canceled course never reads as still studying.
            switch (shown.getStage()) {
            case COMPLETED:
                teaching.setText(
                        i18n.get(Loc.nativeTeachingWaitingCompleted, skill));
                break;
            case CANCELED:
                teaching.setText(i18n.get(Loc.nativeTeachingWaitingCanceled));
                break;
            default:
                teaching.setText(i18n.get(Loc.nativeTeachingProgress, skill,
                        shown.getCompletedUpdates(),
                        shown.getRequiredUpdates()));
                break;
            }
            stopTeaching.setText(
                    i18n.get(shown.isAwaitingExit() ? Loc.nativeTeachingLeave
                            : Loc.nativeTeachingStop));
        });
    }

    /**
     * Center the map on the chosen settlement, but only while the running game
     * is the one this dialog read and the viewer still knows the settlement on
     * its square. Otherwise the dialog explains why nothing opens, and the map
     * stays as it is.
     */
    private void centerOnSettlement(final NativeAdvisorReport.Settlement row) {
        final NativeSettlement settlement = row.getSettlement();
        final boolean current = gameModelController.isGameModelReady()
                && gameModelController.getModel() == shownModel
                && shownModel
                        .getNativeSettlementKnownTo(
                                gameModelController.getHumanPlayer(),
                                settlement.getLocation())
                        .filter(known -> known == settlement).isPresent();
        if (!current) {
            unavailable.setText(i18n.get(Loc.nativeAdvisorUnavailable));
            unavailable.setVisible(true);
            unavailable.setManaged(true);
            return;
        }
        close();
        selectedTileManager.setSelectedTile(settlement.getLocation(),
                ScrollToFocusedTile.smoothScroll);
    }

}
