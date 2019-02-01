package org.microcol.gui.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Provide localized text strings. Strings are loaded from localized resource
 * bundles.
 * 
 */
@Deprecated
public final class Text {

    private static final Logger logger = LoggerFactory.getLogger(Text.class);

    /**
     * Name of XML file with localization.
     */
    private static final String RESOURCE_BUNDLE_NAME = "localization";

    /**
     * Define keys that will be set in swing from resource bundle. Before some
     * updates please look at: <a href=
     * "http://alvinalexander.com/java/java-swing-uimanager-defaults">http://alvinalexander.com/java/java-swing-uimanager-defaults</a>
     */
    private static final List<String> SWING_LOCALIZED_MESSAGE_KEYS = Collections.unmodifiableList(
            Arrays.asList("FileChooser.cancelButtonText", "FileChooser.fileDateHeaderText",
                    "FileChooser.openButtonText", "FileChooser.saveButtonText",
                    "FileChooser.saveDialogTitleText", "FileChooser.openDialogTitleText",
                    "FileChooser.newFolderTitleText", "FileChooser.newFolderButtonText",
                    "FileChooser.filesOfTypeLabelText", "FileChooser.saveDialogFileNameLabelText"));

    /**
     * Actually loaded resource bundle.
     */
    private ResourceBundle bundle;

    public enum Language {
        en(new Locale("en", "US")), cz(new Locale("cs", "CZ"));

        private final Locale locale;

        private Language(final Locale locale) {
            this.locale = locale;
        }

        public Locale getLocale() {
            return locale;
        }

        public static Optional<Language> resolve(final Locale locale) {
            final String language = locale.getLanguage();
            for (final Language l : values()) {
                if (l.locale.getLanguage().equals(language)) {
                    return Optional.of(l);
                }
            }
            return Optional.empty();
        }

    }

    @Inject
    public Text(final Locale language) {
        setLocale(language);
    }

    public void setLocale(final Locale locale) {
        bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale,
                new XMLResourceBundleControl());
        setSwingTranslations();
        logger.debug("Language " + locale + " was set.");
    }

    /**
     * Java swing use in some visual components predefined string values. For
     * example it's text of cancel button at dialog for choosing file. This string
     * should be localized and change with language. Proper setting of this strings
     * is done here.
     */
    private void setSwingTranslations() {
        SWING_LOCALIZED_MESSAGE_KEYS.forEach(key -> UIManager.put(key, get(key)));
    }

    @Deprecated
    public String get(final String key, final Object... args) {
        if (args.length == 0) {
            return bundle.getString(key);
        } else {
            return String.format(bundle.getString(key), args);
        }
    }

}
