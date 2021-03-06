package org.microcol.i18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.spi.ResourceBundleProvider;
import java.util.stream.Collectors;

import org.microcol.gui.util.StreamReader;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Internationalization service. Class should be initialized via builder:
 * 
 * <pre>
 * I18n i18n = I18n.builder().setVerifyThatAllEnumKeysAreDefined(true)
 *         .setVerifyThatAllKeysInResourceBundleHaveConstant(true).build();
 * </pre>
 * 
 * For configuration option look at documentation below. There should be
 * 
 * <pre>
 * public enum Keys implements MessageKeyResource {
 *     greeting;
 * }
 * </pre>
 * 
 * Than it should be used in a way:
 * 
 * <pre>
 * I18n.getMessage(Key.greeting);
 * </pre>
 *
 * @author jan
 *
 */
public class I18n {

    private final static Locale cs_CZ = new Locale("cs", "CZ");

    private final ResourceBundlesManager resourceBundlesManager;

    private Locale currentLocale;

    private final boolean verifyThatAllEnumKeysAreDefined;

    private final boolean verifyThatAllKeysInResourceBundleHaveConstant;

    public static class Builder {

        private boolean verifyThatAllEnumKeysAreDefined = false;

        private boolean verifyThatAllKeysInResourceBundleHaveConstant = false;

        private StreamReader streamReader;

        private Locale defaultLocale;

        public Builder setVerifyThatAllEnumKeysAreDefined(boolean verifyThatAllEnumKeysAreDefined) {
            this.verifyThatAllEnumKeysAreDefined = verifyThatAllEnumKeysAreDefined;
            return this;
        }

        public Builder setVerifyThatAllKeysInResourceBundleHaveConstant(
                boolean verifyThatAllKeysInResourceBundleHaveConstant) {
            this.verifyThatAllKeysInResourceBundleHaveConstant = verifyThatAllKeysInResourceBundleHaveConstant;
            return this;
        }

        public Builder setDefaultLocale(final Locale defaultLocale) {
            Objects.requireNonNull(defaultLocale, "Default locale can't be null");
            this.defaultLocale = defaultLocale;
            return this;
        }

        public Builder setStreamReader(final StreamReader streamReader) {
            Objects.requireNonNull(streamReader, "Stream reader can't be null");
            this.streamReader = streamReader;
            return this;
        }

        public I18n build() {
            return new I18n(verifyThatAllEnumKeysAreDefined,
                    verifyThatAllKeysInResourceBundleHaveConstant, streamReader, defaultLocale);
        }

    }

    private I18n(final boolean verifyThatAllEnumKeysAreDefined,
            final boolean verifyThatAllKeyDefinitionsHaveConstant, final StreamReader streamReader,
            final Locale defaultLocale) {
        Preconditions.checkNotNull(streamReader,"Stream reader is null.");
        final ResourceBundleProvider impl = new ResourceBundleProviderXmlImpl(streamReader);
        resourceBundlesManager = new ResourceBundlesManager(impl);
        this.verifyThatAllEnumKeysAreDefined = verifyThatAllEnumKeysAreDefined;
        this.verifyThatAllKeysInResourceBundleHaveConstant = verifyThatAllKeyDefinitionsHaveConstant;
        init(defaultLocale);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(I18n.class).add("currentLocale", currentLocale)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setLocale(final Locale locale) {
        if (locale == null) {
            init(Locale.getDefault());
        } else {
            init(locale);
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    private void init(final Locale locale) {
        Objects.requireNonNull(locale, "Locale can't be null.");
        if (locale.equals(cs_CZ)) {
            currentLocale = locale;
        } else {
            currentLocale = Locale.ROOT;
        }
    }

    private <T extends Enum<T> & MessageKeyResource> String getRawMessage(final T messageKeyEnum) {
        Objects.requireNonNull(messageKeyEnum, "Message key enum can't be null");
        final String messageKey = messageKeyEnum.toString();
        return getRourceBundle(messageKeyEnum).getString(messageKey);
    }

    private <T extends Enum<T> & MessageKeyResource> ResourceBundle getRourceBundle(
            final T messageKeyResource) {
        final String baseName = messageKeyResource.getClass().getCanonicalName();
        return resourceBundlesManager.getCachedBundle(baseName, currentLocale)
                .orElseGet(() -> initResourceBundle(messageKeyResource));
    }

    private <T extends Enum<T> & MessageKeyResource> ResourceBundle initResourceBundle(
            final T messageKeyResource) {
        final String baseName = messageKeyResource.getClass().getCanonicalName();
        final ResourceBundle resourceBundle = resourceBundlesManager.init(baseName, currentLocale);
        if (verifyThatAllEnumKeysAreDefined) {
            verifyThatAllEnumKeysAreDefined(messageKeyResource, resourceBundle);
        }
        if (verifyThatAllKeysInResourceBundleHaveConstant) {
            verifyThatAllKeysInResourceBundleHaveConstant(messageKeyResource, resourceBundle);
        }
        return resourceBundle;
    }

    private <T extends Enum<T> & MessageKeyResource> String getMessage(final T messageKeyEnum,
            final Object... arguments) {
        return String.format(getRawMessage(messageKeyEnum), arguments);
    }

    public <T extends Enum<T> & MessageKeyResource> String get(final T messageKeyEnum,
            final Object... arguments) {
        return getMessage(messageKeyEnum, arguments);
    }

    public <T extends Enum<T> & MessageKeyResource> String get(final T messageKeyEnum) {
        return getMessage(messageKeyEnum);
    }

    private <T extends Enum<T>> void verifyThatAllEnumKeysAreDefined(final T messageKeyEnum,
            final ResourceBundle resourceBundle) {
        Objects.requireNonNull(messageKeyEnum, "Message key enum can't be null");
        Objects.requireNonNull(resourceBundle, "resource bundle can't be null");
        final T[] constants = messageKeyEnum.getDeclaringClass().getEnumConstants();
        for (final T key : constants) {
            final String name = key.name();
            resourceBundle.getString(name);
        }
    }

    private <T extends Enum<T>> void verifyThatAllKeysInResourceBundleHaveConstant(
            final T messageKeyEnum, final ResourceBundle resourceBundle) {
        Objects.requireNonNull(messageKeyEnum, "Message key enum can't be null");
        Objects.requireNonNull(resourceBundle, "resource bundle can't be null");
        final T[] constants = messageKeyEnum.getDeclaringClass().getEnumConstants();
        final Set<String> enumKeys = Arrays.asList(constants).stream().map(key -> key.name())
                .collect(Collectors.toSet());
        Collections.list(resourceBundle.getKeys()).forEach(key -> {
            if (!enumKeys.contains(key)) {
                throw new IllegalStateException(String.format(
                        "message key '%s' is defined in resource bundle '%s' doesn't contain it.",
                        key, resourceBundle.toString()));
            }
        });
    }

}
