package org.microcol.gui.screen.editor;

import java.util.Set;

import org.microcol.model.Location;

class MouseOperationField implements MouseOperation {

    @Override
    public void execute(final MouseOperationContext context, final Location location) {
        final Set<Location> fields = context.getModelPo().getMap().getFieldSet();
        if (fields.contains(location)) {
            fields.remove(location);
        } else {
            fields.add(location);
        }
        context.getModelPo().getMap().setFields(fields);
    }

}
