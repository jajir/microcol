package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.microcol.gui.event.VolumeChangeController;

public class PreferencesVolume extends JDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

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
		super();
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(text.get("preferencesVolume.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel(text.get("preferencesVolume.caption"));
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 0), 0, 0));

		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 80);
		slider.setMinorTickSpacing(10);
		slider.setSnapToTicks(false);
		slider.setPaintTicks(false);
		slider.setPaintLabels(false);
		slider.setValue(actualVolume);
		slider.addChangeListener(changeEvent -> {
			volumeChangeController.fireVolumeChangedEvent(slider.getValue());
		});

		add(slider, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));

		setResizable(false);
		pack();
		setLocation(viewUtil.centerWindow(this));
		setModal(true);
	}

}
