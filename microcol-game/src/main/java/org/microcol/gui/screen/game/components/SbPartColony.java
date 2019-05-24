package org.microcol.gui.screen.game.components;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;

class SbPartColony extends SbPart {

    private final String colonyName;

    SbPartColony(final I18n i18n, final String colonyName) {
        super(i18n);
        this.colonyName = colonyName;
    }

    @Override
    void writeTo(final StringBuilder buff) {
        buff.append(getI18n().get(Loc.withColony));
        buff.append(" ");
        buff.append(colonyName);
    }

}
