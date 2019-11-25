package org.microcol.gui.dialog;

import org.microcol.gui.screen.europe.ChooseGoods;
import org.microcol.gui.screen.market.SimpleStringConverter;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Goods;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Allows user to choose how many goods wants to transfer.
 */
public final class ChooseGoodsDialog extends AbstractMessageWindow {

    final static int MIN_VALUE = 0;

    private Goods actualValue;

    private final Slider slider;

    private final I18n i18n;

    private int maximalNumberToTransfer;

    private final Label labelActualValue;

    private final Label labelCaption;

    @Inject
    public ChooseGoodsDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = Preconditions.checkNotNull(i18n);

        VBox root = new VBox();
        init(root);

        labelCaption = new Label();

        final HBox boxActualValue = new HBox();
        labelActualValue = new Label();
        final Label labelValue = new Label(String.valueOf(actualValue));
        boxActualValue.getChildren().addAll(labelActualValue, labelValue);

        slider = new Slider();
        slider.setLabelFormatter(new SimpleStringConverter(this::converToString));
        slider.setMin(MIN_VALUE);
        slider.setSnapToTicks(false);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);

        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            labelValue.setText(String.valueOf(newValue.intValue()));
            actualValue = Goods.of(actualValue.getType(), newValue.intValue());
        });

        final ButtonBarOk buttonsBar = new ButtonBarOk(i18n);
        buttonsBar.getButtonOk().setOnAction(e -> {
            close();
        });
        buttonsBar.getButtonOk().requestFocus();

        root.getChildren().addAll(labelCaption, boxActualValue, slider, buttonsBar.getContent());
    }

    public void init(final Goods maximalGoodsToTransfer) {
        actualValue = maximalGoodsToTransfer;
        this.maximalNumberToTransfer = maximalGoodsToTransfer.getAmount();
        slider.setMax(maximalGoodsToTransfer.getAmount());
        slider.setValue(maximalGoodsToTransfer.getAmount());
        showAndWait();
    }

    @Override
    protected void onCancelDialog() {
        actualValue = Goods.of(actualValue.getType());
        super.onCancelDialog();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(ChooseGoods.caption));
        labelCaption.setText(i18n.get(ChooseGoods.caption));
        labelActualValue.setText(i18n.get(ChooseGoods.selectedAmount));
    }

    public Goods getActualValue() {
        return actualValue;
    }

    private String converToString(final Double value) {
        int v = value.intValue();
        if (MIN_VALUE == value) {
            return i18n.get(ChooseGoods.zero);
        } else if (maximalNumberToTransfer == value) {
            return i18n.get(ChooseGoods.everything);
        } else if (v % 10 == 0) {
            return String.valueOf(v);
        }
        return null;
    }

}
