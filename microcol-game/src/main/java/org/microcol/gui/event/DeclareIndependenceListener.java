package org.microcol.gui.event;

import org.microcol.gui.mainmenu.DeclareIndependenceController;
import org.microcol.gui.mainmenu.DeclareIndependenceEvent;

import com.google.inject.Inject;

public final class DeclareIndependenceListener implements Listener<DeclareIndependenceEvent> {

    @Inject
    public DeclareIndependenceListener(final DeclareIndependenceController controller) {
        controller.addListener(this);
    }

    @Override
    public void onEvent(final DeclareIndependenceEvent event) {
        event.getCurrentPlayer().declareIndependence();
    }

}
