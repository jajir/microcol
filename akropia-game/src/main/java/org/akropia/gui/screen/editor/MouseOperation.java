package org.akropia.gui.screen.editor;

import org.akropia.model.Location;

interface MouseOperation {

    void execute(MouseOperationContext context, Location location);

}
