package org.microcol.gui.screen.editor;

import java.util.Set;

import org.microcol.model.Location;

class MouseOperationTree implements MouseOperation {

    @Override
    public void execute(final MouseOperationContext context, final Location location) {
        final Set<Location> trees = context.getModelPo().getMap().getTreeSet();
        if (trees.contains(location)) {
            trees.remove(location);
        } else {
            trees.add(location);
        }
        context.getModelPo().getMap().setTrees(trees);
    }

}
