package org.microcol.gui.europe;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Allows user to choose how many goods wants to transfer.
 */
public class ChooseGoodAmount extends AbstractMessageWindow {

	private final static int MIN_VALUE = 0;

	private int actualValue;

	public ChooseGoodAmount(final ViewUtil viewUtil, final Text text, final int maximalNumberToTransfer) {
		super(viewUtil);
		getDialog().setTitle(text.get("chooseGoodAmount.caption"));
		actualValue = maximalNumberToTransfer;

		VBox root = new VBox();
		init(root);

		final Label label = new Label(text.get("chooseGoodAmount.caption"));

		final HBox boxActualValue = new HBox();
		final Label labelActualValue = new Label(text.get("chooseGoodAmount.selectedAmount"));
		final Label labelValue = new Label(String.valueOf(actualValue));
		boxActualValue.getChildren().addAll(labelActualValue, labelValue);

		Slider slider = new Slider();
		slider.setMin(MIN_VALUE);
		slider.setMax(maximalNumberToTransfer);
		slider.setValue(maximalNumberToTransfer);
		slider.setSnapToTicks(true);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(false);
		slider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(final Double value) {
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

			@Override
			public Double fromString(final String string) {
				return null;
			}
		});
		slider.valueProperty().addListener((obj, oldValue, newValue) -> {
			// TODO JJ value is send on each change, only last value is enough
			labelValue.setText(String.valueOf(newValue.intValue()));
			actualValue = newValue.intValue();
		});

		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();

		root.getChildren().addAll(label, boxActualValue, slider, buttonOk);

		getDialog().showAndWait();
	}

	public int getActualValue() {
		return actualValue;
	}

}
