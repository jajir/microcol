package org.microcol;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

import javafx.stage.Stage;

/**
 * Module allows to inject additional properties to guice context.
 */
final class ExternalModule extends AbstractModule {

    private final Stage primaryStage;

    /**
     * Default constructor called from guice.
     *
     * @param primaryStage
     *            required primary stage
     */
    ExternalModule(final Stage primaryStage) {
        this.primaryStage = Preconditions.checkNotNull(primaryStage, "Primary stage is null.");
    }

    @Override
    protected void configure() {
        bind(Stage.class).toInstance(primaryStage);
    }

}
