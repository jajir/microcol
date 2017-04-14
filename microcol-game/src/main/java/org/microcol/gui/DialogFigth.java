package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.microcol.gui.util.Text;
import org.microcol.model.Unit;

public class DialogFigth extends AbstractDialog {

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
	 */
	public DialogFigth(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper, final Unit unitAttacker, final Unit unitDefender) {
		super();
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(text.get("dialogFight.title"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel(text.get("dialogFight.title"));
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(BORDER, BORDER_BIG, BORDER_BIG, BORDER_BIG), 0, 0));

		/**
		 * Attacker
		 */
		final JLabel labelAttacker = new JLabel(text.get("dialogFight.attacker"));
		add(labelAttacker, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(BORDER, BORDER_BIG, 0, BORDER_BIG), 0, 0));
		describeUnit(0, unitAttacker, imageProvider, localizationHelper);

		/**
		 * Fight image
		 */
		final ImageIcon swords = new ImageIcon(imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS));
		add(new JLabel(swords), new GridBagConstraints(1, 1, 1, 7, 1.0D, 1.0D, GridBagConstraints.SOUTH,
				GridBagConstraints.NONE, new Insets(BORDER, BORDER, BORDER, BORDER), 0, 0));

		/**
		 * Defender
		 */
		final JLabel labelDefender = new JLabel(text.get("dialogFight.defender"));
		add(labelDefender, new GridBagConstraints(2, 1, 1, 1, 1.0D, 1.0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(BORDER, BORDER_BIG, 0, BORDER_BIG), 0, 0));
		describeUnit(2, unitDefender, imageProvider, localizationHelper);

		/**
		 * Buttons
		 */
		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(2, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(BORDER_BIG, 0, BORDER, BORDER), 0, 0));

		setResizable(false);
		pack();
		setLocation(viewUtil.centerWindow(this));
		setModal(true);
	}

	private void describeUnit(final int column, final Unit unit, final ImageProvider imageProvider,
			final LocalizationHelper localizationHelper) {
		final JLabel labelname = new JLabel(localizationHelper.getUnitName(unit.getType()));
		add(labelname, new GridBagConstraints(column, 2, 1, 1, 0.0D, 0.0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_BIG, 0, BORDER_BIG), 0, 0));

		final ImageIcon unitImage = new ImageIcon(imageProvider.getUnitImage(unit.getType()));
		add(new JLabel(unitImage), new GridBagConstraints(column, 3, 1, 1, 0.0D, 0.0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_BIG, BORDER, BORDER_BIG), 0, 0));

	}

}
