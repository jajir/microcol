package org.microcol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;

class GuiceGuavaCompatibilityTest {

    @Singleton
    static class Listener {
        final EventBus eventBus;
        String received;

        @Inject
        Listener(EventBus eventBus) {
            this.eventBus = eventBus;
            eventBus.register(this);
        }

        @Subscribe
        public void receive(String message) {
            received = message;
        }
    }

    @Test
    void productionInjectorAndEventBusWorkTogether() {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(EventBus.class).in(Singleton.class);
                        bind(Listener.class);
                    }
                });
        final Listener listener = injector.getInstance(Listener.class);
        assertSame(listener, injector.getInstance(Listener.class));
        assertSame(listener.eventBus, injector.getInstance(EventBus.class));
        listener.eventBus.post("dependency compatibility");
        assertEquals("dependency compatibility", listener.received);
    }
}
