package org.microcol.gui.screen.colony;

import org.microcol.model.Colony;

/**
 * Callback from form component back to main dialog.
 */
public interface ColonyDialogCallback {

    void close();

    Colony getColony();

}
