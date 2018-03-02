package org.microcol;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import org.microcol.gui.ApplicationController;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.MicroColModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Game should be started by this class.
 */
public final class MicroCol extends Application {

    /**
     * Main static method.
     *
     * @param args
     *            list of command line parameters.
     */
    public static void main(final String[] args) {
        launch(args);
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
            final Injector injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
                    new MicroColModule());
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

}
