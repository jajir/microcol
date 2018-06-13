package org.microcol.gui.statistics;

import java.util.List;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.TurnPlayerStatistics;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatisticsDialog extends AbstractMessageWindow implements StatisticsDialogCallback {

    private final GameModelController gameModelController;

    private final Text text;

    private final VBox chartPanel;

    @Inject
    StatisticsDialog(final ViewUtil viewUtil, final Text text,
            final GameModelController gameModelController) {
        super(viewUtil);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        getDialog().setTitle(text.get("statistics.title"));

        final Label labelCaption = new Label(text.get("statistics.caption"));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(text.get("dialog.ok"));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        chartPanel = new VBox();

        mainPanel.getChildren().addAll(labelCaption, chartPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Override
    public void repaint() {
        chartPanel.getChildren().clear();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel(text.get("statistics.year"));
        yAxis.setLabel(text.get("statistics.gold"));

        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.getData().add(initGold(gameModelController.getModel().getStatistics()
                .getStatsForPlayer(gameModelController.getCurrentPlayer())));

        chartPanel.getChildren().add(lineChart);
    }

    private XYChart.Series<Number, Number> initGold(final List<TurnPlayerStatistics> stats) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(text.get("statistics.gold"));
        stats.forEach(stat -> series.getData()
                .add(new XYChart.Data<Number, Number>(stat.getTurnNo(), stat.getGold())));
        return series;

    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        getDialog().close();
    }

    public void show() {
        repaint();
        getDialog().showAndWait();
    }

}
