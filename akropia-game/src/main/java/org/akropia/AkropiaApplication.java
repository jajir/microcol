package org.akropia;

import org.fxmisc.cssfx.CSSFX;
import org.akropia.gui.MainStageBuilder;
import org.akropia.diagnostics.DiagnosticEvent;
import org.akropia.diagnostics.DiagnosticLog;
import org.akropia.gui.AkropiaModule;
import org.akropia.gui.MusicController;
import org.akropia.gui.SuccessCuePlayer;
import org.akropia.gui.dialog.ApplicationController;
import org.akropia.gui.image.ModuleImages;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.ApplicationInfo;
import org.akropia.model.campaign.CampaignModule;
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
public final class AkropiaApplication extends Application {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Module overridenModule;

    private Injector injector;

    /**
     * Stops the development style sheet reloading started by this application.
     */
    private Runnable stopStyleSheetReloading = () -> {
    };

    public AkropiaApplication() {
        this(new ClassesForMocking());
    }

    public AkropiaApplication(final Module overridenModule) {
        this.overridenModule = Preconditions.checkNotNull(overridenModule);
    }

    /**
     * Java FX application entry point.
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        Platform.runLater(() -> initializeAkropia(primaryStage));
    }

    private void setDevelopmentMode() {
        final GamePreferences gamePreferences = injector
                .getInstance(GamePreferences.class);
        if (gamePreferences.isDevelopment()) {
            logger.info("Appliaction started in development mode.");
            // CSSFX 11 retains global window listeners even after its stopper
            // runs.
            // Automated UI tests reload whole applications, not stylesheet
            // files.
            if (!Boolean.getBoolean("akropia.test.disableCssReload")) {
                stopStyleSheetReloading = CSSFX.start();
            }
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
    private void initializeAkropia(final Stage primaryStage) {
        try {
            injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                    new AkropiaModule(), new ModuleImages(),
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
                    "Akropia failed to start (" + failure.getClass().getName()
                            + "); the diagnostic log is unavailable.");
        }
    }

    static void recordStartupFailure(final Exception failure,
            final DiagnosticLog diagnosticLog) {
        diagnosticLog.record(DiagnosticEvent.STARTUP_FAILED, failure);
        System.err.println(
                "Akropia diagnostic log: " + diagnosticLog.getActiveFile());
    }

    /**
     * Stops what this application started. The primary stage may outlive it, so
     * listeners added to that stage are removed as well.
     */
    @Override
    public void stop() {
        stopStyleSheetReloading.run();
        stopStyleSheetReloading = () -> {
        };
        if (injector != null) {
            injector.getInstance(SuccessCuePlayer.class).stop();
            injector.getInstance(MusicController.class).stop();
            injector.getInstance(
                    org.akropia.gui.screen.MainPanelPresenter.class).stop();
            injector.getInstance(MainStageBuilder.class).releasePrimaryStage();
        }
    }

    public Injector getInjector() {
        return Preconditions.checkNotNull(injector, "Guice injector is null");
    }

}
