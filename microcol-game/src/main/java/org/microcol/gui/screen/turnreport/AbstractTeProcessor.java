package org.microcol.gui.screen.turnreport;

import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class AbstractTeProcessor {

    private final I18n i18n;

    @Inject
    AbstractTeProcessor(final I18n i18n) {
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    public I18n getI18n() {
        return i18n;
    }

}
