package org.microcol.gui;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.UnitType;

/**
 * Define keys for terrain names.
 */
public enum UnitTypes implements MessageKeyResource {

    FRIGATE_name,
    FRIGATE_name_7,
    GALLEON_name,
    GALLEON_name_7,
    GALLEON_name_7pl,
    COLONIST_name,
    COLONIST_name_7,
    COLONIST_name_7pl
    ;


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

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
