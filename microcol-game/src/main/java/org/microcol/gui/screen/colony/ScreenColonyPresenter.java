package org.microcol.gui.screen.colony;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class ScreenColonyPresenter implements ColonyDialogCallback {

    private final ColonyPanel colonyPanel;

    private final EventBus eventBus;

    @Inject
    public ScreenColonyPresenter(final ColonyPanel colonyPanel,
            final EventBus eventBus) {
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Override
    public void close() {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    @Override
    public Colony getColony() {
        return colonyPanel.getColony();
    }

}
