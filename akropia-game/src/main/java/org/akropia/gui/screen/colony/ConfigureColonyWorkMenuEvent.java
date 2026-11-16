package org.akropia.gui.screen.colony;

import org.akropia.model.Unit;
import javafx.scene.control.ContextMenu;

public final class ConfigureColonyWorkMenuEvent {
    public final boolean includeWork;
    public final Unit worker;
    public final ContextMenu menu;

    public ConfigureColonyWorkMenuEvent(final Unit worker,
            final ContextMenu menu) {
        this(worker, menu, true);
    }

    public ConfigureColonyWorkMenuEvent(final Unit worker,
            final ContextMenu menu, final boolean includeWork) {
        this.includeWork = includeWork;
        this.worker = worker;
        this.menu = menu;
    }
}
