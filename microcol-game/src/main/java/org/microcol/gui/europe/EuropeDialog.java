package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.microcol.gui.AbstractDialog;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Show Europe port.
 */
public class EuropeDialog extends AbstractDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController) {
//		super(viewUtil.getParentFrame());
		Preconditions.checkNotNull(gameController);
		setTitle(text.get("europeDialog.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel("European port");
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(BORDER_BIG, BORDER_BIG, BORDER, BORDER), 0, 0));
		/**
		 * Row 1
		 */
		final PanelShips outgoingShips = new PanelShips("Ships travelling to New World");
		add(outgoingShips, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_BIG, 10, 10), 0, 0));

		/**
		 * Row 2
		 */
		final PanelShips incomingShips = new PanelShips("Ships travelling to Europe");
		add(incomingShips, new GridBagConstraints(0, 2, 1, 1, 1.0D, 1.0D, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_BIG, 10, 10), 0, 0));

		final PanelPortPier pierShips = new PanelPortPier();
		add(pierShips, new GridBagConstraints(1, 2, 2, 1, 1.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, BORDER_BIG), 0, 0));
		/**
		 * Good row - 3
		 */
		final PanelGoods goods = new PanelGoods(imageProvider);
		add(goods, new GridBagConstraints(0, 3, 3, 1, 1.0D, 0.0D, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(BORDER, BORDER_BIG, BORDER, BORDER_BIG), 0, 0));

		/**
		 * Last row 10
		 */
		final JButton recruiteButton = new JButton("Recruite");
		add(recruiteButton, new GridBagConstraints(0, 10, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_BIG, BORDER_BIG, 0), 0, 0));

		final JButton buyButton = new JButton("Buy");
		add(buyButton, new GridBagConstraints(1, 10, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, BORDER_BIG, 0), 0, 0));

		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(2, 10, 1, 1, 0.0D, 0.0D, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, BORDER_BIG, BORDER_BIG), 0, 0));
		viewUtil.showDialog(this);
	}

}
