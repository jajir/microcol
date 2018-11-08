package org.microcol.gui.mainmenu;

import org.microcol.gui.util.Listener;
import org.microcol.model.unit.UnitActionType;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Front end event.
 */
@Listener
public final class PlowFieldEventListener {

    @Inject
    PlowFieldEventListener() {
    }

    @Subscribe
    private void onPlowField(final PlowFieldEvent event) {
        event.getUnit().setAction(UnitActionType.plowField);
    }

}
