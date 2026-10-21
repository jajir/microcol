package org.microcol.gui.screen.nativesettlements;

import java.util.Optional;

import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
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
 * Keyboard-reachable details of the native settlements the player has learned.
 * Each opening reads the list and the relation values from the model, so the
 * dialog never shows a stale value and never changes a relation.
 */
public final class NativeSettlementsDialog extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeSettlementsContent";

    public static final String LIST_ID = "nativeSettlementsList";

    public static final String ATTITUDE_ID = "nativeSettlementAttitude";

    public static final String ALARM_CHANGE_ID = "nativeSettlementAlarmChange";

    public static final String RIVAL_ID = "nativeSettlementRival";

    public static final String NONE_ID = "nativeSettlementsNone";

    public static final String TEACHING_ID = "nativeSettlementTeaching";

    public static final String STOP_TEACHING_ID = "nativeSettlementStopTeaching";

    private final I18n i18n;

    private final GameModelController gameModelController;

    private final StatusBarMessageService statusBarMessageService;

    private final ButtonBarOk buttonBar;

    private final ObservableList<NativeSettlement> settlements = FXCollections
            .observableArrayList();

    private final ListView<NativeSettlement> settlementList = new ListView<>(
            settlements);

    private final Label none = new Label();

    private final Label attitude = new Label();

    private final Label alarmChange = new Label();

    private final Label rival = new Label();

    private final Label teaching = new Label();

    private final Button stopTeaching = new Button();

    /** The colonist of the shown course, when it is the player's own. */
    private Optional<Unit> learner = Optional.empty();

    @Inject
    public NativeSettlementsDialog(final I18n i18n, final ViewUtil viewUtil,
            final GameModelController gameModelController,
            final StatusBarMessageService statusBarMessageService) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.gameModelController = gameModelController;
        this.statusBarMessageService = statusBarMessageService;
        buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        settlementList.setId(LIST_ID);
        settlementList.setPrefSize(300, 170);
        settlementList.setCellFactory(list -> new ListCell<NativeSettlement>() {

            @Override
            protected void updateItem(final NativeSettlement settlement,
                    final boolean empty) {
                super.updateItem(settlement, empty);
                setText(empty || settlement == null ? null
                        : statusBarMessageService
                                .nativeSettlementText(settlement));
            }

        });
        settlementList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldSettlement,
                        newSettlement) -> showDetails(newSettlement));

        none.setId(NONE_ID);
        none.setWrapText(true);
        attitude.setId(ATTITUDE_ID);
        attitude.setWrapText(true);
        alarmChange.setId(ALARM_CHANGE_ID);
        alarmChange.setWrapText(true);
        rival.setId(RIVAL_ID);
        rival.setWrapText(true);
        teaching.setId(TEACHING_ID);
        teaching.setWrapText(true);
        stopTeaching.setId(STOP_TEACHING_ID);
        stopTeaching.setOnAction(e -> {
            learner.ifPresent(gameModelController::cancelNativeTeaching);
            close();
        });
        final VBox details = new VBox(8, none, attitude, alarmChange, rival,
                teaching, stopTeaching);
        details.setPrefWidth(300);
        final HBox main = new HBox(20, settlementList, details);
        final VBox root = new VBox(10, main, buttonBar.getContent());
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
     * Each opening reads the known settlements again and puts keyboard focus on
     * the list, so arrow keys and Escape work without a mouse.
     */
    @Override
    public void showAndWait() {
        final Player human = gameModelController.getHumanPlayer();
        settlements.setAll(gameModelController.getModel()
                .getNativeSettlementsKnownTo(human));
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
        settlementList.refresh();
        showDetails(settlementList.getSelectionModel().getSelectedItem());
    }

    /**
     * Attitude, rate and provoker are three separate lines, each read from the
     * model view of the selected settlement; nothing is computed here.
     */
    private void showDetails(final NativeSettlement settlement) {
        final boolean empty = settlement == null;
        none.setVisible(empty);
        none.setManaged(empty);
        final Optional<NativeRelationView> view = empty ? Optional.empty()
                : gameModelController.getModel().getNativeRelationView(
                        gameModelController.getHumanPlayer(), settlement);
        final boolean shown = view.isPresent();
        attitude.setVisible(shown);
        attitude.setManaged(shown);
        alarmChange.setVisible(shown);
        alarmChange.setManaged(shown);
        rival.setVisible(shown);
        rival.setManaged(shown);
        view.ifPresent(relation -> {
            attitude.setText(
                    statusBarMessageService.nativeAttitudeText(relation));
            alarmChange.setText(
                    statusBarMessageService.nativeAlarmChangeText(relation));
            rival.setText(statusBarMessageService.nativeRivalText(relation));
        });
        showTeaching(empty ? Optional.empty()
                : gameModelController.getModel()
                        .getNativeTeachingCourse(settlement));
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

}
