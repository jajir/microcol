package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When selected unit is changed it scroll to newly selected unit if it's
 * necessary.
 */
@Listener
public final class ScrollToSelectedUnit {

    private final GamePanelPainter gamePanelView;

    @Inject
    public ScrollToSelectedUnit(final GamePanelPainter gamePanelView) {
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
    }

    @Subscribe
    private void onTileWasSelected(final TileWasSelectedEvent event) {
        if (ScrollToFocusedTile.smoothScroll.equals(event.getScrollToFocusedTile())) {
            gamePanelView.planScrollingAnimationToLocation(event.getLocation());
        } else if (ScrollToFocusedTile.skip.equals(event.getScrollToFocusedTile())) {
            gamePanelView.skipCenterViewAtLocation(event.getLocation());
        }
        // Enum value ScrollToFocusedTile.skip is skipped.
    }

}
