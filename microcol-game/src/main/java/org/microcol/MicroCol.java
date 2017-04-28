package org.microcol;

import org.microcol.gui.ApplicationController;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.MicroColModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.application.Platform;
import javafx.stage.Stage;

public class MicroCol extends javafx.application.Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		Platform.runLater(() -> {
			try {
				final Injector injector = Guice.createInjector(com.google.inject.Stage.PRODUCTION,
						new MicroColModule());
				final MainStageBuilder mainStageBuilder = injector.getInstance(MainStageBuilder.class);
				mainStageBuilder.buildPrimaryStage(primaryStage);

				final ApplicationController applicationController = injector.getInstance(ApplicationController.class);
				applicationController.startApplication();

				primaryStage.show();
			} catch (Exception e) {
				e.printStackTrace();
				/**
				 * When exception occurs during starting up it's probably fatal.
				 */
				System.exit(1);
			}
		});
	}

}
