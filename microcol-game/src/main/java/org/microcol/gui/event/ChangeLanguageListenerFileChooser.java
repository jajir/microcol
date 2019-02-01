package org.microcol.gui.event;

import javax.swing.UIManager;

import org.microcol.gui.util.FileChooser;
import org.microcol.gui.util.Listener;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class ChangeLanguageListenerFileChooser {

    private final static String PREFIX = "FileChooser.";

    @Inject
    public ChangeLanguageListenerFileChooser() {
    }

    /**
     * Java swing use in some visual components predefined string values. For
     * example it's text of cancel button at dialog for choosing file. This
     * string should be localized and change with language. Proper setting of
     * this strings is done here.
     */
    @Subscribe
    public void onEvent(final ChangeLanguageEvent event) {
        for (final FileChooser val : FileChooser.values()) {
            final String key = PREFIX + val.name();
            UIManager.put(key, event.getI18n().get(val));
        }
    }

}
