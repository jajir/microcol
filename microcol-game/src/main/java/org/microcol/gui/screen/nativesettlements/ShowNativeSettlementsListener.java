package org.microcol.gui.screen.nativesettlements;

import org.microcol.gui.event.ShowNativeSettlementsEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Opens the native settlement details for the N shortcut.
 */
@Listener
public final class ShowNativeSettlementsListener {

    private final Provider<NativeSettlementsDialog> dialog;

    @Inject
    public ShowNativeSettlementsListener(
            final Provider<NativeSettlementsDialog> dialog) {
        this.dialog = Preconditions.checkNotNull(dialog);
    }

    @Subscribe
    private void onShowNativeSettlements(
            final ShowNativeSettlementsEvent event) {
        dialog.get().showAndWait();
    }

}
