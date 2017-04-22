package org.microcol.gui.colony;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.microcol.gui.AbstractDialog;
import org.microcol.gui.europe.PanelGoods;
import org.microcol.gui.europe.PanelPortPier;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Show Europe port.
 */
public class ColonyDialog extends AbstractDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public ColonyDialog(final ViewUtil viewUtil, final Text text, final GameController gameController) {
		super(viewUtil.getParentFrame());
		Preconditions.checkNotNull(gameController);
		setTitle(text.get("europeDialog.caption"));
		setLayout(new GridBagLayout());

		/**
		 * Row 1
		 */
		final JLabel label = new JLabel("Colony: First nice colony");
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(BORDER_BIG, BORDER_BIG, BORDER, BORDER), 0, 0));
		/**
		 * Row 1
		 */
		final PanelColonyLayout colonyLayout = new PanelColonyLayout();
		add(colonyLayout, new GridBagConstraints(0, 1, 2, 1, 1.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, BORDER_BIG), 0, 0));
		
		final PanelColonyStructures colonyStructures = new PanelColonyStructures();
		add(colonyStructures, new GridBagConstraints(1, 1, 2, 1, 1.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, BORDER_BIG), 0, 0));

		/**
		 * Row 2
		 */
		final PanelPortPier pierShips = new PanelPortPier();
		add(pierShips, new GridBagConstraints(1, 2, 2, 1, 1.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, BORDER_BIG), 0, 0));
		/**
		 * Good row - 3
		 */
		final PanelGoods goods = new PanelGoods();
		add(goods, new GridBagConstraints(0, 3, 3, 1, 1.0D, 0.0D, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(BORDER, BORDER_BIG, BORDER, BORDER_BIG), 0, 0));

		/**
		 * Last row 10
		 */
		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(2, 10, 1, 1, 0.0D, 0.0D, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, BORDER_BIG, BORDER_BIG), 0, 0));
		viewUtil.showDialog(this);
	}

}
