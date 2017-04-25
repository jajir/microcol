package org.microcol;

import org.microcol.gui.ApplicationController;
import org.microcol.gui.MainFrameView;
import org.microcol.gui.MainMenuView;
import org.microcol.gui.MicroColModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MicroCol extends javafx.application.Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final Injector injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION, new MicroColModule());
		final MainMenuView mainMenuView = injector.getInstance(MainMenuView.class);

		primaryStage.setTitle("Hello, World!");
		primaryStage.show();
		Scene scene = new Scene(new VBox(), 400, 350);
		scene.setFill(Color.OLDLACE);
		((VBox) scene.getRoot()).getChildren().addAll(mainMenuView.getMenuBar());
		primaryStage.setScene(scene);

		try {
			final MainFrameView mainFrame = injector.getInstance(MainFrameView.class);
			final ApplicationController applicationController = injector.getInstance(ApplicationController.class);
			mainFrame.setVisible(true);
			applicationController.startApplication();
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
