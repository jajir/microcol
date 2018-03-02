package org.microcol.gui.europe;

import java.util.function.Function;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Allows user to choose how many goods wants to transfer.
 */
public class ChooseGoodAmount extends AbstractMessageWindow {

	final static int MIN_VALUE = 0;

	private int actualValue;

	private final Slider slider;

	private final Text text;

	private int maximalNumberToTransfer;

	@Inject
	public ChooseGoodAmount(final ViewUtil viewUtil, final Text text) {
		super(viewUtil);
		this.text = Preconditions.checkNotNull(text);
		getDialog().setTitle(text.get("chooseGoodAmount.caption"));

		VBox root = new VBox();
		init(root);

		final Label label = new Label(text.get("chooseGoodAmount.caption"));

		final HBox boxActualValue = new HBox();
		final Label labelActualValue = new Label(text.get("chooseGoodAmount.selectedAmount"));
		final Label labelValue = new Label(String.valueOf(actualValue));
		boxActualValue.getChildren().addAll(labelActualValue, labelValue);

		slider = new Slider();
		slider.setLabelFormatter(new MyStringConverted(this::converToString));
		slider.setMin(MIN_VALUE);
		slider.setSnapToTicks(false);
		slider.setMajorTickUnit(10);
		slider.setMinorTickCount(1);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(false);

		slider.valueProperty().addListener((obj, oldValue, newValue) -> {
			labelValue.setText(String.valueOf(newValue.intValue()));
			actualValue = newValue.intValue();
		});

		final ButtonsBar buttonsBar = new ButtonsBar(text.get("dialog.ok"));
		buttonsBar.getButtonOk().setOnAction(e -> {
			getDialog().close();
		});
		buttonsBar.getButtonOk().requestFocus();

		root.getChildren().addAll(label, boxActualValue, slider, buttonsBar);
	}

	public void showAndWait() {
		getDialog().showAndWait();
	}

	public void init(final int maximalNumberToTransfer) {
		actualValue = maximalNumberToTransfer;
		this.maximalNumberToTransfer = maximalNumberToTransfer;
		slider.setMax(maximalNumberToTransfer);
		slider.setValue(maximalNumberToTransfer);
		showAndWait();
	}

	@Override
	protected void onCancelDialog() {
		actualValue = 0;
		super.onCancelDialog();
	}

	public int getActualValue() {
		return actualValue;
	}

	private String converToString(final Double value) {
		int v = value.intValue();
		if (MIN_VALUE == value) {
			return text.get("chooseGoodAmount.zero");
		} else if (maximalNumberToTransfer == value) {
			return text.get("chooseGoodAmount.everything");
		} else if (v % 10 == 0) {
			return String.valueOf(v);
		}
		return null;
	}

	private class MyStringConverted extends StringConverter<Double> {

		private final Function<Double, String> convertorToString;

		MyStringConverted(final Function<Double, String> convertorToString) {
			this.convertorToString = Preconditions.checkNotNull(convertorToString);
		}

		@Override
		public String toString(final Double value) {
			return convertorToString.apply(value);
		}

		@Override
		public Double fromString(final String string) {
			return null;
		}
	}

}
