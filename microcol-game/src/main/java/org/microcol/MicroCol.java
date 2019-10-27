package org.microcol;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.microcol.gui.preferences.GamePreferences;
import org.microcol.model.campaign.CampaignManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;

/**
 * Game should be started by this class. Here is main(args ...) method for
 * MicroCol.
 */
public final class MicroCol {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroCol.class);

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
            if (isOSX()) {
                System.setProperty("com.apple.macos.useScreenMenuBar", "true");
                // set application name for oracle JDK
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MicroCol");
                // set application name for openJDK
                System.setProperty("apple.awt.application.name", "MicroCol");
            }
            Application.launch(MicroColApplication.class, args);
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
    interface ExceptionWrapper {
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
     * Provide information if it's apple operation system.
     *
     * @return return <code>true</code> when it's apple operation system
     *         otherwise return <code>false</code>
     */
    private static boolean isOSX() {
        final String osName = System.getProperty("os.name");
        return osName.contains("OS X");
    }

}
