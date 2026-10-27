package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.util.ViewUtil;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.MicroColException;
import org.microcol.model.TurnPlayerStatistics;
import org.microcol.model.campaign.po.HallOfFameDao;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class StatisticsPanel implements GameScreen {

    private final static String KEY_RELATIONS = "relations";
    private final EuropeanRelationsPanel relations;
    private final ToggleButton relationsButton = ActionButtons
            .toggle(ButtonIcon.DIPLOMACY);

    private final static String KEY_TRADE = "trade";
    private final EuropeanTradePanel trade;
    private final ToggleButton tradeButton = ActionButtons
            .toggle(ButtonIcon.TRADE);

    private final static String KEY_BASE_SCORE = "baseScore";
    private final BaseScorePanel baseScore;
    private final ToggleButton baseScoreButton = ActionButtons
            .toggle(ButtonIcon.STATISTICS);

    private final static String KEY_HALL_OF_FAME = "hallOfFame";
    private final HallOfFamePanel hallOfFame;
    private final ToggleButton hallOfFameButton = ActionButtons
            .toggle(ButtonIcon.TROPHY);

    private final static String KEY_LABOR = "labor";
    private final LaborPanel labor;
    private final ToggleButton laborButton = ActionButtons
            .toggle(ButtonIcon.LABOR);

    private final static String KEY_RELIGION = "religion";
    private final ReligionPanel religion;
    private final ToggleButton religionButton = ActionButtons
            .toggle(ButtonIcon.RELIGION);

    private final static String KEY_COLONIES = "colonies";
    private final ColonyAdvisorPanel colonies;
    private final ToggleButton coloniesButton = ActionButtons
            .toggle(ButtonIcon.COLONIES);

    private final static String KEY_FLEET = "fleet";
    private final FleetPanel fleet;
    private final ToggleButton fleetButton = ActionButtons
            .toggle(ButtonIcon.SHIP);

    private final static String KEY_GOLD = "gold";
    private final static String KEY_MILITARY = "military";
    private final static String KEY_WEALTH = "wealth";
    private final static String KEY_SCORE = "score";

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private final StackPane mainPanel = new StackPane();

    private final VBox leftPanel = new VBox();

    /** The tabs scroll when the window is too short for all of them. */
    private final ScrollPane sidebar = new ScrollPane(leftPanel);

    private final Label labelIndexTitle = new Label();

    private final Label reportTitle = new Label();

    private final Label historySummary = new Label();

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ToggleButton tb1 = ActionButtons.toggle(ButtonIcon.GOLD);
    private final ToggleButton tb2 = ActionButtons.toggle(ButtonIcon.MILITARY);
    private final ToggleButton tb3 = ActionButtons.toggle(ButtonIcon.CARGO);
    private final ToggleButton tb4 = ActionButtons
            .toggle(ButtonIcon.STATISTICS);

    private final Pane chartPanel = new StackPane();

    @Inject
    StatisticsPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController,
            final ViewUtil viewUtil, final HallOfFameDao hallOfFameDao,
            final LocalizationHelper localization,
            final LaborNavigator laborNavigator,
            final ColonyAdvisorNavigator colonyAdvisorNavigator,
            final FleetNavigator fleetNavigator) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);

        relations = new EuropeanRelationsPanel(gameModelController, viewUtil,
                i18n);
        relationsButton.setId("relationsTab");
        relationsButton.getStyleClass().add("relations-tab");
        relationsButton.setToggleGroup(toggleGroup);
        relationsButton.setUserData(KEY_RELATIONS);

        trade = new EuropeanTradePanel(gameModelController, i18n);
        tradeButton.setId("tradeTab");
        tradeButton.getStyleClass().add("relations-tab");
        tradeButton.setToggleGroup(toggleGroup);
        tradeButton.setUserData(KEY_TRADE);

        baseScore = new BaseScorePanel(gameModelController, viewUtil, i18n,
                hallOfFameDao);
        baseScoreButton.setId("baseScoreTab");
        baseScoreButton.getStyleClass().add("relations-tab");
        baseScoreButton.setToggleGroup(toggleGroup);
        baseScoreButton.setUserData(KEY_BASE_SCORE);

        hallOfFame = new HallOfFamePanel(hallOfFameDao, i18n, localization);
        hallOfFameButton.setId("hallOfFameTab");
        hallOfFameButton.getStyleClass().add("relations-tab");
        hallOfFameButton.setToggleGroup(toggleGroup);
        hallOfFameButton.setUserData(KEY_HALL_OF_FAME);

        labor = new LaborPanel(gameModelController, i18n, laborNavigator);
        laborButton.setId("laborTab");
        laborButton.getStyleClass().add("relations-tab");
        laborButton.setToggleGroup(toggleGroup);
        laborButton.setUserData(KEY_LABOR);

        religion = new ReligionPanel(gameModelController, i18n);
        religionButton.setId("religionTab");
        religionButton.getStyleClass().add("relations-tab");
        religionButton.setToggleGroup(toggleGroup);
        religionButton.setUserData(KEY_RELIGION);

        colonies = new ColonyAdvisorPanel(gameModelController, i18n,
                colonyAdvisorNavigator);
        coloniesButton.setId("coloniesTab");
        coloniesButton.getStyleClass().add("relations-tab");
        coloniesButton.setToggleGroup(toggleGroup);
        coloniesButton.setUserData(KEY_COLONIES);

        fleet = new FleetPanel(gameModelController, i18n, fleetNavigator);
        fleetButton.setId("fleetTab");
        fleetButton.getStyleClass().add("relations-tab");
        fleetButton.setToggleGroup(toggleGroup);
        fleetButton.setUserData(KEY_FLEET);

        toggleGroup.selectedToggleProperty()
                .addListener(this::onToggleGroupWasChanged);

        tb1.setId("goldTab");
        tb1.setToggleGroup(toggleGroup);
        tb1.setUserData(KEY_GOLD);

        tb2.setId("militaryTab");
        tb2.setToggleGroup(toggleGroup);
        tb2.setUserData(KEY_MILITARY);

        tb3.setId("wealthTab");
        tb3.setToggleGroup(toggleGroup);
        tb3.setUserData(KEY_WEALTH);

        tb4.setId("scoreTab");
        tb4.setToggleGroup(toggleGroup);
        tb4.setUserData(KEY_SCORE);

        labelIndexTitle.getStyleClass().add("indexTitle");

        leftPanel.getChildren().add(labelIndexTitle);
        leftPanel.getChildren().add(tb1);
        leftPanel.getChildren().add(tb2);
        leftPanel.getChildren().add(tb3);
        leftPanel.getChildren().add(tb4);
        leftPanel.getChildren().add(relationsButton);
        leftPanel.getChildren().add(tradeButton);
        leftPanel.getChildren().add(baseScoreButton);
        leftPanel.getChildren().add(hallOfFameButton);
        leftPanel.getChildren().add(laborButton);
        leftPanel.getChildren().add(religionButton);
        leftPanel.getChildren().add(coloniesButton);
        leftPanel.getChildren().add(fleetButton);
        // The position moves the whole sidebar, so the scroll bar stays at its
        // edge instead of being moved out of the viewport with the tabs.
        sidebar.getStyleClass().add("atlas-sidebar");
        sidebar.setMinHeight(0);
        sidebar.setPrefViewportHeight(520);
        leftPanel.getStyleClass().add("atlas-index");
        sidebar.setId("statsSidebar");
        sidebar.setFitToWidth(true);
        sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        for (final ToggleButton tab : List.of(tb1, tb2, tb3, tb4,
                relationsButton, tradeButton, baseScoreButton, hallOfFameButton,
                laborButton, religionButton, coloniesButton, fleetButton)) {
            tab.getStyleClass().add("atlas-tab");
            tab.setMaxWidth(Double.MAX_VALUE);
            tab.focusedProperty()
                    .addListener((observable, wasFocused, isFocused) -> {
                        if (isFocused) {
                            showInSidebar(tab);
                        }
                    });
        }

        reportTitle.setId("atlasReportTitle");
        reportTitle.getStyleClass().add("atlas-report-title");
        reportTitle.setWrapText(true);
        reportTitle.setMaxWidth(Double.MAX_VALUE);
        historySummary.setId("atlasHistorySummary");
        historySummary.getStyleClass().add("atlas-history-summary");
        historySummary.setWrapText(true);
        chartPanel.setId("atlasReportContent");
        chartPanel.setMinWidth(0);
        final VBox rightPanel = new VBox(16, reportTitle, historySummary,
                chartPanel);
        rightPanel.getStyleClass().add("atlas-detail");
        rightPanel.setMinWidth(0);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        VBox.setVgrow(chartPanel, Priority.ALWAYS);

        final HBox statsPanel = new HBox(sidebar, rightPanel);
        statsPanel.getStyleClass().add("statsPanel");

        mainPanel.getChildren().addAll(statsPanel);
    }

    @SuppressWarnings("unused")
    private void onToggleGroupWasChanged(
            final ObservableValue<? extends Toggle> object,
            final Toggle oldValue, final Toggle newValue) {
        if (newValue == null) {
            toggleGroup.selectToggle(oldValue);
            return;
        }
        historySummary.setVisible(false);
        historySummary.setManaged(false);
        if (KEY_RELATIONS.equals(newValue.getUserData())) {
            setTitle(Stats.titleRelations);
            relations.showReport();
            chartPanel.getChildren().setAll(relations);
            return;
        }
        if (KEY_TRADE.equals(newValue.getUserData())) {
            setTitle(Stats.titleTrade);
            trade.showReport();
            chartPanel.getChildren().setAll(trade);
            return;
        }
        if (KEY_BASE_SCORE.equals(newValue.getUserData())) {
            setTitle(Stats.titleBaseScore);
            baseScore.showReport();
            chartPanel.getChildren().setAll(baseScore);
            return;
        }
        if (KEY_HALL_OF_FAME.equals(newValue.getUserData())) {
            reportTitle.setText(i18n.get(HallOfFameText.title));
            hallOfFame.showReport(gameModelController.isGameModelReady()
                    ? gameModelController.getModel().getHallOfFameEntry()
                    : Optional.empty());
            chartPanel.getChildren().setAll(hallOfFame);
            return;
        }
        if (KEY_LABOR.equals(newValue.getUserData())) {
            setTitle(Stats.titleLabor);
            labor.showReport();
            chartPanel.getChildren().setAll(labor);
            return;
        }
        if (KEY_RELIGION.equals(newValue.getUserData())) {
            setTitle(Stats.titleReligion);
            religion.showReport();
            chartPanel.getChildren().setAll(religion);
            return;
        }
        if (KEY_COLONIES.equals(newValue.getUserData())) {
            setTitle(Stats.titleColonies);
            colonies.showReport();
            chartPanel.getChildren().setAll(colonies);
            return;
        }
        if (KEY_FLEET.equals(newValue.getUserData())) {
            setTitle(Stats.titleFleet);
            fleet.showReport();
            chartPanel.getChildren().setAll(fleet);
            return;
        }
        final List<TurnPlayerStatistics> stats = gameModelController.getModel()
                .getStatistics()
                .getStatsForPlayer(gameModelController.getHumanPlayer());
        final Calendar calendar = gameModelController.getModel().getCalendar();
        chartPanel.getChildren().clear();
        if (KEY_GOLD.equals(newValue.getUserData())) {
            setTitle(Stats.titleGold);
            chartPanel.getChildren().add(createChartGold(stats, calendar));
        } else if (KEY_MILITARY.equals(newValue.getUserData())) {
            setTitle(Stats.titleMilitary);
            chartPanel.getChildren()
                    .add(createChartMilitaryPower(stats, calendar));
        } else if (KEY_WEALTH.equals(newValue.getUserData())) {
            setTitle(Stats.titleWealth);
            chartPanel.getChildren().add(createChartWealth(stats, calendar));
        } else if (KEY_SCORE.equals(newValue.getUserData())) {
            setTitle(Stats.titleScore);
            chartPanel.getChildren().add(createChartScore(stats, calendar));
        } else {
            throw new MicroColException(
                    "Unknown user data: " + newValue.getUserData());
        }
    }

    private void setTitle(final Stats key) {
        reportTitle.setText(i18n.get(key));
    }

    /**
     * Scrolls the sidebar just enough to show a focused tab, so keyboard
     * navigation never moves the focus onto a tab out of view.
     */
    private void showInSidebar(final Node tab) {
        final double viewport = sidebar.getViewportBounds().getHeight();
        final double range = leftPanel.getHeight() - viewport;
        if (range <= 0) {
            return;
        }
        final Bounds bounds = tab.getBoundsInParent();
        final double top = sidebar.getVvalue() * range;
        double target = top;
        if (bounds.getMinY() < top) {
            target = bounds.getMinY();
        } else if (bounds.getMaxY() > top + viewport) {
            target = bounds.getMaxY() - viewport;
        }
        sidebar.setVvalue(target / range);
    }

    private Node createChartScore(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                stats, Stats.titleScore);
        lineChart.getData().add(initSerie(stats, stat -> stat.getScore(),
                Stats.titleScore, calendar));
        return lineChart;
    }

    private Node createChartWealth(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                stats, Stats.titleWealth);
        lineChart.getData().add(initSerie(stats, stat -> stat.getEcononyScore(),
                Stats.titleWealth, calendar));

        return lineChart;
    }

    private Node createChartGold(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                stats, Stats.titleGold);
        lineChart.getData().add(initSerie(stats, stat -> stat.getGold(),
                Stats.titleGold, calendar));

        return lineChart;
    }

    private Node createChartMilitaryPower(
            final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                stats, Stats.titleMilitary);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getMilitaryScore(),
                        Stats.titleMilitary, calendar));

        return lineChart;
    }

    private final LineChart<Number, Number> makeLineChart(
            final Calendar calendar, final List<TurnPlayerStatistics> stats,
            final Stats yAxisMessageKey) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(calendar.getStartYear());
        final int lastYear = stats.stream()
                .mapToInt(stat -> calendar.getYearForTurnNo(stat.getTurnNo()))
                .max().orElse(calendar.getCurrentYear());
        xAxis.setUpperBound(Math.max(calendar.getCurrentYear(), lastYear) + 2);
        xAxis.setLabel(i18n.get(Stats.statistics_year));
        yAxis.setLabel(i18n.get(yAxisMessageKey));

        final LineChart<Number, Number> out = new LineChart<Number, Number>(
                xAxis, yAxis);
        out.setId("statisticsChart");
        out.setLegendVisible(false);
        out.setAnimated(false);
        out.setMinSize(0, 280);
        out.setPrefHeight(420);
        xAxis.setTickUnit(Math.max(1, Math
                .ceil((xAxis.getUpperBound() - xAxis.getLowerBound()) / 6)));
        xAxis.setMinorTickVisible(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(final Number value) {
                return Integer.toString(value.intValue());
            }

            @Override
            public Number fromString(final String text) {
                return Integer.valueOf(text);
            }
        });

        return out;
    }

    private XYChart.Series<Number, Number> initSerie(
            final List<TurnPlayerStatistics> stats,
            final Function<TurnPlayerStatistics, Integer> statFunction,
            final Stats messageKey, final Calendar calendar) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(i18n.get(messageKey));
        stats.forEach(stat -> series.getData()
                .add(new XYChart.Data<Number, Number>(
                        calendar.getYearForTurnNo(stat.getTurnNo()),
                        statFunction.apply(stat))));
        historySummary.setText(stats.stream()
                .max(java.util.Comparator
                        .comparingInt(TurnPlayerStatistics::getTurnNo))
                .map(stat -> i18n.get(Stats.historyLatest,
                        statFunction.apply(stat),
                        calendar.getYearForTurnNo(stat.getTurnNo())))
                .orElseGet(() -> i18n.get(Stats.historyEmpty)));
        historySummary.setVisible(true);
        historySummary.setManaged(true);
        return series;
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        labelIndexTitle.setText(i18n.get(Stats.titleIndex));
        tb1.setText(i18n.get(Stats.titleGold));
        tb2.setText(i18n.get(Stats.titleMilitary));
        tb3.setText(i18n.get(Stats.titleWealth));
        tb4.setText(i18n.get(Stats.titleScore));
        relationsButton.setText(i18n.get(Stats.titleRelations));
        tradeButton.setText(i18n.get(Stats.titleTrade));
        baseScoreButton.setText(i18n.get(Stats.titleBaseScore));
        hallOfFameButton.setText(i18n.get(HallOfFameText.tab));
        laborButton.setText(i18n.get(Stats.titleLabor));
        religionButton.setText(i18n.get(Stats.titleReligion));
        coloniesButton.setText(i18n.get(Stats.titleColonies));
        fleetButton.setText(i18n.get(Stats.titleFleet));
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void beforeShow() {
        if (toggleGroup.getSelectedToggle() == tb1) {
            onToggleGroupWasChanged(null, null, tb1);
        } else {
            toggleGroup.selectToggle(tb1);
        }
    }

    @Override
    public void beforeHide() {

    }

}
