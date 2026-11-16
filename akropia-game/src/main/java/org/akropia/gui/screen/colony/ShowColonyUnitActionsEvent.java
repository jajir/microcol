package org.akropia.gui.screen.colony;

import org.akropia.model.Colony;
import org.akropia.model.Unit;
import javafx.scene.Node;

/**
 * Requests the shared unit commands, with production limited to this colony.
 */
public final class ShowColonyUnitActionsEvent {
    public final java.util.List<javafx.scene.control.MenuItem> extraItems;
    public final Node owner;
    public final double x;
    public final double y;
    public final Unit worker;
    public final Colony colony;

    public ShowColonyUnitActionsEvent(final Node owner, final double x,
            final double y, final Unit worker, final Colony colony) {
        this(owner, x, y, worker, colony, java.util.List.of());
    }

    public ShowColonyUnitActionsEvent(final Node owner, final double x,
            final double y, final Unit worker, final Colony colony,
            final java.util.List<javafx.scene.control.MenuItem> extraItems) {
        this.extraItems = extraItems;
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.worker = worker;
        this.colony = colony;
    }
}
