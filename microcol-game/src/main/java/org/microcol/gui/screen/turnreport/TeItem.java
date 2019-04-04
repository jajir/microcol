package org.microcol.gui.screen.turnreport;

/**
 * All front-end TeItems should extends from this class. TE is abbreviation from
 * turn event.
 */
public interface TeItem {

    /**
     * return localized turn event message.
     *
     * @return return final localized mesasge.
     */
    String getMessage();

}
