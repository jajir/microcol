package org.microcol.gui.util;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Provide localized text strings. Strings are loaded from localized resource
 * bundles.
 * 
 */
public class Text {

	private static final Logger logger = LoggerFactory.getLogger(Text.class);

	private static final String RESOURCE_BUNDLE_NAME = "localization";

	public static Text INSTANCE;

	public static enum Language {
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

	private ResourceBundle bundle;

	@Inject
	public Text(final Locale language) {
		setLocale(language);
		INSTANCE = this;
	}

	public void setLocale(final Locale locale) {
		bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, new XMLResourceBundleControl());
		logger.debug("Language " + locale + " was set.");
	}

	public String get(final String key) {
		return bundle.getString(key);
	}

}
