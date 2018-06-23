package org.microcol.gui.gamepanel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When selected unit is changed it scroll to newly selected unit if it's
 * necessary.
 */
public class ScrollToSelectedUnit {

    private final GamePanelView gamePanelView;

    @Inject
    public ScrollToSelectedUnit(
            final SelectedUnitWasChangedController selectedUnitWasChangedController,
            final GamePanelView gamePanelView) {
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
        selectedUnitWasChangedController.addListener(this::onSelectedUnitWasChanged);
    }

    private void onSelectedUnitWasChanged(final SelectedUnitWasChangedEvent event) {
        if (event.isNecesarryToScrool()) {
            gamePanelView
                    .planScrollingAnimationToLocation(event.getSelectedUnit().get().getLocation());
        }
    }
}
