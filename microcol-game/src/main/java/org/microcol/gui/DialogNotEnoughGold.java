package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

public class DialogNotEnoughGold extends AbstractWarningDialog {

	public DialogNotEnoughGold(final ViewUtil viewUtil, final Text text) {
		super(viewUtil, text, "dialogNotEnoughGold.caption");
	}

}
