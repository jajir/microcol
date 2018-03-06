package org.microcol;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

import javafx.stage.Stage;

/**
 * Module allows to inject additional properties to guice context.
 */
public class ExternalModule extends AbstractModule {

    private final Stage primaryStage;

    ExternalModule(final Stage primaryStage) {
        this.primaryStage = Preconditions.checkNotNull(primaryStage, "Primary stage is null.");
    }

    @Override
    protected void configure() {
        bind(Stage.class).toInstance(primaryStage);
    }

}
