package org.akropia.gui.event;

import javax.swing.UIManager;

import org.akropia.gui.util.FileChooser;
import org.akropia.gui.util.Listener;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class ChangeLanguageListenerFileChooser {

    private final static String PREFIX = "FileChooser.";

    private final I18n i18n;

    @Inject
    public ChangeLanguageListenerFileChooser(final I18n i18n) {
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Java swing use in some visual components predefined string values. For
     * example it's text of cancel button at dialog for choosing file. This
     * string should be localized and change with language. Proper setting of
     * this strings is done here.
     *
     * @param event required event
     */
    @Subscribe
    public void onEvent(final ChangeLanguageEvent event) {
        for (final FileChooser val : FileChooser.values()) {
            final String key = PREFIX + val.name();
            UIManager.put(key, i18n.get(val));
        }
    }

}
