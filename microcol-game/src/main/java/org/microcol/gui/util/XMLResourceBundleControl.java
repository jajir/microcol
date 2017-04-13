package org.microcol.gui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import com.google.common.base.Preconditions;

/**
 * Class helps to load XML resource bundle to java SE {@link ResourceBundle}.
 */
public class XMLResourceBundleControl extends ResourceBundle.Control {

	@Override
	public List<String> getFormats(final String baseName) {
		Preconditions.checkNotNull(baseName);
		return Arrays.asList("xml");
	}

	@Override
	public ResourceBundle newBundle(final String baseName, final Locale locale, final String format,
			final ClassLoader loader, final boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		Preconditions.checkNotNull(baseName);
		Preconditions.checkNotNull(locale);
		Preconditions.checkNotNull(format);
		Preconditions.checkNotNull(loader);
		if (format.equals("xml")) {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, format);
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					if (reload) {
						// disable caches if reloading
						connection.setUseCaches(false);
					}
					try (InputStream stream = connection.getInputStream()) {
						if (stream != null) {
							BufferedInputStream bis = new BufferedInputStream(stream);
							return new XMLResourceBundle(bis);
						}
					}
				}
			}
		}
		return null;
	}

	private static class XMLResourceBundle extends ResourceBundle {

		private Properties props;

		XMLResourceBundle(final InputStream stream) throws IOException {
			props = new Properties();
			props.loadFromXML(stream);
		}

		@Override
		protected Object handleGetObject(final String key) {
			if (key == null) {
				throw new NullPointerException();
			}
			return props.get(key);
		}

		@Override
		public Enumeration<String> getKeys() {
			// Not implemented
			return null;
		}
	}
}
