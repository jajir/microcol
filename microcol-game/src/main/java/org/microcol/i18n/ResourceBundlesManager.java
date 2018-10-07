package org.microcol.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

class ResourceBundlesManager {

    private final Map<String, ResourceBundle> resourceBundlesCache = new HashMap<>();

    Optional<ResourceBundle> getCachedBundle(final String baseName, final Locale locale) {
        return Optional.ofNullable(resourceBundlesCache.get(createCacheKey(baseName, locale)));
    }

    ResourceBundle init(final String baseName, final Locale locale,
            final ResourceBundle.Control resourceBundleControl) {
        ResourceBundle out = forceLoad(baseName, locale, resourceBundleControl);
        if (out == null) {
            throw new NullPointerException(String.format(
                    "Resource bundle for base name '%s' and locale '%s' can't be found.", baseName,
                    locale));
        }
        resourceBundlesCache.put(createCacheKey(baseName, locale), out);
        return out;
    }

    private String createCacheKey(final String baseName, final Locale locale) {
        Objects.requireNonNull(baseName, "base name can't be null");
        Objects.requireNonNull(locale, "base name can't be null");
        return baseName + "." + locale.toString();
    }

    private ResourceBundle forceLoad(final String baseName, final Locale locale,
            final ResourceBundle.Control resourceBundleControl) {
        Objects.requireNonNull(baseName, "base name can't be null");
        Objects.requireNonNull(locale, "base name can't be null");
        if (resourceBundleControl == null) {
            return ResourceBundle.getBundle(baseName, locale);
        } else {
            return ResourceBundle.getBundle(baseName, locale, resourceBundleControl);
        }
    }

}
