package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;

/**
 * Class accept list of functions. When apply is called than all functions are
 * called and when first return not-null result this result is returned back.
 * <p>
 * When input is not processed than {@link IllegalArgumentException} is thrown.
 * </p>
 */
public class ChainOfCommandStrategyBi<T, U, R> implements BiFunction<T, U, R> {

    private final List<BiFunction<T, U, R>> filters;

    public ChainOfCommandStrategyBi() {
        filters = new ArrayList<>();
    }

    public ChainOfCommandStrategyBi(final List<BiFunction<T, U, R>> filters) {
        this.filters = new ArrayList<>(Preconditions.checkNotNull(filters));
    }

    /**
     * Go through all filters first non-null return value is returned otherwise
     * empty is returned.
     *
     * @param t
     *            required filter input
     * @return optional result of empty
     */
    @Override
    public R apply(final T t, final U u) {
        Preconditions.checkNotNull(t);
        for (final BiFunction<T, U, R> filter : filters) {
            R result = filter.apply(t, u);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unable to process chain of command input (%s)", t));
    }

}
