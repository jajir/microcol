package org.akropia.gui.screen.colony;

import org.akropia.model.Colony;

/**
 * Callback from form component back to main dialog.
 */
public interface ColonyDialogCallback {

    void close();

    Colony getColony();

}
