package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.event.ColonyWasCapturedEvent;

public class DialogColonyWasCaptured extends AbstractWarningDialog {
	

	public DialogColonyWasCaptured(final ViewUtil viewUtil, final Text text, final ColonyWasCapturedEvent event) {
		super(viewUtil, text, "You captred colony", "Colony " + event.getCapturedColony().getName() + " was captured. Congratulation.");
	}

}
