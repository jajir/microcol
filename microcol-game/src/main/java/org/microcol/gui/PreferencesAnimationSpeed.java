package org.microcol.gui;

import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.AnimationSpeedChangeEvent;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PreferencesAnimationSpeed {

	private final Stage dialog;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param text
	 *            required localization helper class
	 * @param viewUtil
	 *            required show dialog utilities
	 * @param controller
	 *            required animation speed controller
	 * @param actualVolume
	 *            required actual animation speed value
	 */
	public PreferencesAnimationSpeed(final Text text, final ViewUtil viewUtil,
			final AnimationSpeedChangeController controller, final int actualVolume) {
		dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getPrimaryStage());
		dialog.setTitle(text.get("preferencesAnimationSpeed.caption"));

		VBox root = new VBox();
		Scene scene = new Scene(root);
		dialog.setScene(scene);

		final Label label = new Label(text.get("preferencesAnimationSpeed.caption"));

		Slider slider = new Slider();
		slider.setMin(PathPlanning.ANIMATION_SPEED_MIN_VALUE);
		slider.setMax(PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1);
		slider.setValue(actualVolume);
		slider.setSnapToTicks(true);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(false);
		slider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(final Double value) {
				if (PathPlanning.ANIMATION_SPEED_MIN_VALUE == value) {
					return text.get("preferencesAnimationSpeed.slow");
				}
				if (PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1 == value) {
					return text.get("preferencesAnimationSpeed.fast");
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
			controller.fireEvent(new AnimationSpeedChangeEvent(newValue.intValue()));
		});

		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			dialog.close();
		});
		buttonOk.requestFocus();

		root.getChildren().addAll(label, slider, buttonOk);

		dialog.showAndWait();
	}

}
