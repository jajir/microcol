package org.akropia.gui.screen.colony;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.util.InGamePage;
import org.akropia.i18n.I18n;
import org.akropia.model.CargoSlot;
import org.akropia.model.Colony;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteAssignment;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteMode;
import org.akropia.model.TradeRouteProblem;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.TradeRouteStopProblem;
import org.akropia.model.TradeRouteStopResult;
import org.akropia.model.TradeRouteTransfer;
import org.akropia.model.unit.UnitWithCargo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Management of the player's saved trade routes in three tabs: Stations,
 * Carriers and Goods. Stations and Goods edit one draft of the whole route, its
 * ordered stops, the goods instruction of every stop and the route-wide mode,
 * which the shared Save stores through the model and Cancel changes discards.
 * Switching tabs keeps the draft; leaving the screen or choosing another route
 * with unsaved changes asks for a second press. Carriers are added and removed
 * at once through the model, and a carrier standing at a stop is processed only
 * by its explicit button. Opening the screen or loading a game moves nothing;
 * every rule, quantity and waiting reason comes from the model.
 */
@Singleton
public final class DialogTradeRouteCargo implements GameScreen {

    /** Leaving the screen with unsaved changes. */
    private static final int LEAVE_BACK = -1;

    private final GameModelController game;
    private final I18n i18n;
    private final EventBus eventBus;
    private final ImageProvider images;
    private final Label caption = new Label();
    private final MenuButton routeMenu = ActionButtons.menu(ButtonIcon.TRADE);
    private final Button close = ActionButtons.button(ButtonIcon.BACK);
    private final Label routeSummary = new Label();
    private final TabPane tabs = new TabPane();
    private final Tab stationsTab = new Tab();
    private final Tab carriersTab = new Tab();
    private final Tab goodsTab = new Tab();
    /** Route whose draft and carriers the screen shows, if any. */
    private TradeRoute selectedRoute;
    /** Staged stops with their goods, in travel order. */
    private final List<TradeRouteStop> draftStops = new ArrayList<>();
    private TradeRouteMode draftMode = TradeRouteMode.TOUCH_AND_GO;
    /** Leave request that already showed the unsaved warning, if any. */
    private Integer leaveAskedFor;

    // Stations
    private final Label stationsRule = new Label();
    private final VBox stationRows = new VBox(6);
    private final Label addStationLabel = new Label();
    private final ComboBox<TradeRouteStop> addStationBox = new ComboBox<>();
    private final Button addStation = ActionButtons.button(ButtonIcon.ADD);
    private final Label stationStatus = new Label();
    private final TitledPane deletePane = new TitledPane();
    private final Button deleteRoute = ActionButtons.button(ButtonIcon.DELETE);
    private final Label deleteInfo = new Label();
    /** Route whose deletion was asked for and awaits the second press. */
    private Integer deleteAskedFor;

    // Carriers
    private final Label carriersRule = new Label();
    private final VBox carrierRows = new VBox(8);
    private final MenuButton addCarrier = ActionButtons.menu(ButtonIcon.ADD);
    /** Explicit processing of a carrier standing at a stop, collapsed. */
    private final TitledPane processPane = new TitledPane();
    private final Label processRule = new Label();
    private final ComboBox<UnitWithCargo> carrierBox = new ComboBox<>();
    /** Stop index of each carrier offered for explicit processing. */
    private final Map<UnitWithCargo, Integer> carrierStops = new LinkedHashMap<>();
    private final Label carrierStatus = new Label();
    private final Button process = ActionButtons.button(ButtonIcon.PLAY);
    private final Label resultHeading = new Label();
    private final Label result = new Label();

    // Goods
    private final Label goodsRule = new Label();
    private final GridPane matrix = new GridPane();
    private final Label modeLabel = new Label();
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final RadioButton touchAndGo = new RadioButton();
    private final RadioButton waitForCompletion = new RadioButton();

    // Shared draft actions
    private final Button save = ActionButtons.button(ButtonIcon.SAVE);
    private final Button discard = ActionButtons.button(ButtonIcon.CANCEL);
    private final Label message = new Label();

    private final InGamePage page;
    private final org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning planning;
    private Colony originColony;
    private Model model;
    private Player player;
    /** Set while controls are rebuilt, so their own events do not recurse. */
    private boolean refreshing;

    @Inject
    public DialogTradeRouteCargo(final I18n i18n,
            final GameModelController game, final EventBus eventBus,
            final InGamePage page, final ImageProvider images,
            final org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning planning) {
        this.planning = planning;
        this.page = page;
        this.i18n = i18n;
        this.game = game;
        this.eventBus = eventBus;
        this.images = images;
        caption.setId("tradeRouteCargoTitle");
        routeMenu.setId("tradeRouteCargoRoute");
        routeMenu.getStyleClass().add("toolbar-menu");
        routeMenu.setMnemonicParsing(false);
        // Long route names shorten so Back stays reachable in narrow windows.
        routeMenu.setMaxWidth(320);
        close.setId("tradeRouteCargoClose");
        close.getStyleClass().add("toolbar-action");
        close.setMinWidth(Region.USE_PREF_SIZE);
        close.setCancelButton(true);
        close.setOnAction(event -> close());
        routeSummary.setId("tradeRouteCargoSummary");
        routeSummary.setWrapText(true);

        stationsTab.setId("tradeRouteStationsTab");
        carriersTab.setId("tradeRouteCarriersTab");
        goodsTab.setId("tradeRouteGoodsTab");
        stationsTab.setContent(buildStations());
        carriersTab.setContent(buildCarriers());
        goodsTab.setContent(buildGoods());
        tabs.getTabs().setAll(stationsTab, carriersTab, goodsTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setId("tradeRouteTabs");
        // The page scrolls; the tabs keep their full height instead of being
        // squeezed into the window, which would clip their lower controls.
        // TabBody makes the tabs measure that height again when it grows.
        tabs.setMinHeight(Region.USE_PREF_SIZE);

        save.setId("tradeRouteCargoSave");
        save.setOnAction(event -> saveRoute());
        discard.setId("tradeRouteCargoDiscard");
        discard.setOnAction(event -> discardChanges());
        message.setId("tradeRouteCargoMessage");
        message.setWrapText(true);
        message.setMinHeight(20);
        message.setMaxWidth(740);
        final HBox draftActions = new HBox(8, save, discard);
        draftActions.setId("tradeRouteDraftActions");

        final VBox root = new VBox(12, routeSummary, tabs, draftActions,
                message);
        root.setId("tradeRouteCargoDialog");
        page.setTitle(caption);
        final HBox actions = new HBox(3, routeMenu, close);
        actions.setId("tradeRouteCargoActions");
        page.setToolbar(actions);
        page.setContent(root);
        // Goods icons and cells explain themselves in the status bar.
        page.setStatusSource(Source.COLONY);
        page.getContent().lookupAll(".screen-scroll")
                .forEach(node -> node.setId("tradeRouteCargoScroll"));
        page.getContent().setId("tradeRouteCargoPage");
        page.getContent().getStylesheets().add(getClass()
                .getResource("/gui/TradeRouteCargo.css").toExternalForm());
        page.getContent().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
    }

    private VBox buildStations() {
        stationsRule.setWrapText(true);
        stationsRule.setMaxWidth(740);
        stationRows.setId("tradeRouteStationRows");
        addStationBox.setId("tradeRouteAddStation");
        addStationBox.setConverter(stopConverter());
        addStationBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
        addStationLabel.setLabelFor(addStationBox);
        addStation.setId("tradeRouteAddStationButton");
        addStation.setOnAction(event -> addStation());
        stationStatus.setId("tradeRouteStationStatus");
        stationStatus.setWrapText(true);
        stationStatus.setMaxWidth(740);
        deleteRoute.setId("tradeRouteDelete");
        deleteRoute.setOnAction(event -> deleteRoute());
        deleteInfo.setId("tradeRouteDeleteInfo");
        deleteInfo.setWrapText(true);
        deleteInfo.setMaxWidth(740);
        deletePane.setContent(new VBox(10, deleteRoute, deleteInfo));
        deletePane.setId("tradeRouteDeletePane");
        deletePane.setAnimated(false);
        deletePane.setExpanded(false);
        final VBox stations = new TabBody(10, stationsRule, stationRows,
                new HBox(8, addStationLabel, addStationBox, addStation),
                stationStatus, deletePane);
        stations.setId("tradeRouteStopsColumn");
        stations.getStyleClass().add("trade-route-column");
        return stations;
    }

    private VBox buildCarriers() {
        carriersRule.setWrapText(true);
        carriersRule.setMaxWidth(740);
        carrierRows.setId("tradeRouteCarrierRows");
        addCarrier.setId("tradeRouteAddCarrier");
        addCarrier.setMnemonicParsing(false);
        // The list is read from the model each time it opens; closing it
        // without a choice changes nothing.
        addCarrier.setOnShowing(event -> fillAddCarrier());
        processPane.setId("tradeRouteProcessPane");
        processPane.setAnimated(false);
        processPane.setExpanded(false);
        processRule.setWrapText(true);
        processRule.setMaxWidth(740);
        carrierBox.setId("tradeRouteCargoCarrier");
        carrierBox.setConverter(new StringConverter<UnitWithCargo>() {
            @Override
            public String toString(final UnitWithCargo carrier) {
                if (carrier == null) {
                    return "";
                }
                final Integer stop = carrierStops.get(carrier);
                return stop == null || selectedRoute == null
                        || stop >= selectedRoute.getStops().size()
                                ? carrierName(carrier)
                                : i18n.get(TradeRouteText.carrierAtStop,
                                        carrierName(carrier),
                                        stopName(selectedRoute.getStops()
                                                .get(stop)));
            }

            @Override
            public UnitWithCargo fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        carrierBox.setOnAction(event -> {
            if (!refreshing) {
                updateState();
            }
        });
        carrierStatus.setId("tradeRouteCargoCarrierStatus");
        carrierStatus.setWrapText(true);
        carrierStatus.setMaxWidth(740);
        process.setId("tradeRouteCargoProcess");
        process.setOnAction(event -> processStop());
        resultHeading.setId("tradeRouteCargoResultHeading");
        result.setId("tradeRouteCargoResult");
        result.setWrapText(true);
        result.setMaxWidth(740);
        processPane.setContent(new VBox(10, processRule, carrierBox,
                carrierStatus, process, resultHeading, result));
        final VBox carriers = new TabBody(10, carriersRule, carrierRows,
                addCarrier, processPane);
        carriers.setId("tradeRouteCarriersColumn");
        carriers.getStyleClass().add("trade-route-column");
        return carriers;
    }

    private VBox buildGoods() {
        goodsRule.setWrapText(true);
        goodsRule.setMaxWidth(740);
        matrix.setId("tradeRouteGoodsMatrix");
        matrix.getStyleClass().add("trade-route-matrix");
        matrix.setHgap(2);
        matrix.setVgap(2);
        touchAndGo.setId("tradeRouteModeTouch");
        waitForCompletion.setId("tradeRouteModeWait");
        touchAndGo.setToggleGroup(modeGroup);
        waitForCompletion.setToggleGroup(modeGroup);
        touchAndGo.setUserData(TradeRouteMode.TOUCH_AND_GO);
        waitForCompletion.setUserData(TradeRouteMode.WAIT_FOR_COMPLETION);
        modeGroup.selectedToggleProperty()
                .addListener((observable, previous, current) -> {
                    if (!refreshing && current != null) {
                        draftMode = (TradeRouteMode) current.getUserData();
                        leaveAskedFor = null;
                        updateState();
                    }
                });
        modeLabel.setId("tradeRouteModeLabel");
        final VBox goods = new TabBody(10, goodsRule, matrix, modeLabel,
                touchAndGo, waitForCompletion);
        goods.setId("tradeRouteGoodsColumn");
        goods.getStyleClass().add("trade-route-column");
        return goods;
    }

    private StringConverter<TradeRouteStop> stopConverter() {
        return new StringConverter<TradeRouteStop>() {
            @Override
            public String toString(final TradeRouteStop stop) {
                return stop == null ? "" : stopName(stop);
            }

            @Override
            public TradeRouteStop fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Back. Unsaved changes are kept after the first press, which only warns; a
     * second press leaves and discards them.
     */
    public void close() {
        if (isDirty() && !Objects.equals(leaveAskedFor, LEAVE_BACK)) {
            leaveAskedFor = LEAVE_BACK;
            message.setText(i18n.get(TradeRouteText.unsavedWarning));
            return;
        }
        leaveAskedFor = null;
        planning.invalidate();
        eventBus.post(originColony == null ? new ShowScreenEvent(Screen.GAME)
                : new ShowScreenEvent(Screen.COLONY, originColony));
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void beforeShow() {
        page.beforeShow();
    }

    @Override
    public void beforeHide() {
    }

    @Override
    public void updateLanguage(final I18n texts) {
        page.updateLanguage(texts);
        if (model != null) {
            translate();
            showDraft();
        }
    }

    private String carrierName(final UnitWithCargo carrier) {
        return i18n.get(TradeRouteText.carrierItem,
                i18n.get(UnitTypes.getUnitName(carrier.getType())),
                carrier.getId());
    }

    private String stopName(final TradeRouteStop stop) {
        if (stop.isEurope()) {
            return i18n.get(TradeRouteText.europe);
        }
        return model.getColonyById(stop.getColonyId()).map(Colony::getName)
                .orElse(i18n.get(TradeRouteText.stopLost));
    }

    private String goodsName(final GoodsType type) {
        return i18n.get(GoodsTypeName.getNameForGoodsType(type));
    }

    /** Sets the fixed texts in the language current when the screen opens. */
    private void translate() {
        caption.setText(i18n.get(TradeRouteText.cargoTitle));
        stationsTab.setText(i18n.get(TradeRouteText.tabStations));
        carriersTab.setText(i18n.get(TradeRouteText.tabCarriers));
        goodsTab.setText(i18n.get(TradeRouteText.tabGoods));
        stationsRule.setText(i18n.get(TradeRouteText.stationsRule));
        addStationLabel.setText(i18n.get(TradeRouteText.stationAddLabel));
        addStationBox.setAccessibleText(addStationLabel.getText());
        addStation.setText(i18n.get(TradeRouteText.stationAdd));
        deletePane.setText(i18n.get(TradeRouteText.deleteRoute));
        carriersRule.setText(i18n.get(TradeRouteText.carriersRule));
        addCarrier.setText(i18n.get(TradeRouteText.carrierAdd));
        processPane.setText(i18n.get(TradeRouteText.carriersSection));
        processRule.setText(i18n.get(TradeRouteText.processRule));
        carrierBox.setAccessibleText(i18n.get(TradeRouteText.carrierLabel));
        process.setText(i18n.get(TradeRouteText.processNow));
        resultHeading.setText(i18n.get(TradeRouteText.resultHeading));
        goodsRule.setText(i18n.get(TradeRouteText.goodsRule));
        modeLabel.setText(i18n.get(TradeRouteText.modeLabel));
        touchAndGo.setText(i18n.get(TradeRouteText.modeTOUCH_AND_GO));
        waitForCompletion
                .setText(i18n.get(TradeRouteText.modeWAIT_FOR_COMPLETION));
        save.setText(i18n.get(TradeRouteText.saveRoute));
        discard.setText(i18n.get(TradeRouteText.discardRoute));
        close.setText(i18n.get(TradeRouteText.closeCargo));
        showRouteChoice();
    }

    /**
     * Opens the screen over the colony screen. Nothing is changed unless the
     * player presses one of its actions.
     *
     * @param opened required colony screen the screen was opened from
     */
    public void showFor(final Colony opened) {
        originColony = opened;
        open(Optional.empty());
    }

    /**
     * Opens the screen from the map planner with one route chosen.
     *
     * @param route required route chosen in the planner
     */
    public void showForRoute(final TradeRoute route) {
        originColony = null;
        open(Optional.of(route.getId()));
    }

    private void open(final Optional<Integer> routeId) {
        this.model = game.getModel();
        this.player = game.getHumanPlayer();
        translate();
        message.setText("");
        result.setText("");
        resultHeading.setVisible(false);
        deleteAskedFor = null;
        leaveAskedFor = null;
        refreshing = true;
        try {
            // A choice or a carrier left from an earlier game is dropped.
            carrierBox.getItems().clear();
            carrierStops.clear();
            addStationBox.getItems().clear();
        } finally {
            refreshing = false;
        }
        tabs.getSelectionModel().select(stationsTab);
        reloadRoutes(routeId);
        eventBus.post(new ShowScreenEvent(Screen.TRADE_ROUTE_CARGO));
        Platform.runLater(() -> routeMenu.requestFocus());
    }

    /**
     * Rebuilds the route list from the model, keeping the chosen route, and
     * resets the draft to the stored route.
     */
    private void reloadRoutes(final Optional<Integer> keepRouteId) {
        refreshing = true;
        try {
            final List<TradeRoute> routes = model.getTradeRoutes(player);
            routeMenu.getItems().clear();
            routes.forEach(route -> {
                final MenuItem item = new MenuItem(route.getName());
                item.setId("tradeRouteChoice-" + route.getId());
                item.setMnemonicParsing(false);
                item.setOnAction(event -> chooseRoute(route.getId()));
                routeMenu.getItems().add(item);
            });
            if (routes.isEmpty()) {
                final MenuItem placeholder = new MenuItem(
                        i18n.get(TradeRouteText.noRoutes));
                placeholder.setDisable(true);
                routeMenu.getItems().add(placeholder);
            }
            selectedRoute = routes.stream()
                    .filter(route -> keepRouteId.isPresent()
                            && route.getId() == keepRouteId.get())
                    .findFirst()
                    .orElse(routes.isEmpty() ? null : routes.get(0));
            showRouteChoice();
        } finally {
            refreshing = false;
        }
        resetDraft();
    }

    /**
     * Shows another route of the player. Unsaved changes of the current route
     * are kept after the first choice, which only warns.
     */
    private void chooseRoute(final int routeId) {
        if (refreshing || (selectedRoute != null
                && selectedRoute.getId() == routeId)) {
            return;
        }
        if (isDirty() && !Objects.equals(leaveAskedFor, routeId)) {
            leaveAskedFor = routeId;
            message.setText(i18n.get(TradeRouteText.unsavedWarning));
            return;
        }
        leaveAskedFor = null;
        deleteAskedFor = null;
        message.setText("");
        result.setText("");
        resultHeading.setVisible(false);
        final Optional<TradeRoute> route = model.getTradeRoutes(player).stream()
                .filter(stored -> stored.getId() == routeId).findFirst();
        if (route.isEmpty()) {
            reloadRoutes(Optional.empty());
            return;
        }
        selectedRoute = route.get();
        showRouteChoice();
        resetDraft();
    }

    private void showRouteChoice() {
        final String label = i18n.get(TradeRouteText.cargoRouteLabel);
        routeMenu.setText(
                selectedRoute == null ? label : selectedRoute.getName());
        routeMenu.setAccessibleText(selectedRoute == null ? label
                : label + ": " + selectedRoute.getName());
    }

    /** Replaces the draft by the stored route. */
    private void resetDraft() {
        draftStops.clear();
        if (selectedRoute != null) {
            draftStops.addAll(selectedRoute.getStops());
            draftMode = selectedRoute.getMode();
        } else {
            draftMode = TradeRouteMode.TOUCH_AND_GO;
        }
        leaveAskedFor = null;
        showDraft();
    }

    private boolean isDirty() {
        final TradeRoute route = selectedRoute;
        if (route == null) {
            return false;
        }
        if (draftMode != route.getMode()
                || draftStops.size() != route.getStops().size()) {
            return true;
        }
        for (int index = 0; index < draftStops.size(); index++) {
            if (!draftStops.get(index)
                    .isSameConfiguration(route.getStops().get(index))) {
                return true;
            }
        }
        return false;
    }

    /** Rebuilds every tab from the draft and the model. */
    private void showDraft() {
        final TradeRoute route = selectedRoute;
        routeSummary.setText(route == null ? ""
                : i18n.get(TradeRouteText.routeSummary,
                        i18n.get(route.getKind() == TradeRouteKind.SEA
                                ? TradeRouteText.sea
                                : TradeRouteText.land),
                        route.getStops().size()));
        refreshing = true;
        try {
            modeGroup.selectToggle(
                    draftMode == TradeRouteMode.WAIT_FOR_COMPLETION
                            ? waitForCompletion
                            : touchAndGo);
            touchAndGo.setDisable(route == null);
            waitForCompletion.setDisable(route == null);
            showStations();
            showMatrix();
            showCarriers();
            refreshProcessCarriers();
        } finally {
            refreshing = false;
        }
        updateState();
    }

    // ---------------------------------------------------------------- Stations

    private void showStations() {
        stationRows.getChildren().clear();
        for (int index = 0; index < draftStops.size(); index++) {
            final int at = index;
            final Label name = new Label(i18n.get(TradeRouteText.cargoStopItem,
                    index + 1, stopName(draftStops.get(index))));
            name.setId("tradeRouteStationName-" + index);
            name.getStyleClass().add("trade-route-station-name");
            final Button up = ActionButtons.button(ButtonIcon.UP,
                    i18n.get(TradeRouteText.stationUp));
            up.setId("tradeRouteStationUp-" + index);
            up.setDisable(index == 0);
            up.setAccessibleText(name.getText() + ": " + up.getText());
            up.setOnAction(event -> moveStation(at, -1));
            final Button down = ActionButtons.button(ButtonIcon.DOWN,
                    i18n.get(TradeRouteText.stationDown));
            down.setId("tradeRouteStationDown-" + index);
            down.setDisable(index == draftStops.size() - 1);
            down.setAccessibleText(name.getText() + ": " + down.getText());
            down.setOnAction(event -> moveStation(at, 1));
            final Button remove = ActionButtons.button(ButtonIcon.REMOVE,
                    i18n.get(TradeRouteText.stationRemove));
            remove.setId("tradeRouteStationRemove-" + index);
            // A route keeps at least one stop.
            remove.setDisable(draftStops.size() <= 1);
            remove.setAccessibleText(name.getText() + ": " + remove.getText());
            remove.setOnAction(event -> removeStation(at));
            final HBox row = new HBox(8, name, up, down, remove);
            row.setId("tradeRouteStation-" + index);
            row.getStyleClass().add("trade-route-row");
            stationRows.getChildren().add(row);
        }
        final TradeRoute route = selectedRoute;
        final List<TradeRouteStop> options = route == null ? List.of()
                : model.getTradeRouteAddableStops(player, route, draftStops);
        addStationBox.getItems().setAll(options);
        addStationBox.setValue(options.isEmpty() ? null : options.get(0));
        addStationBox.setDisable(options.isEmpty());
    }

    private void moveStation(final int index, final int offset) {
        final int target = index + offset;
        if (target < 0 || target >= draftStops.size()) {
            return;
        }
        draftStops.add(target, draftStops.remove(index));
        leaveAskedFor = null;
        showDraft();
        focusStationButton(target, offset < 0 ? "Up" : "Down");
    }

    /** Keeps keyboard work in place after the rows were rebuilt. */
    private void focusStationButton(final int index, final String kind) {
        final Node button = stationRows
                .lookup("#tradeRouteStation" + kind + "-" + index);
        if (button != null && !button.isDisabled()) {
            button.requestFocus();
        } else {
            final Node name = stationRows.lookup("#tradeRouteStation-" + index);
            if (name != null) {
                name.requestFocus();
            }
        }
    }

    private void removeStation(final int index) {
        if (draftStops.size() <= 1) {
            return;
        }
        draftStops.remove(index);
        leaveAskedFor = null;
        showDraft();
    }

    private void addStation() {
        final TradeRouteStop choice = addStationBox.getValue();
        if (choice == null || draftStops.contains(choice)) {
            return;
        }
        draftStops.add(choice.isEurope() ? TradeRouteStop.europe()
                : TradeRouteStop.ofColony(choice.getColonyId()));
        leaveAskedFor = null;
        showDraft();
        Platform.runLater(addStationBox::requestFocus);
    }

    // ------------------------------------------------------------------ Goods

    /**
     * Rows are the draft stops in their order; columns are every transportable
     * goods type in the fixed colony order, shown by icon only. Each cell
     * cycles ignore, load and unload.
     */
    private void showMatrix() {
        matrix.getChildren().clear();
        int column = 1;
        for (final GoodsType type : TradeRouteStop.TRANSPORTABLE_GOODS) {
            final ImageView icon = new ImageView(
                    images.getGoodsTypeImage(type));
            icon.setFitWidth(28);
            icon.setFitHeight(28);
            icon.setPreserveRatio(true);
            final Label header = new Label();
            header.setGraphic(icon);
            header.setId("tradeRouteGoodsHeader-" + type.name());
            // The icon alone is shown; its name is the accessible text.
            header.setAccessibleText(goodsName(type));
            help(header, () -> goodsName(type));
            header.getStyleClass().add("trade-route-goods-header");
            matrix.add(header, column++, 0);
        }
        for (int row = 0; row < draftStops.size(); row++) {
            final TradeRouteStop stop = draftStops.get(row);
            final Label name = new Label(i18n.get(TradeRouteText.cargoStopItem,
                    row + 1, stopName(stop)));
            name.setId("tradeRouteGoodsStop-" + row);
            name.getStyleClass().add("trade-route-station-name");
            matrix.add(name, 0, row + 1);
            column = 1;
            for (final GoodsType type : TradeRouteStop.TRANSPORTABLE_GOODS) {
                matrix.add(makeCell(row, type), column++, row + 1);
            }
        }
    }

    private Button makeCell(final int row, final GoodsType type) {
        // The shared factory's load icon marks the cell as a cargo action;
        // the cell itself shows only the agreed +, \u2212 or empty sign.
        final Button cell = ActionButtons.button(ButtonIcon.LOAD);
        cell.setContentDisplay(ContentDisplay.TEXT_ONLY);
        cell.setId("tradeRouteCell-" + row + "-" + type.name());
        cell.getStyleClass().add("trade-route-cell");
        cell.setFocusTraversable(true);
        showCell(cell, row, type);
        cell.setOnAction(event -> cycleCell(cell, row, type));
        help(cell, () -> cell.getAccessibleText() + ". "
                + i18n.get(TradeRouteText.cellHint));
        return cell;
    }

    /**
     * Shows the text in the status bar while the pointer is over the node or it
     * has the keyboard focus, as the colony goods do.
     */
    private void help(final Node node,
            final java.util.function.Supplier<String> text) {
        node.setOnMouseEntered(event -> showHelp(text.get()));
        node.setOnMouseExited(event -> eventBus
                .post(new StatusBarMessageEvent(Source.COLONY)));
        node.focusedProperty().addListener((observable, before, now) -> {
            if (now) {
                showHelp(text.get());
            }
        });
    }

    private void showHelp(final String text) {
        eventBus.post(new StatusBarMessageEvent(text, Source.COLONY));
    }

    private void showCell(final Button cell, final int row,
            final GoodsType type) {
        final TradeRouteStop stop = draftStops.get(row);
        final TradeRouteText state;
        if (stop.getLoadGoods().contains(type)) {
            cell.setText("+");
            state = stop.isEurope() ? TradeRouteText.cellBuy
                    : TradeRouteText.cellLoad;
        } else if (stop.getUnloadGoods().contains(type)) {
            cell.setText("−");
            state = stop.isEurope() ? TradeRouteText.cellSell
                    : TradeRouteText.cellUnload;
        } else {
            cell.setText("");
            state = TradeRouteText.cellIgnore;
        }
        cell.getStyleClass().removeAll("load", "unload");
        if (!cell.getText().isEmpty()) {
            cell.getStyleClass()
                    .add(cell.getText().equals("+") ? "load" : "unload");
        }
        final String text = i18n.get(TradeRouteText.cellAccessible,
                stopName(stop), goodsName(type), i18n.get(state));
        cell.setAccessibleText(text);
    }

    /** Ignore, then load, then unload, then ignore again. */
    private void cycleCell(final Button cell, final int row,
            final GoodsType type) {
        final TradeRouteStop stop = draftStops.get(row);
        final List<GoodsType> unload = new ArrayList<>(stop.getUnloadGoods());
        final List<GoodsType> load = new ArrayList<>(stop.getLoadGoods());
        if (load.remove(type)) {
            unload.add(type);
        } else if (!unload.remove(type)) {
            load.add(type);
        }
        draftStops.set(row, stop.withCargo(unload, load));
        leaveAskedFor = null;
        showCell(cell, row, type);
        showHelp(cell.getAccessibleText() + ". "
                + i18n.get(TradeRouteText.cellHint));
        updateState();
    }

    // --------------------------------------------------------------- Carriers

    /**
     * One row per carrier that follows the stored route: type and ID, the stop
     * it is heading to or standing at, why it waits, its real holds and a
     * Remove button.
     */
    private void showCarriers() {
        carrierRows.getChildren().clear();
        final TradeRoute route = selectedRoute;
        final List<UnitWithCargo> carriers = route == null ? List.of()
                : model.getTradeRouteAssignedCarriers(player, route);
        if (carriers.isEmpty()) {
            final Label none = new Label(i18n.get(TradeRouteText.assignedNone));
            none.setId("tradeRouteCarrierNone");
            carrierRows.getChildren().add(none);
        }
        for (final UnitWithCargo carrier : carriers) {
            carrierRows.getChildren().add(carrierRow(carrier));
        }
        addCarrier.setDisable(route == null);
    }

    /**
     * One carrier: its name, destination and Remove on the first line, the
     * state below, and every real hold on the third line, so six holds fit a
     * 1024-pixel window.
     */
    private VBox carrierRow(final UnitWithCargo carrier) {
        final int id = carrier.getId();
        final Label name = new Label(carrierName(carrier));
        name.setId("tradeRouteCarrierName-" + id);
        name.getStyleClass().add("trade-route-station-name");
        final Label destination = new Label(destinationText(carrier));
        destination.setId("tradeRouteCarrierDestination-" + id);
        final Label state = new Label(stateText(carrier));
        state.setId("tradeRouteCarrierState-" + id);
        state.setWrapText(true);
        state.setMaxWidth(700);
        final HBox cargo = new HBox(2);
        cargo.setId("tradeRouteCarrierCargo-" + id);
        int slotIndex = 0;
        for (final CargoSlot slot : carrier.getCargo().getSlots()) {
            cargo.getChildren().add(slotBox(id, slotIndex++, slot));
        }
        final Button remove = ActionButtons.button(ButtonIcon.REMOVE,
                i18n.get(TradeRouteText.carrierRemove));
        remove.setId("tradeRouteCarrierRemove-" + id);
        remove.setAccessibleText(name.getText() + ": " + remove.getText());
        remove.setDisable(
                model.getTradeRouteCancelProblem(carrier).isPresent());
        remove.setOnAction(event -> removeCarrier(carrier));
        final HBox heading = new HBox(10, name, destination, remove);
        heading.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        final VBox row = new VBox(4, heading, state, cargo);
        row.setId("tradeRouteCarrierRow-" + id);
        row.getStyleClass().add("trade-route-row");
        return row;
    }

    private Label slotBox(final int unitId, final int index,
            final CargoSlot slot) {
        final Label box = new Label();
        box.setId("tradeRouteCarrierSlot-" + unitId + "-" + index);
        box.getStyleClass().add("trade-route-slot");
        final Optional<Goods> goods = slot.getGoods();
        final String text;
        if (goods.isPresent()) {
            final ImageView icon = new ImageView(
                    images.getGoodsTypeImage(goods.get().getType()));
            icon.setFitWidth(22);
            icon.setFitHeight(22);
            icon.setPreserveRatio(true);
            box.setGraphic(icon);
            box.setText(String.valueOf(goods.get().getAmount()));
            text = i18n.get(TradeRouteText.cargoSlot,
                    goodsName(goods.get().getType()), goods.get().getAmount());
        } else if (slot.getUnit().isPresent()) {
            box.setText("☺");
            text = i18n.get(TradeRouteText.cargoPassenger);
        } else {
            text = i18n.get(TradeRouteText.cargoEmptySlot);
        }
        box.setAccessibleText(text);
        return box;
    }

    /**
     * The stop the model says the carrier is heading to or standing at; a
     * carrier without a usable route shows no destination.
     */
    private String destinationText(final UnitWithCargo carrier) {
        final Optional<TradeRouteAssignment> assignment = model
                .getTradeRouteAssignment(carrier);
        if (assignment.isEmpty() || assignment.get().getPause().filter(
                pause -> pause == org.akropia.model.TradeRoutePause.NO_ROUTE)
                .isPresent()) {
            return "";
        }
        return model.getTradeRouteHeadingStop(carrier).map(
                stop -> i18n.get(TradeRouteText.carrierToward, stopName(stop)))
                .orElse("");
    }

    private String stateText(final UnitWithCargo carrier) {
        final Optional<TradeRouteAssignment> assignment = model
                .getTradeRouteAssignment(carrier);
        if (assignment.isPresent() && assignment.get().getPause().isPresent()) {
            return i18n.get(TradeRouteText.valueOf(
                    "pause" + assignment.get().getPause().get().name()));
        }
        if (carrier.isAtHighSea()) {
            return i18n.get(TradeRouteText.stateAtSea);
        }
        return i18n.get(TradeRouteText.stateMoving);
    }

    private void fillAddCarrier() {
        final TradeRoute route = selectedRoute;
        addCarrier.getItems().clear();
        final List<UnitWithCargo> eligible = route == null ? List.of()
                : model.getTradeRouteEligibleCarriers(player, route);
        for (final UnitWithCargo carrier : eligible) {
            final MenuItem item = new MenuItem(carrierName(carrier));
            item.setId("tradeRouteAddCarrier-" + carrier.getId());
            item.setMnemonicParsing(false);
            item.setOnAction(event -> addCarrier(carrier));
            addCarrier.getItems().add(item);
        }
        if (eligible.isEmpty()) {
            final MenuItem none = new MenuItem(
                    i18n.get(TradeRouteText.carrierAddNone));
            none.setDisable(true);
            addCarrier.getItems().add(none);
        }
    }

    /**
     * Joins the carrier to the stored route from its nearest legal stop, by the
     * same rule as the carrier's own route command.
     */
    private void addCarrier(final UnitWithCargo carrier) {
        final TradeRoute route = selectedRoute;
        if (route == null) {
            return;
        }
        try {
            final TradeRouteAssignment current = model
                    .getTradeRouteAssignment(carrier).orElse(null);
            final int first = model.getNearestTradeRouteStop(carrier, route)
                    .orElseThrow(
                            () -> new IllegalStateException("No legal stop"));
            model.changeTradeRoute(carrier, route, current);
            message.setText(
                    i18n.get(TradeRouteText.carrierAdded, carrierName(carrier),
                            stopName(route.getStops().get(first))));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            message.setText(i18n.get(TradeRouteText.changed));
        }
        showCarriers();
        refreshProcessCarriers();
        updateState();
    }

    /**
     * Returns only this carrier to manual orders. It does not move and keeps
     * its cargo; the other carriers continue.
     */
    private void removeCarrier(final UnitWithCargo carrier) {
        try {
            model.cancelTradeRouteAssignment(carrier);
            message.setText(i18n.get(TradeRouteText.cancelAssignmentDone,
                    carrierName(carrier)));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            message.setText(i18n.get(TradeRouteText.changed));
        }
        showCarriers();
        refreshProcessCarriers();
        updateState();
    }

    /** Carriers standing at any stop of the stored route, by stop order. */
    private void refreshProcessCarriers() {
        final TradeRoute route = selectedRoute;
        final UnitWithCargo previous = carrierBox.getValue();
        carrierStops.clear();
        if (route != null) {
            for (int index = 0; index < route.getStops().size(); index++) {
                for (final UnitWithCargo carrier : model
                        .getTradeRouteCarriers(player, route, index)) {
                    carrierStops.putIfAbsent(carrier, index);
                }
            }
        }
        final List<UnitWithCargo> carriers = new ArrayList<>(
                carrierStops.keySet());
        final boolean wasRefreshing = refreshing;
        refreshing = true;
        try {
            carrierBox.getItems().setAll(carriers);
            carrierBox.setValue(carriers.contains(previous) ? previous
                    : carriers.isEmpty() ? null : carriers.get(0));
        } finally {
            refreshing = wasRefreshing;
        }
    }

    // ------------------------------------------------------------ Shared state

    /** Enables the actions that fit the draft and the stored state. */
    private void updateState() {
        if (refreshing) {
            return;
        }
        final TradeRoute route = selectedRoute;
        final boolean dirty = isDirty();
        final Optional<TradeRouteProblem> problem = route == null || !dirty
                ? Optional.empty()
                : model.getTradeRouteUpdateProblem(player, route, draftStops,
                        draftMode);
        save.setDisable(!dirty || problem.isPresent());
        discard.setDisable(!dirty);
        stationStatus.setText(problem.map(this::editProblemText)
                .orElse(dirty ? i18n.get(TradeRouteText.stationPending) : ""));
        addStation.setDisable(addStationBox.getValue() == null);
        // Explicit processing
        final UnitWithCargo carrier = carrierBox.getValue();
        String status;
        boolean ready = false;
        if (route == null) {
            status = i18n.get(TradeRouteText.cargoNoRoutes);
        } else if (carrier == null) {
            status = i18n.get(TradeRouteText.noCarrier);
        } else if (dirty) {
            status = i18n.get(TradeRouteText.cargoUnsaved);
        } else {
            final Optional<TradeRouteStopProblem> stopProblem = model
                    .getTradeRouteStopProblem(carrier, route,
                            carrierStops.get(carrier));
            status = stopProblem.map(this::problemText)
                    .orElse(i18n.get(TradeRouteText.carrierReady));
            ready = stopProblem.isEmpty();
        }
        carrierStatus.setText(status);
        process.setDisable(!ready);
        carrierBox.setDisable(carrierBox.getItems().isEmpty());
        // Delete
        final boolean asked = route != null && deleteAskedFor != null
                && deleteAskedFor == route.getId();
        deleteRoute.setText(i18n.get(asked ? TradeRouteText.confirmDeleteRoute
                : TradeRouteText.deleteRoute));
        deleteRoute.setDisable(route == null
                || model.getTradeRouteDeleteProblem(player, route).isPresent());
        deleteInfo.setText(route == null ? ""
                : i18n.get(
                        asked ? TradeRouteText.deleteConfirm
                                : TradeRouteText.deleteReleases,
                        affectedCarriers(route)));
    }

    /** Names of the carriers that follow the route, or a sentence for none. */
    private String affectedCarriers(final TradeRoute route) {
        final List<String> names = model
                .getTradeRouteAssignedCarriers(player, route).stream()
                .map(this::carrierName).collect(Collectors.toList());
        return names.isEmpty() ? i18n.get(TradeRouteText.affectedNone)
                : String.join(", ", names);
    }

    private String editProblemText(final TradeRouteProblem problem) {
        return i18n.get(TradeRouteText.valueOf("editProblem" + problem.name()));
    }

    private String problemText(final TradeRouteStopProblem problem) {
        return i18n.get(TradeRouteText.valueOf("problem" + problem.name()));
    }

    /** Stores the whole draft through the model; nothing changes if refused. */
    private void saveRoute() {
        final TradeRoute route = selectedRoute;
        if (route == null || !isDirty()) {
            return;
        }
        try {
            final TradeRoute stored = model.updateTradeRoute(player, route,
                    new ArrayList<>(draftStops), draftMode);
            reloadRoutes(Optional.of(stored.getId()));
            message.setText(i18n.get(TradeRouteText.routeSaved));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            // The route or colony changed; nothing was stored. The draft stays
            // so the player can review it.
            message.setText(i18n.get(TradeRouteText.changed));
            updateState();
        }
    }

    private void discardChanges() {
        resetDraft();
        message.setText(i18n.get(TradeRouteText.routeDiscarded));
    }

    /**
     * Deletes the chosen route. Assigned carriers return to manual orders where
     * they stand; the message says how many were released.
     */
    private void deleteRoute() {
        final TradeRoute route = selectedRoute;
        if (route == null) {
            return;
        }
        message.setText("");
        // The first press only names the affected carriers; the second press
        // on the same route deletes it, so a stray key press cannot.
        if (deleteAskedFor == null || deleteAskedFor != route.getId()) {
            deleteAskedFor = route.getId();
            updateState();
            return;
        }
        deleteAskedFor = null;
        final int released = model.getTradeRouteAssignedCarriers(player, route)
                .size();
        try {
            model.deleteTradeRoute(player, route);
            reloadRoutes(Optional.empty());
            message.setText(i18n.get(TradeRouteText.routeDeleted, released));
        } catch (final IllegalArgumentException | IllegalStateException stale) {
            reloadRoutes(Optional.of(route.getId()));
            message.setText(i18n.get(TradeRouteText.changed));
        }
    }

    private void processStop() {
        final TradeRoute route = selectedRoute;
        final UnitWithCargo carrier = carrierBox.getValue();
        if (route == null || carrier == null || isDirty()) {
            return;
        }
        final Integer index = carrierStops.get(carrier);
        message.setText("");
        final TradeRouteStopResult outcome;
        try {
            outcome = model.processTradeRouteStop(carrier, route, index);
        } catch (final IllegalStateException stale) {
            message.setText(i18n.get(TradeRouteText.changed));
            refreshProcessCarriers();
            updateState();
            return;
        }
        resultHeading.setVisible(true);
        result.setText(describe(outcome));
        // The colony screen behind this screen shows the moved goods.
        eventBus.post(new RepaintColonyEvent());
        showCarriers();
        refreshProcessCarriers();
        updateState();
    }

    private String describe(final TradeRouteStopResult outcome) {
        if (outcome.getProblem().isPresent()) {
            return problemText(outcome.getProblem().get());
        }
        if (outcome.getTransfers().isEmpty()) {
            return i18n.get(TradeRouteText.noEntries);
        }
        return outcome.getTransfers().stream().map(this::entryText)
                .collect(Collectors.joining("\n"));
    }

    private String entryText(final TradeRouteTransfer entry) {
        final String verb = i18n
                .get(entry.isUnload() ? TradeRouteText.verbUnload
                        : TradeRouteText.verbLoad);
        final String goods = goodsName(entry.getGoodsType());
        final String reason = i18n.get(
                TradeRouteText.valueOf("reason" + entry.getReason().name()))
                + entry.getDetail().map(detail -> ": " + detail).orElse("");
        if (entry.getGoldChange() == 0) {
            return i18n.get(TradeRouteText.entryLine, verb, goods,
                    entry.getAmount(), reason);
        }
        return i18n.get(TradeRouteText.entryGold, verb, goods,
                entry.getAmount(), reason,
                String.format("%+d", entry.getGoldChange()));
    }

    /**
     * Content of one tab that makes the tab pane measure it again. The tab pane
     * skin keeps each tab's content in an unmanaged region, a layout root, so a
     * growing station list or an expanded section stops its layout request
     * there and the tab pane keeps its earlier preferred height; its lower
     * controls would be clipped. Forwarding the request to the tab pane lets it
     * grow, and the scrolled page around it measures it in turn.
     */
    private static final class TabBody extends VBox {

        TabBody(final double spacing, final Node... children) {
            super(spacing, children);
        }

        @Override
        public void requestLayout() {
            super.requestLayout();
            for (Parent parent = getParent(); parent != null; parent = parent
                    .getParent()) {
                if (parent instanceof TabPane) {
                    parent.requestLayout();
                    return;
                }
            }
        }
    }
}
