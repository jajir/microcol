package org.microcol;

import org.fxmisc.cssfx.CSSFX;
import org.microcol.gui.MainStageBuilder;
import org.microcol.diagnostics.DiagnosticEvent;
import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.MicroColModule;
import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.image.ModuleImages;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.ApplicationInfo;
import org.microcol.model.campaign.CampaignModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
    public void start(final Stage primaryStage) throws Exception {
        Platform.runLater(() -> initializeMicrocol(primaryStage));
    }

    private void setDevelopmentMode() {
        final GamePreferences gamePreferences = injector
                .getInstance(GamePreferences.class);
        if (gamePreferences.isDevelopment()) {
            logger.info("Appliaction started in development mode.");
            CSSFX.start();
        } else {
            logger.info("Appliaction started in production mode.");
        }
    }

    /**
     * Initialize guice, connect guice to primary stage and show first
     * application screen.
     *
     * @param primaryStage required primary stage
     */
    private void initializeMicrocol(final Stage primaryStage) {
        try {
            injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                    new MicroColModule(), new ModuleImages(),
                    new ExternalModule(primaryStage), new CampaignModule(),
                    overridenModule);
            setDevelopmentMode();
            injector.getInstance(DiagnosticLog.class)
                    .record(DiagnosticEvent.STARTED, null);

            final MainStageBuilder mainStageBuilder = injector
                    .getInstance(MainStageBuilder.class);
            mainStageBuilder.buildPrimaryStage(primaryStage);
            final ApplicationController applicationController = injector
                    .getInstance(ApplicationController.class);
            primaryStage.setOnShowing(e -> applicationController.startMusic());
            primaryStage.show();
        } catch (Exception e) {
            recordStartupFailure(e);
            /**
             * When exception occurs during starting up it's probably fatal.
             */
            System.exit(1);
        }
    }

    /**
     * Records a startup failure without the injector, which may not exist. The
     * raw stack trace is not printed because exception messages can contain
     * paths or player text. If even the log cannot be prepared, only the
     * exception class is reported on the console.
     *
     * @param failure required startup failure
     */
    static void recordStartupFailure(final Exception failure) {
        try {
            recordStartupFailure(failure,
                    new DiagnosticLog(DiagnosticLog.defaultDirectory(),
                            new ApplicationInfo().getDiagnosticVersion()));
        } catch (RuntimeException unavailable) {
            System.err.println(
                    "MicroCol failed to start (" + failure.getClass().getName()
                            + "); the diagnostic log is unavailable.");
        }
    }

    static void recordStartupFailure(final Exception failure,
            final DiagnosticLog diagnosticLog) {
        diagnosticLog.record(DiagnosticEvent.STARTUP_FAILED, failure);
        System.err.println(
                "MicroCol diagnostic log: " + diagnosticLog.getActiveFile());
    }

    @Override
    public void stop() {
        if (injector != null) {
            injector.getInstance(
                    org.microcol.gui.screen.MainPanelPresenter.class).stop();
        }
    }

    public Injector getInjector() {
        return Preconditions.checkNotNull(injector, "Guice injector is null");
    }

}
