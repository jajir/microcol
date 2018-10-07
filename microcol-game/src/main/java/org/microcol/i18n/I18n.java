package org.microcol.i18n;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final ResourceBundlesManager resourceBundlesManager;

    private Locale currentLocale;

    private final boolean verifyThatAllEnumKeysAreDefined;

    private final boolean verifyThatAllKeysInResourceBundleHaveConstant;

    public static class Builder {

        private boolean verifyThatAllEnumKeysAreDefined = false;

        private boolean verifyThatAllKeysInResourceBundleHaveConstant = false;

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

        public I18n build() {
            return new I18n(verifyThatAllEnumKeysAreDefined,
                    verifyThatAllKeysInResourceBundleHaveConstant, defaultLocale);
        }

    }

    private I18n(final boolean verifyThatAllEnumKeysAreDefined,
            final boolean verifyThatAllKeyDefinitionsHaveConstant, final Locale defaultLocale) {
        resourceBundlesManager = new ResourceBundlesManager();
        this.verifyThatAllEnumKeysAreDefined = verifyThatAllEnumKeysAreDefined;
        this.verifyThatAllKeysInResourceBundleHaveConstant = verifyThatAllKeyDefinitionsHaveConstant;
        init(defaultLocale);
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

    private void init(final Locale locale) {
        Objects.requireNonNull(locale, "Locale can't be null.");
        currentLocale = locale;
    }

    public <T extends Enum<T> & MessageKeyResource> String getRawMessage(final T messageKeyEnum) {
        Objects.requireNonNull(messageKeyEnum, "Message key enum can't be null");
        final String baseName = messageKeyEnum.getClass().getCanonicalName();
        final String messageKey = messageKeyEnum.toString();

        final Optional<ResourceBundle> oResourceBundle = resourceBundlesManager
                .getCachedBundle(baseName, currentLocale);
        if (oResourceBundle.isPresent()) {
            return oResourceBundle.get().getString(messageKey);
        } else {
            final ResourceBundle resourceBundle = resourceBundlesManager.init(baseName,
                    currentLocale, messageKeyEnum.getResourceBundleControl());
            if (verifyThatAllEnumKeysAreDefined) {
                verifyThatAllEnumKeysAreDefined(messageKeyEnum, resourceBundle);
            }
            if (verifyThatAllKeysInResourceBundleHaveConstant) {
                verifyThatAllKeysInResourceBundleHaveConstant(messageKeyEnum, resourceBundle);
            }
            return resourceBundle.getString(messageKey);
        }
    }

    public <T extends Enum<T> & MessageKeyResource> String getMessage(final T messageKeyEnum,
            final Object... arguments) {
        return MessageFormat.format(getRawMessage(messageKeyEnum), arguments);
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
