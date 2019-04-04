package org.microcol.gui.event;

import org.microcol.gui.dialog.AboutDialog;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Provide about game event listener. Listener open about game dialog.
 */
@Listener
public final class AboutGameListenerImpl {

    private final AboutDialog aboutDialog;

    @Inject
    public AboutGameListenerImpl(final AboutDialog aboutDialog) {
        this.aboutDialog = Preconditions.checkNotNull(aboutDialog);
    }

    @Subscribe
    public void onAboutGame(@SuppressWarnings("unused") final AboutGameEvent event) {
        aboutDialog.showAndWait();
    }

}
