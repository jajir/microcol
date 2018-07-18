package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import javafx.application.Platform;

/**
 * Allows to collect consumers and call them once at fire.
 *
 * @param <T>
 *            Consumed object.
 */
public final class OneTimeExecuter<T> {

    private final List<Consumer<T>> consumers = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();

    public void addCallWhenReady(final Consumer<T> consumer) {
        Preconditions.checkNotNull(consumer);
        consumers.add(consumer);
    }

    /**
     * Each registered consumer will be called in front end FX thread.
     *
     * @param t
     *            required parameter
     */
    public void fire(final T t) {
        try {
            lock.lock();
            Preconditions.checkNotNull(t);
            consumers.forEach(consumer -> Platform.runLater(() -> consumer.accept(t)));
            consumers.clear();
        } finally {
            lock.unlock();
        }
    }

}
