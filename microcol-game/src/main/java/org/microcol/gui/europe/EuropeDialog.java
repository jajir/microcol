package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.microcol.gui.AbstractDialog;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

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
	public EuropeDialog(final ViewUtil viewUtil, final Text text, final GameController gameController) {
		super(viewUtil.getParentFrame());
		setTitle(text.get("europeDialog.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel(
				"<html>brekeke<br/>..." + gameController.getModel().getCurrentPlayer().getName() + "</html>");
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));

		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
		viewUtil.showDialog(this);
	}

}
