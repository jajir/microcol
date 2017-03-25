package org.microcol.gui;

import java.util.Locale;
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

	private final static Logger logger = LoggerFactory.getLogger(Text.class);

	private final static String RESOURCE_BUNDLE_NAME = "localization";

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

	}

	private ResourceBundle bundle;

	@Inject
	public Text(final Language language) {
		setLanguage(language);
		INSTANCE = this;
	}

	public void setLanguage(final Language language) {
		bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, language.getLocale());
		logger.debug("Language " + language + " was set.");
	}

	public String get(final String key) {
		return bundle.getString(key);
	}

}
