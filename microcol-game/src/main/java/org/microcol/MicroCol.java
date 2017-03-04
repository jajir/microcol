package org.microcol;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.SplashScreen;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.microcol.gui.AppleMenuListener;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.MainFrameView;
import org.microcol.gui.MicroColModule;
import org.microcol.gui.model.GameController;

import com.apple.mrj.MRJApplicationUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * MicroCol's main class.
 */
public class MicroCol {

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
	 * It's in separate method, because it works just when it's called as soon
	 * as possible after application start. When it's called later it's not
	 * applied.
	 */
	private static void initAppleProperties() {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MicroCol");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.application.name", "MicroCol");
		System.setProperty("apple.awt.brushMetalLook", "true");
	}

	private static void setSplashScreen() {
		final SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash == null) {
			System.err.println("Splash screen is null!");
		} else {
			Graphics2D g = splash.createGraphics();
			if (g == null) {
				System.out.println("unable to instantiate graphics for splash screen");
				return;
			}
		}
	}

	private static void setSystemNativeUi() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method try to set application icon. It use apple native classes. Because
	 * it should be compiled at windows platform apple specific classes are
	 * access by java reflection. For this reason is also exception sunk.
	 */
	private static void setAppleDockIcon() {
		try {
			Class<?> clazz = Class.forName("com.apple.eawt.Application", false, null);
			if (clazz != null) {
				Method m = clazz.getMethod("getApplication");
				Object o = m.invoke(null);
				Method m2 = clazz.getMethod("setDockIconImage", Image.class);
				m2.invoke(o, ImageProvider.getRawImage("images/splash-screen.png"));
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			/**
			 * intentionally do nothing.
			 */
			e.printStackTrace();
		}
	}

	/**
	 * Creates and displays the form.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(final String args[]) {

		setSplashScreen();

		if (isOSX()) {
			initAppleProperties();
			setAppleDockIcon();
		}

		final Injector injector = Guice.createInjector(Stage.PRODUCTION, new MicroColModule());

		if (isOSX()) {
			AppleMenuListener macController = injector.getInstance(AppleMenuListener.class);
			MRJApplicationUtils.registerQuitHandler(macController);
			MRJApplicationUtils.registerAboutHandler(macController);
			setSystemNativeUi();
		}

		SwingUtilities.invokeLater(() -> {
			final MainFrameView mainFrame = injector.getInstance(MainFrameView.class);
			final GameController gameController = injector.getInstance(GameController.class);
			mainFrame.setVisible(true);
			gameController.newGame();
		});
	}
}
