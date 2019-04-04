package org.microcol.gui.screen.game.components;

import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;

public abstract class SbPart {

    private final I18n i18n;

    SbPart(final I18n i18n) {
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Add this part to given string builder.
     *
     * @param buff
     *            required string builder
     */
    abstract void writeTo(final StringBuilder buff);

    protected I18n getI18n() {
        return i18n;
    }

}
