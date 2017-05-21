package org.microcol.gui;

import java.awt.Rectangle;

import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.ExitGameEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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
		if (isOnScreen(rectangle)) {
			primaryStage.setX(rectangle.getX());
			primaryStage.setY(rectangle.getY());
			primaryStage.setWidth(rectangle.getWidth());
			primaryStage.setHeight(rectangle.getHeight());
		} else {
			// use default game size
			primaryStage.setWidth(800);
			primaryStage.setHeight(600);
		}
		final VBox mainBox = new VBox();
		final Scene scene = new Scene(mainBox);
		scene.getStylesheets().add("gui/MicroCol.css");

		mainBox.getChildren().add(mainMenuView.getMenuBar());
		mainBox.getChildren().add(mainFrame.getBox());

		primaryStage.setScene(scene);
	}

	private boolean isOnScreen(final Rectangle rectangle) {
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		if (isPointOutOfScreen(rectangle.getMinX(), rectangle.getMinY(), primaryScreenBounds)) {
			return false;
		}
		if (isPointOutOfScreen(rectangle.getMaxX(), rectangle.getMaxY(), primaryScreenBounds)) {
			return false;
		}
		return true;
	}

	private boolean isPointOutOfScreen(final double x, final double y, final Rectangle2D primaryScreenBounds) {
		return x < primaryScreenBounds.getMinX() || x > primaryScreenBounds.getMaxX()
				|| y < primaryScreenBounds.getMinY() || y > primaryScreenBounds.getMaxY();
	}

}
