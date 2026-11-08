package org.akropia.gui.shortcut;

import java.util.EnumSet;
import java.util.Set;

/**
 * Where a shortcut is active. Two actions may share a combination only when
 * their contexts are never active together.
 */
public enum ShortcutContext {

    /**
     * Every window and screen.
     */
    GLOBAL(EnumSet.allOf(Area.class)),

    /**
     * Main window screens offering Load, Save, Settings or Quit, not the map
     * editor; each command checks which screen it accepts.
     */
    SESSION(EnumSet.of(Area.MENU, Area.MAP, Area.EUROPE, Area.COLONY,
            Area.OTHER)),

    /**
     * Main menu.
     */
    MAIN_MENU(EnumSet.of(Area.MENU)),

    /**
     * Main game map.
     */
    MAP(EnumSet.of(Area.MAP)),

    /**
     * Europe port screen.
     */
    EUROPE(EnumSet.of(Area.EUROPE)),

    /**
     * Colony screen.
     */
    COLONY(EnumSet.of(Area.COLONY)),

    /**
     * Developer map editor.
     */
    EDITOR(EnumSet.of(Area.EDITOR));

    private enum Area {
        MENU, MAP, EUROPE, COLONY, EDITOR, OTHER
    }

    private final Set<Area> areas;

    ShortcutContext(final Set<Area> areas) {
        this.areas = areas;
    }

    /**
     * @param other required context
     * @return <code>true</code> when both contexts can be active at once
     */
    public boolean overlaps(final ShortcutContext other) {
        for (final Area area : areas) {
            if (other.areas.contains(area)) {
                return true;
            }
        }
        return false;
    }

}
