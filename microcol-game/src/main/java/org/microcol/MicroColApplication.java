package org.microcol;

import org.fxmisc.cssfx.CSSFX;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.MicroColModule;
import org.microcol.gui.dialog.ApplicationController;
import org.microcol.model.campaign.CampaignModule;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX application is started by this class.
 */
public final class MicroColApplication extends Application {

    private final Module overridenModule;

    private Injector injector;

    public MicroColApplication() {
        this(new ClassesForMocking());
    }

    public MicroColApplication(final Module overridenModule) {
        this.overridenModule = Preconditions.checkNotNull(overridenModule);
    }

    /**
     * Java FX application entry point.
     */
    @Override
    @SuppressWarnings("ucd")
    public void start(final Stage primaryStage) throws Exception {
        Platform.runLater(() -> initializeMicrocol(primaryStage));
    }

    /**
     * Initialize guice, connect guice to primary stage and show first
     * application screen.
     *
     * @param primaryStage
     *            required primary stage
     */
    private void initializeMicrocol(final Stage primaryStage) {
        try {
            CSSFX.start();
            injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                    new MicroColModule(), new ExternalModule(primaryStage), new CampaignModule(),
                    overridenModule);
            final MainStageBuilder mainStageBuilder = injector.getInstance(MainStageBuilder.class);
            mainStageBuilder.buildPrimaryStage(primaryStage);
            final ApplicationController applicationController = injector
                    .getInstance(ApplicationController.class);
            primaryStage.setOnShowing(e -> applicationController.startMusic());
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            /**
             * When exception occurs during starting up it's probably fatal.
             */
            System.exit(1);
        }
    }

    public Injector getInjector() {
        return Preconditions.checkNotNull(injector, "Guice injector is null");
    }

}
