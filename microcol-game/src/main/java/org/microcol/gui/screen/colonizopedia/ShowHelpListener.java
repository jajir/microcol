package org.microcol.gui.screen.colonizopedia;

import org.microcol.gui.event.ShowHelpEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Opens the Colonizopedia for the map help button and the H shortcut. The
 * dialog is created on the first request, after the primary stage exists.
 */
@Listener
public final class ShowHelpListener {

    private final Provider<ColonizopediaDialog> dialog;

    @Inject
    public ShowHelpListener(final Provider<ColonizopediaDialog> dialog) {
        this.dialog = Preconditions.checkNotNull(dialog);
    }

    @Subscribe
    private void onShowHelp(final ShowHelpEvent event) {
        dialog.get().showAndWait();
    }

}
