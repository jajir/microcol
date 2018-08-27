package org.microcol.gui.gamepanel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When selected unit is changed it scroll to newly selected unit if it's
 * necessary.
 */
public final class ScrollToSelectedUnit {

    private final GamePanelView gamePanelView;

    @Inject
    public ScrollToSelectedUnit(final TileWasSelectedController tileWasSelectedController,
            final GamePanelView gamePanelView) {
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
        tileWasSelectedController.addListener(this::onTileWasSelected);
    }

    private void onTileWasSelected(final TileWasSelectedEvent event) {
        if (ScrollToFocusedTile.smoothScroll
                .equals(event.getScrollToFocusedTile())) {
            gamePanelView.planScrollingAnimationToLocation(event.getLocation());
        } else if (ScrollToFocusedTile.skip
                .equals(event.getScrollToFocusedTile())) {
            gamePanelView.skipCenterViewAtLocation(event.getLocation());
        }
        // Enum value ScrollToFocusedTile.skip is skipped.
    }

}
