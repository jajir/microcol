package org.microcol.gui.screen.game.components;

import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.i18n.I18n;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

public class SbPartUnits extends SbPart {

    private final UnitType unitType;

    private final long count;

    SbPartUnits(final I18n i18n, final UnitType unitType, final long count) {
        super(i18n);
        this.unitType = Preconditions.checkNotNull(unitType);
        this.count = count;
    }

    @Override
    void writeTo(final StringBuilder buff) {
        if (count == 1) {
            final String name = getI18n().get(UnitTypes.getUnitName7(unitType));
            buff.append(name);
        } else {
            buff.append(getUnitsNumber(count));
            final String name = getI18n().get(UnitTypes.getUnitName7pl(unitType));
            buff.append(" ");
            buff.append(name);
        }
    }

    private String getUnitsNumber(final long count) {
        if (count > 5) {
            return count + ".";
        } else {
            final Loc loc = Loc.valueOf("no_" + count + "_7p");
            return getI18n().get(loc);
        }
    }

}
