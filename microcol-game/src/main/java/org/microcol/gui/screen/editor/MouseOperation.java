package org.microcol.gui.screen.editor;

import org.microcol.model.Location;

interface MouseOperation {

    void execute(MouseOperationContext context, Location location);

}
