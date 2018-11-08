package org.microcol.gui.event;

import org.microcol.gui.mainmenu.DeclareIndependenceEvent;
import org.microcol.gui.util.Listener;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class DeclareIndependenceListener {

    @Inject
    public DeclareIndependenceListener() {
    }

    @Subscribe
    public void onEvent(final DeclareIndependenceEvent event) {
        event.getCurrentPlayer().declareIndependence();
    }

}
