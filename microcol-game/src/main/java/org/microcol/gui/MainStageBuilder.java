package org.microcol.gui;

import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.ExitGameEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainStageBuilder {

	private final MainMenuView mainMenuView;

	private final MainFrameView mainFrame;
	
	private final ExitGameController exitGameController;

	@Inject
	public MainStageBuilder(final MainMenuView mainMenuView, final MainFrameView mainFrame, final ExitGameController exitGameController) {
		this.mainMenuView = Preconditions.checkNotNull(mainMenuView);
		this.mainFrame = Preconditions.checkNotNull(mainFrame);
		this.exitGameController = Preconditions.checkNotNull(exitGameController);
	}

	public void buildPrimaryStage(final Stage primaryStage) {
		primaryStage.setTitle("Hello, World!");
		primaryStage.setOnCloseRequest(event -> {
			exitGameController.fireEvent(new ExitGameEvent());
		});
		final VBox mainBox = new VBox();
		final Scene scene = new Scene(mainBox, 400, 350);
		scene.setFill(Color.OLDLACE);

		mainBox.getChildren().add(mainMenuView.getMenuBar());
		mainBox.getChildren().add(mainFrame.getBox());

		primaryStage.setScene(scene);
	}

}
