package org.microcol.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.AnimationSpeedChangeEvent;
import org.microcol.gui.util.Text;

public class PreferencesAnimationSpeed extends AbstractDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param text
	 *            required localization helper class
	 * @param controller
	 *            required animation speed controller
	 * @param actualVolume
	 *            required actual animation speed value
	 */
	public PreferencesAnimationSpeed(final Text text, final AnimationSpeedChangeController controller,
			final int actualVolume) {
		super();
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(text.get("preferencesAnimationSpeed.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel(text.get("preferencesAnimationSpeed.caption"));
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(BORDER, BORDER, BORDER_BIG, 0), 0, 0));

		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, PathPlanning.ANIMATION_SPEED_MIN_VALUE,
				PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1, actualVolume);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(new Integer(PathPlanning.ANIMATION_SPEED_MIN_VALUE),
				new JLabel(text.get("preferencesAnimationSpeed.slow")));
		labelTable.put(new Integer(PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1),
				new JLabel(text.get("preferencesAnimationSpeed.fast")));
		slider.setLabelTable(labelTable);
		slider.setMinorTickSpacing(1);
		slider.setSnapToTicks(false);
		slider.setPaintTicks(false);
		slider.setPaintLabels(true);
		slider.setValue(actualVolume);
		slider.addChangeListener(changeEvent -> {
			controller.fireEvent(new AnimationSpeedChangeEvent(slider.getValue()));
		});

		add(slider, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.BOTH, new Insets(0, BORDER, 0, BORDER), 0, 0));

		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(BORDER_BIG, 0, BORDER, BORDER), 0, 0));

		setResizable(false);
		pack();
		int width = (int) (getPreferredSize().getWidth() * 2);
		setMinimumSize(new Dimension(width, getMinimumSize().height));
		setLocationRelativeTo(null);
		setModal(true);
	}

}
