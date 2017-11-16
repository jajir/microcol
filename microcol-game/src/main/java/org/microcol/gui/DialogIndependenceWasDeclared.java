package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

public class DialogIndependenceWasDeclared extends AbstractWarningDialog {

	@Inject
	DialogIndependenceWasDeclared(final ViewUtil viewUtil, final Text text) {
		super(viewUtil, text, "dialogIndependenceWasDeclared.caption");
	}

}
