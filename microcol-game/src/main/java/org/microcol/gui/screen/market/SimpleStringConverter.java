package org.microcol.gui.screen.market;

import java.util.function.Function;

import com.google.common.base.Preconditions;

import javafx.util.StringConverter;

/**
 * Simple converter from double to string and back.
 */
public class SimpleStringConverter extends StringConverter<Double> {

    private final Function<Double, String> convertorToString;

    public SimpleStringConverter(final Function<Double, String> convertorToString) {
        this.convertorToString = Preconditions.checkNotNull(convertorToString);
    }

    @Override
    public String toString(final Double value) {
        return convertorToString.apply(value);
    }

    @Override
    public Double fromString(final String string) {
        return null;
    }
    
}
