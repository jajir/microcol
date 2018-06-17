package org.microcol.gui.statistics;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
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

public class StatisticsDialog extends AbstractMessageWindow implements StatisticsDialogCallback {

    private final GameModelController gameModelController;

    private final Text text;

    private final TabPane chartPanel;

    @Inject
    StatisticsDialog(final ViewUtil viewUtil, final Text text,
            final GameModelController gameModelController) {
        super(viewUtil);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        setTitle(text.get("statistics.title"));

        final Label labelCaption = new Label(text.get("statistics.caption"));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(text.get("dialog.ok"));
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

    private Node createChartScore(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, "statistics.score");
        lineChart.getData().add(
                initSerie(stats, stat -> stat.getScore(), "statistics.score", calendar));

        return lineChart;
    }

    private Node createChartWealth(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, "statistics.wealth");
        lineChart.getData().add(
                initSerie(stats, stat -> stat.getEcononyScore(), "statistics.wealth", calendar));

        return lineChart;
    }

    private Node createChartGold(final List<TurnPlayerStatistics> stats, final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar, "statistics.gold");
        lineChart.getData()
                .add(initSerie(stats, stat -> stat.getGold(), "statistics.gold", calendar));

        return lineChart;
    }

    private Node createChartMilitaryPower(final List<TurnPlayerStatistics> stats,
            final Calendar calendar) {
        final LineChart<Number, Number> lineChart = makeLineChart(calendar,
                "statistics.militaryPower");
        lineChart.getData().add(initSerie(stats, stat -> stat.getMilitaryScore(),
                "statistics.militaryPower", calendar));

        return lineChart;
    }

    private final LineChart<Number, Number> makeLineChart(final Calendar calendar,
            final String yAxisMessageKey) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(calendar.getStartYear());
        xAxis.setUpperBound(calendar.getCurrentYear() + 2);
        xAxis.setLabel(text.get("statistics.year"));
        yAxis.setLabel(text.get(yAxisMessageKey));

        return new LineChart<Number, Number>(xAxis, yAxis);
    }

    private XYChart.Series<Number, Number> initSerie(final List<TurnPlayerStatistics> stats,
            final Function<TurnPlayerStatistics, Integer> statFunction, final String messageKey,
            final Calendar calendar) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(text.get(messageKey));
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
