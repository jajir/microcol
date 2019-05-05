package org.microcol.gui.screen.statistics;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.TurnPlayerStatistics;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class StatisticsPanel implements GameScreen {

    private final static String KEY_GOLD = "gold";
    private final static String KEY_MILITARY = "military";
    private final static String KEY_WEALTH = "wealth";
    private final static String KEY_SCORE = "score";

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private final HBox mainPanel = new HBox();

    private final VBox leftPanel = new VBox();

    private final ButtonBarOk buttonsBar = new ButtonBarOk();
    private final Label labelIndexTitle = new Label();

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ToggleButton tb1 = new ToggleButton();
    private final ToggleButton tb2 = new ToggleButton();
    private final ToggleButton tb3 = new ToggleButton();
    private final ToggleButton tb4 = new ToggleButton();

    private Pane chartPanel = new StackPane();

    private final StatisticsView statisticsView;

    @Inject
    StatisticsPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController, final StatisticsView statisticsView) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.statisticsView = Preconditions.checkNotNull(statisticsView);

        toggleGroup.selectedToggleProperty().addListener(this::onToggleGroupWasChanged);

        tb1.getStyleClass().clear();
        tb1.setToggleGroup(toggleGroup);
        tb1.setUserData(KEY_GOLD);

        tb2.getStyleClass().clear();
        tb2.setToggleGroup(toggleGroup);
        tb2.setUserData(KEY_MILITARY);

        tb3.getStyleClass().clear();
        tb3.setToggleGroup(toggleGroup);
        tb3.setUserData(KEY_WEALTH);

        tb4.getStyleClass().clear();
        tb4.setToggleGroup(toggleGroup);
        tb4.setUserData(KEY_SCORE);

        labelIndexTitle.getStyleClass().add("indexTitle");

        leftPanel.getStyleClass().add("left");
        leftPanel.getChildren().add(labelIndexTitle);
        leftPanel.getChildren().add(tb1);
        leftPanel.getChildren().add(tb2);
        leftPanel.getChildren().add(tb3);
        leftPanel.getChildren().add(tb4);

        buttonsBar.getButtonOk().setOnAction(this::onClose);

        final VBox rightPanel = new VBox();
        rightPanel.getStyleClass().add("right");
        rightPanel.getChildren().addAll(chartPanel, buttonsBar.getContent());

        mainPanel.getChildren().addAll(leftPanel, rightPanel);
    }

    @SuppressWarnings("unused")
    private void onToggleGroupWasChanged(final ObservableValue<? extends Toggle> object,
            final Toggle oldValue, final Toggle newValue) {
        if (newValue == null) {
            return;
        }
        final List<TurnPlayerStatistics> stats = gameModelController.getModel().getStatistics()
                .getStatsForPlayer(gameModelController.getHumanPlayer());
        final Calendar calendar = gameModelController.getModel().getCalendar();
        chartPanel.getChildren().clear();
        if (KEY_GOLD.equals(newValue.getUserData())) {
            setTitle(Stats.titleGold);
            chartPanel.getChildren().add(createChartGold(stats, calendar));
        } else if (KEY_MILITARY.equals(newValue.getUserData())) {
            setTitle(Stats.titleMilitary);
            chartPanel.getChildren().add(createChartMilitaryPower(stats, calendar));
        } else if (KEY_WEALTH.equals(newValue.getUserData())) {
            setTitle(Stats.titleWealth);
            chartPanel.getChildren().add(createChartWealth(stats, calendar));
        } else if (KEY_SCORE.equals(newValue.getUserData())) {
            setTitle(Stats.titleScore);
            chartPanel.getChildren().add(createChartScore(stats, calendar));
        } else {
            throw new MicroColException("Unknown user data: " + newValue.getUserData());
        }
    }

    private void setTitle(final Stats key) {
        statisticsView.setTitle(i18n.get(key));
    }

    private Node createChartScore(final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Stats.titleScore);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getScore(), Stats.titleScore, calendar));
        return lineChart;
    }

    private Node createChartWealth(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Stats.titleWealth);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getEcononyScore(), Stats.titleWealth, calendar));

        return lineChart;
    }

    private Node createChartGold(final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Stats.titleGold);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getGold(), Stats.titleGold, calendar));

        return lineChart;
    }

    private Node createChartMilitaryPower(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Stats.titleMilitary);
        lineChart.getData().add(
                initSerie(stats, stat -> stat.getMilitaryScore(), Stats.titleMilitary, calendar));

        return lineChart;
    }

    private final LineChart<Number, Number> makeLineChart(final Calendar calendar,
            final Stats yAxisMessageKey) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(calendar.getStartYear());
        xAxis.setUpperBound(calendar.getCurrentYear() + 2);
        xAxis.setLabel(i18n.get(Stats.statistics_year));
        yAxis.setLabel(i18n.get(yAxisMessageKey));

        final LineChart<Number, Number> out = new LineChart<Number, Number>(xAxis, yAxis);
        out.setLegendVisible(false);
        
        return out;
    }

    private XYChart.Series<Number, Number> initSerie(final List<TurnPlayerStatistics> stats,
            final Function<TurnPlayerStatistics, Integer> statFunction, final Stats messageKey,
            final Calendar calendar) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(i18n.get(messageKey));
        stats.forEach(stat -> series.getData().add(new XYChart.Data<Number, Number>(
                calendar.getYearForTurnNo(stat.getTurnNo()), statFunction.apply(stat))));
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
        buttonsBar.setButtonText(i18n.get(Stats.buttonCancel));
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void beforeShow() {
        toggleGroup.selectToggle(tb1);
    }

    @Override
    public void beforeHide() {

    }

}
