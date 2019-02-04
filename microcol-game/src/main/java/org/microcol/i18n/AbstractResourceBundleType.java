package org.microcol.i18n;

import java.util.Objects;
import java.util.ResourceBundle;

public abstract class AbstractResourceBundleType {

    private final String code;

    public AbstractResourceBundleType(final String code) {
        this.code = Objects.requireNonNull(code);
    }

    public String getCode() {
        return code;
    }

    abstract ResourceBundle.Control getResourceBundleControl();

    @Override
    public String toString() {
        return Class.class.getSimpleName() + "{code=" + code + "}";
    }

}