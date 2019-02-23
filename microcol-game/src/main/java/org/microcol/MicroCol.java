package org.microcol;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.MicroColModule;
import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.model.campaign.CampaignManager;
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
 * Game should be started by this class.
 */
public final class MicroCol extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroCol.class);

    private final Module overridenModule;

    private Injector injector;

    public MicroCol() {
        this(new ClassesForMocking());
    }

    public MicroCol(final Module overridenModule) {
        this.overridenModule = Preconditions.checkNotNull(overridenModule);
    }

    /**
     * Main static method.
     *
     * @param args
     *            list of command line parameters.
     */
    public static void main(final String[] args) {
        if (isClean()) {
            cleanAll();
            System.exit(0);
        } else {
            launch(args);
        }
    }

    /**
     * Inform if it's clean game start and all settings should be cleaned.
     *
     * @return Return <code>true</code> if it's clean start otherwise return
     *         false.
     */
    private static boolean isClean() {
        return Boolean.getBoolean(GamePreferences.SYSTEM_PROPERTY_CLEAN_SETTINGS);
    }

    /**
     * Functional interface that allows to execute code and just throws optional
     * exception.
     */
    @FunctionalInterface
    public interface ExceptionWrapper {
        /**
         * Allows to execute some functionality throwing exception.
         *
         * @throws BackingStoreException
         *             backing store exception
         */
        void consume() throws BackingStoreException;
    }

    /**
     * Allows to hide exception in called code and don't fail application.
     *
     * @param consumer
     *            required exception wrapper
     */
    private static void exec(final ExceptionWrapper consumer) {
        try {
            consumer.consume();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean all game setting and preferences.
     */
    private static void cleanAll() {
        LOGGER.info("Cleaning all game setting");
        exec(() -> {
            final Preferences gamePref = Preferences.userNodeForPackage(GamePreferences.class);
            gamePref.removeNode();
            gamePref.flush();
        });
        exec(() -> {
            final Preferences campaignPref = Preferences.userNodeForPackage(CampaignManager.class);
            campaignPref.removeNode();
            campaignPref.flush();
        });
    }

    /**
     * Method try to set application icon. It use apple native classes. Because
     * it should be compiled at windows platform apple specific classes are
     * access by java reflection. For this reason is also exception sunk.
     */
    private static void setAppleDockIcon() {
        try {
            final Class<?> clazz = Class.forName("com.apple.eawt.Application", false, null);
            if (clazz != null) {
                Method m = clazz.getMethod("getApplication");
                Object o = m.invoke(null);
                Method m2 = clazz.getMethod("setDockIconImage", Image.class);
                final ClassLoader cl = MicroCol.class.getClassLoader();
                final Image i = ImageIO.read(cl.getResourceAsStream("images/splash-screen.png"));
                m2.invoke(o, i);
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            /**
             * intentionally do nothing.
             */
            e.printStackTrace();
        }
    }

    /**
     * Provide information if it's apple operation system.
     *
     * @return return <code>true</code> when it's apple operation system
     *         otherwise return <code>false</code>
     */
    private static boolean isOSX() {
        final String osName = System.getProperty("os.name");
        return osName.contains("OS X");
    }

    /**
     * Java FX application entry point.
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        if (isOSX()) {
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            // set application name for oracle JDK
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MicroCol");
            // set application name for openJDK
            System.setProperty("apple.awt.application.name", "MicroCol");
            setAppleDockIcon();
        }
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
            injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                    new MicroColModule(), new ExternalModule(primaryStage), new CampaignModule(),
                    overridenModule);
            final MainStageBuilder mainStageBuilder = injector.getInstance(MainStageBuilder.class);
            mainStageBuilder.buildPrimaryStage(primaryStage);
            final ApplicationController applicationController = injector
                    .getInstance(ApplicationController.class);
            primaryStage.setOnShowing(e -> applicationController.startApplication());
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
