package org.microcol.gui.event;

import org.microcol.gui.AboutDialog;
import org.microcol.gui.mainmenu.AboutGameEvent;
import org.microcol.gui.mainmenu.AboutGameEventController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Provide about game event listener. Listener open about game dialog.
 */
public final class AboutGameListenerImpl implements Listener<AboutGameEvent> {

    private final AboutDialog aboutDialog;

    @Inject
    public AboutGameListenerImpl(final AboutGameEventController gameEventController,
            final AboutDialog aboutDialog) {
        gameEventController.addListener(this);
        this.aboutDialog = Preconditions.checkNotNull(aboutDialog);
    }

    @Override
    public void onEvent(final AboutGameEvent event) {
        aboutDialog.showAndWait();
    }

}
