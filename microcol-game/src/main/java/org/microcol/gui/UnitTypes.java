package org.microcol.gui;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.UnitType;

/**
 * Define keys for terrain names.
 */
public enum UnitTypes implements MessageKeyResource {

    CARAVEL_name, CARAVEL_name_7, CARAVEL_name_7pl, MERCHANTMAN_name,
    MERCHANTMAN_name_7, MERCHANTMAN_name_7pl, PRIVATEER_name, PRIVATEER_name_7,
    PRIVATEER_name_7pl, MAN_OF_WAR_name, MAN_OF_WAR_name_7, MAN_OF_WAR_name_7pl,
    FRIGATE_name, FRIGATE_name_7, FRIGATE_name_7pl, GALLEON_name,
    GALLEON_name_7, GALLEON_name_7pl, COLONIST_name, COLONIST_name_7,
    COLONIST_name_7pl, MASTER_BLACKSMITH_name, MASTER_BLACKSMITH_name_7,
    MASTER_BLACKSMITH_name_7pl, WAGON_name, WAGON_name_7, WAGON_name_7pl;

    private static final String UNIT_SUFFIX_NAME = "_name";

    private static final String UNIT_SUFFIX_NAME_7 = "_name_7";

    private static final String UNIT_SUFFIX_NAME_7pl = "_name_7pl";

    public static UnitTypes getUnitName(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME;
        return valueOf(key);
    }

    public static UnitTypes getUnitName7(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME_7;
        return valueOf(key);
    }

    public static UnitTypes getUnitName7pl(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME_7pl;
        return valueOf(key);
    }

}
