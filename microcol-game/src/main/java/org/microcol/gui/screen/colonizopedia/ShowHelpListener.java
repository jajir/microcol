package org.microcol.gui.screen.colonizopedia;

import org.microcol.gui.event.ShowHelpEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.common.eventbus.EventBus;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;

/**
 * Opens the Colonizopedia for the map help button and the H shortcut. The
 * existing screen navigation keeps it in the main game window.
 */
@Listener
public final class ShowHelpListener {

    private final EventBus eventBus;

    @Inject
    public ShowHelpListener(final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Subscribe
    private void onShowHelp(final ShowHelpEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.HELP));
    }

}
