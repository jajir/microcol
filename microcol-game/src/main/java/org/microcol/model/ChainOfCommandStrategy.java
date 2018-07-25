package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

/**
 * Class accept list of functions. When apply is called than all functions are
 * called and when first return not-null result this result is returned back.
 * <p>
 * When input is not processed than {@link IllegalArgumentException} is thrown.
 * </p>
 * <p>
 * In many cases it's required multiple parameters. In such case is necessary to
 * create extra class T encapsulating all input parameters.
 * </p>
 *
 * @param <T>
 *            function input parameter
 * @param <R>
 *            returned object
 */
public final class ChainOfCommandStrategy<T, R> implements Function<T, R> {

    private final List<Function<T, R>> filters;

    /**
     * Constructor initialize chain of commands.
     *
     * @param filters
     *            required list of commands
     */
    public ChainOfCommandStrategy(final List<Function<T, R>> filters) {
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
    public R apply(final T t) {
        Preconditions.checkNotNull(t);
        for (final Function<T, R> filter : filters) {
            R result = filter.apply(t);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unable to process chain of command input (%s)", t));
    }

}
