package org.microcol.gui;

import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.VolumeChangeEvent;
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

public class PreferencesVolume {

	private final Stage dialog;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 * @param volumeChangeController
	 *            required volume change controller
	 * @param actualVolume
	 *            required actual volume value
	 */
	public PreferencesVolume(final ViewUtil viewUtil, final Text text,
			final VolumeChangeController volumeChangeController, final int actualVolume) {
		dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getParentFrame());
		dialog.setTitle(text.get("preferencesVolume.caption"));

		VBox root = new VBox();
		Scene scene = new Scene(root);
		dialog.setScene(scene);

		final Label label = new Label(text.get("preferencesVolume.caption"));

		Slider slider = new Slider();
		slider.setMin(MusicPlayer.MIN_VOLUME);
		slider.setMax(MusicPlayer.MAX_VOLUME);
		slider.setValue(actualVolume);
		slider.setSnapToTicks(true);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(false);
		slider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(final Double value) {
				if (MusicPlayer.MIN_VOLUME == value) {
					return text.get("preferencesVolume.low");
				}
				if (MusicPlayer.MAX_VOLUME == value) {
					return text.get("preferencesVolume.high");
				}
				return null;
			}

			@Override
			public Double fromString(final String string) {
				return null;
			}
		});
		slider.setBlockIncrement(10);
		slider.valueProperty().addListener((obj, oldValue, newValue) -> {
			volumeChangeController.fireEvent(new VolumeChangeEvent(newValue.intValue()));
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
