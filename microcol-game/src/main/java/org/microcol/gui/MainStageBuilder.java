package org.microcol.gui;

import java.awt.Rectangle;

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

	private final GamePreferences gamePreferences;

	@Inject
	public MainStageBuilder(final MainMenuView mainMenuView, final MainFrameView mainFrame,
			final ExitGameController exitGameController, final GamePreferences gamePreferences) {
		this.mainMenuView = Preconditions.checkNotNull(mainMenuView);
		this.mainFrame = Preconditions.checkNotNull(mainFrame);
		this.exitGameController = Preconditions.checkNotNull(exitGameController);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
	}

	public void buildPrimaryStage(final Stage primaryStage) {
		primaryStage.setTitle("Hello, World!");
		primaryStage.setOnCloseRequest(event -> {
			exitGameController.fireEvent(new ExitGameEvent());
		});
		// TODO JJ use javafx property object to bind them primary stage
		// TODO JJ use maximalize / minimalize window
		primaryStage.xProperty().addListener((object, oldValue, newValue) -> {
			final Rectangle rectangle = gamePreferences.getMainFramePosition();
			rectangle.x = newValue.intValue();
			gamePreferences.setMainFramePosition(rectangle);
		});
		primaryStage.yProperty().addListener((object, oldValue, newValue) -> {
			final Rectangle rectangle = gamePreferences.getMainFramePosition();
			rectangle.y = newValue.intValue();
			gamePreferences.setMainFramePosition(rectangle);
		});
		primaryStage.widthProperty().addListener((object, oldValue, newValue) -> {
			final Rectangle rectangle = gamePreferences.getMainFramePosition();
			rectangle.width = newValue.intValue();
			gamePreferences.setMainFramePosition(rectangle);
		});
		primaryStage.heightProperty().addListener((object, oldValue, newValue) -> {
			final Rectangle rectangle = gamePreferences.getMainFramePosition();
			rectangle.height = newValue.intValue();
			gamePreferences.setMainFramePosition(rectangle);
		});
		final Rectangle rectangle = gamePreferences.getMainFramePosition();
		primaryStage.setX(rectangle.getX());
		primaryStage.setY(rectangle.getY());
		primaryStage.setWidth(rectangle.getWidth());
		primaryStage.setHeight(rectangle.getHeight());
		final VBox mainBox = new VBox();
		final Scene scene = new Scene(mainBox, 400, 350);
		scene.setFill(Color.OLDLACE);

		mainBox.getChildren().add(mainMenuView.getMenuBar());
		mainBox.getChildren().add(mainFrame.getBox());

		primaryStage.setScene(scene);
	}

}
