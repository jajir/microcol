package org.microcol.gui.screen.statistics;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.Loc;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.TurnPlayerStatistics;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public final class StatisticsDialog extends AbstractMessageWindow
        implements StatisticsDialogCallback {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final TabPane chartPanel;

    @Inject
    StatisticsDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController gameModelController) {
        super(viewUtil, i18n);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        setTitle(i18n.get(Loc.statistics_title));

        final Label labelCaption = new Label(i18n.get(Loc.statistics_caption));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(i18n.get(Dialog.ok));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        chartPanel = new TabPane();

        mainPanel.getChildren().addAll(labelCaption, chartPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Override
    public void repaint() {
        chartPanel.getTabs().clear();
        final List<TurnPlayerStatistics> stats = gameModelController.getModel().getStatistics()
                .getStatsForPlayer(gameModelController.getCurrentPlayer());
        final Calendar calendar = gameModelController.getModel().getCalendar();

        Tab tab = new Tab();
        tab.setText("Gold");
        tab.setClosable(false);
        tab.setContent(createChartGold(stats, calendar));
        chartPanel.getTabs().add(tab);

        tab = new Tab();
        tab.setText("Military");
        tab.setClosable(false);
        tab.setContent(createChartMilitaryPower(stats, calendar));
        chartPanel.getTabs().add(tab);

        tab = new Tab();
        tab.setText("Wealth");
        tab.setClosable(false);
        tab.setContent(createChartWealth(stats, calendar));
        chartPanel.getTabs().add(tab);

        tab = new Tab();
        tab.setText("Score");
        tab.setClosable(false);
        tab.setContent(createChartScore(stats, calendar));
        chartPanel.getTabs().add(tab);
    }

    private Node createChartScore(final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Loc.statistics_score);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getScore(), Loc.statistics_score, calendar));

        return lineChart;
    }

    private Node createChartWealth(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Loc.statistics_wealth);
        lineChart.getData().add(
                initSerie(stats, stat -> stat.getEcononyScore(), Loc.statistics_wealth, calendar));

        return lineChart;
    }

    private Node createChartGold(final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, Loc.statistics_gold);
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getGold(), Loc.statistics_gold, calendar));

        return lineChart;
    }

    private Node createChartMilitaryPower(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                Loc.statistics_militaryPower);
        lineChart.getData().add(initSerie(stats, stat -> stat.getMilitaryScore(),
                Loc.statistics_militaryPower, calendar));

        return lineChart;
    }

    private final LineChart<Number, Number> makeLineChart(final Calendar calendar,
            final Loc yAxisMessageKey) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(calendar.getStartYear());
        xAxis.setUpperBound(calendar.getCurrentYear() + 2);
        xAxis.setLabel(i18n.get(Loc.statistics_year));
        yAxis.setLabel(i18n.get(yAxisMessageKey));

        return new LineChart<Number, Number>(xAxis, yAxis);
    }

    private XYChart.Series<Number, Number> initSerie(final List<TurnPlayerStatistics> stats,
            final Function<TurnPlayerStatistics, Integer> statFunction, final Loc messageKey,
            final Calendar calendar) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(i18n.get(messageKey));
        stats.forEach(stat -> series.getData().add(new XYChart.Data<Number, Number>(
                calendar.getYearForTurnNo(stat.getTurnNo()), statFunction.apply(stat))));
        return series;
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        close();
    }

    public void show() {
        repaint();
        showAndWait();
    }

}
