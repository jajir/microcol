package org.akropia.gui.screen.nativesettlements;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.Loc;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.components.StatusBarMessageService;
import org.akropia.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.akropia.gui.screen.game.gamepanel.SelectedTileManager;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ButtonBarOk;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Model;
import org.akropia.model.NativeAdvisorReport;
import org.akropia.model.NativeNation;
import org.akropia.model.NativeSettlement;
import org.akropia.model.Player;
import org.akropia.model.Unit;

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

    private final Label rival = new Label();

    private final Button showOnMap = ActionButtons.button(ButtonIcon.CENTER);

    private final Label unavailable = new Label();

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
        buttonBar = new ButtonBarOk(i18n.get(org.akropia.gui.Loc.close));
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
        rival.setId(RIVAL_ID);
        rival.setWrapText(true);
        showOnMap.setId(SHOW_ON_MAP_ID);
        showOnMap.setOnAction(e -> Optional
                .ofNullable(
                        settlementList.getSelectionModel().getSelectedItem())
                .ifPresent(this::centerOnSettlement));
        unavailable.setId(UNAVAILABLE_ID);
        unavailable.setWrapText(true);
        final VBox details = new VBox(8, none, attitude, rival, showOnMap,
                unavailable);
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
        buttonBar.setButtonText(i18n.get(Loc.close));
        none.setText(i18n.get(Loc.nativeSettlementsNone));
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
        final boolean shown = row != null;
        none.setVisible(!shown);
        none.setManaged(!shown);
        unavailable.setVisible(false);
        unavailable.setManaged(false);
        attitude.setVisible(shown);
        attitude.setManaged(shown);
        rival.setVisible(shown);
        rival.setManaged(shown);
        showOnMap.setVisible(shown);
        showOnMap.setManaged(shown);
        if (shown) {
            final Model model = gameModelController.getModel();
            final NativeNation nation = row.getSettlement().getNation();
            attitude.setText(model.getPlayers().stream()
                    .filter(Player::isColonialNation)
                    .map(player -> i18n.get(Loc.nativeFavour,
                            player.getDisplayName(),
                            model.getNativeFavour(nation,
                                    org.akropia.model.EuropeanNation
                                            .fromPlayerName(player.getName()))))
                    .collect(Collectors.joining("\n")));
            rival.setText(model.getNativeFriend(nation)
                    .map(friend -> i18n.get(Loc.nativeFriend,
                            friend.getDisplayName()))
                    .orElseGet(() -> i18n.get(Loc.nativeNoFriend)));
        }
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
