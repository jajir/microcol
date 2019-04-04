package org.microcol.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResourceBundleControlBuilder {

    private final List<AbstractResourceBundleType> resourceBundleTypes = Arrays.asList(
            new XmlResourceBundleType(), new JsonResourceBundleType(),
            new PropertiesResourceBundleType());

    private ResourceBundleFormat predefinedFormat;

    public ResourceBundleControlBuilder setPredefinedFormat(
            final ResourceBundleFormat predefinedFormat) {
        this.predefinedFormat = predefinedFormat;
        return this;
    }

    public ResourceBundleControlBuilder addResourceBundleType(
            final AbstractResourceBundleType resourceBundleType) {
        resourceBundleTypes.add(Objects.requireNonNull(resourceBundleType));
        return this;
    }

    public ResourceBundle.Control build() {
        Objects.requireNonNull(predefinedFormat, "Predefined format can't be null.");
        for (final AbstractResourceBundleType type : resourceBundleTypes) {
            if (type.getCode().equals(predefinedFormat.name())) {
                return type.getResourceBundleControl();
            }
        }
        throw new IllegalArgumentException("Invalid predefined format");
    }

}
