package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Produce mouse over tile events and holds tile which is mouse over.
 * <p>
 * Don't move functionality from this class to class
 * {@link MouseOverTileChangedController}. This class hold location which is
 * mouse over. This class doesn't handle events.
 * </p>
 */
public class MouseOverTileManager {

    private final MouseOverTileChangedController mouseOverTileChangedController;

    private Location mouseOverTile;

    /**
     * Default constructor
     * 
     * @param mouseOverTileChangedController
     *            required mouse over tile changed
     */
    @Inject
    public MouseOverTileManager(
            final MouseOverTileChangedController mouseOverTileChangedController) {
        this.mouseOverTileChangedController = Preconditions
                .checkNotNull(mouseOverTileChangedController);
        mouseOverTile = null;
    }

    public Optional<Location> getMouseOverTile() {
        return Optional.ofNullable(mouseOverTile);
    }

    public void setMouseOverTile(final Location newMouseOverTile) {
        Preconditions.checkNotNull(newMouseOverTile);
        if (mouseOverTile == null) {
            mouseOverTile = newMouseOverTile;
            mouseOverTileChangedController.fireEvent(new MouseOverTileChangedEvent(mouseOverTile));
        } else {
            if (!mouseOverTile.equals(newMouseOverTile)) {
                this.mouseOverTile = newMouseOverTile;
                mouseOverTileChangedController
                        .fireEvent(new MouseOverTileChangedEvent(mouseOverTile));
            }
        }
    }

}
