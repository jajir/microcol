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

import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.AnimationSpeedChangeEvent;

public class PreferencesAnimationSpeed extends JDialog {

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
	 * @param controller
	 *            required animation speed controller
	 * @param actualVolume
	 *            required actual animation speed value
	 */
	public PreferencesAnimationSpeed(final ViewUtil viewUtil, final Text text,
			final AnimationSpeedChangeController controller, final int actualVolume) {
		super();
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(text.get("preferencesAnimationSpeed.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel(text.get("preferencesAnimationSpeed.caption"));
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 0), 0, 0));

		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 12, actualVolume);
		slider.setMinorTickSpacing(10);
		slider.setSnapToTicks(false);
		slider.setPaintTicks(false);
		slider.setPaintLabels(false);
		slider.setValue(actualVolume);
		slider.addChangeListener(changeEvent -> {
			controller.fireEvent(new AnimationSpeedChangeEvent(slider.getValue()));
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
