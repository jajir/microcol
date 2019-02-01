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
    GALLEON_name,
    COLONIST_name
    ;


    private static final String UNIT_SUFFIX_NAME = "_name";

    public static UnitTypes getUnitName(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME;
        return valueOf(key);
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
