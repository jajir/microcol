package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.screen.colony.ShowBuildingQueueEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Listener
@Singleton
public class ShowQueueDialogListener {

    private final QueueDialog queueDialog;

    @Inject
    ShowQueueDialogListener(final QueueDialog queueDialog) {
        this.queueDialog = Preconditions.checkNotNull(queueDialog);
    }

    @Subscribe
    private void on(@SuppressWarnings("unused") final ShowBuildingQueueEvent event) {
        queueDialog.showColony();
    }

}
