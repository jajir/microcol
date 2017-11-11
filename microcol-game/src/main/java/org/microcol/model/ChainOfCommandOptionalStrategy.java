package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;

/**
 * Class accept list of functions. When apply is called than all functions are
 * called and when first return not-null result this result is returned back.
 * <p>
 * When input is not processed than Optional empty object is returned.
 * </p>
 */
public class ChainOfCommandOptionalStrategy<T, R> implements Function<T, Optional<R>> {

	private final List<Function<T, R>> filters;

	public ChainOfCommandOptionalStrategy() {
		filters = new ArrayList<>();
	}

	public ChainOfCommandOptionalStrategy(final List<Function<T, R>> filters) {
		this.filters = new ArrayList<>(Preconditions.checkNotNull(filters));
	}

	public void addFiter(final Function<T, R> filter) {
		filters.add(Preconditions.checkNotNull(filter));
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
	public Optional<R> apply(final T t) {
		Preconditions.checkNotNull(t);
		for (final Function<T, R> filter : filters) {
			R result = filter.apply(t);
			if (result != null) {
				return Optional.of(result);
			}
		}
		return Optional.empty();
	}

}
