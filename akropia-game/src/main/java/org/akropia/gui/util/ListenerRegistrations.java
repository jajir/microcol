package org.akropia.gui.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Listeners added to observables that may outlive their owner, removed together
 * when the owner stops. A listener left on such an observable keeps its whole
 * owner reachable.
 */
public final class ListenerRegistrations {

    private final List<Runnable> removals = new ArrayList<>();

    /**
     * Adds the listener and remembers how to remove it.
     *
     * @param <T>        observed value type
     * @param observable required observed value
     * @param listener   required listener
     */
    public <T> void add(final ObservableValue<T> observable,
            final ChangeListener<? super T> listener) {
        Preconditions.checkNotNull(observable);
        Preconditions.checkNotNull(listener);
        observable.addListener(listener);
        removals.add(() -> observable.removeListener(listener));
    }

    /**
     * Removes every added listener. Calling it again has no effect.
     */
    public void removeAll() {
        removals.forEach(Runnable::run);
        removals.clear();
    }

}
