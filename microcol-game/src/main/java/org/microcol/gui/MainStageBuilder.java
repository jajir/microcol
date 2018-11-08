package org.microcol.gui;

import java.awt.Rectangle;

import org.microcol.gui.mainmenu.QuitGameEvent;
import org.microcol.gui.mainscreen.MainPanelView;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class MainStageBuilder {

    public static final String STYLE_SHEET_MICROCOL = MainStageBuilder.class
            .getResource("/gui/MicroCol.css").toExternalForm();

    public static final String STYLE_SHEET_DIALOGS = MainStageBuilder.class
            .getResource("/gui/dialogs.css").toExternalForm();

    public static final String STYLE_SHEET_RIGHT_PANEL_VIEW = MainStageBuilder.class
            .getResource("/gui/rightPanelView.css").toExternalForm();

    private final EventBus eventBus;

    private final MainPanelView mainPanelView;

    private final GamePreferences gamePreferences;

    private final Text text;

    @Inject
    public MainStageBuilder(final MainPanelView mainPanelView,
            final GamePreferences gamePreferences, final EventBus eventBus, final Text text) {
        this.mainPanelView = Preconditions.checkNotNull(mainPanelView);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.text = Preconditions.checkNotNull(text);
    }

    /**
     * Pass primary stage to builder.
     * 
     * @param primaryStage
     *            required primary stage
     */
    public void buildPrimaryStage(final Stage primaryStage) {
        primaryStage.setTitle(text.get("game.title"));
        primaryStage.setOnCloseRequest(event -> {
            eventBus.post(new QuitGameEvent());
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
        scene.getStylesheets().add(STYLE_SHEET_MICROCOL);
        mainBox.getChildren().add(mainPanelView.getBox());

        primaryStage.setScene(scene);
    }

    /**
     * Compute if given rectangle is whole on player's screen.
     *
     * @param rectangle
     *            required rectangle
     * @return Return <code>true</code> when whole rectangle is on screen
     *         otherwise return <code>false</code>.
     */
    private boolean isOnScreen(final Rectangle rectangle) {
        final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        return isPointOnScreen(rectangle.getMinX(), rectangle.getMinY(), primaryScreenBounds)
                && isPointOnScreen(rectangle.getMaxX(), rectangle.getMaxY(), primaryScreenBounds);
    }

    private boolean isPointOnScreen(final double x, final double y,
            final Rectangle2D primaryScreenBounds) {
        return x >= primaryScreenBounds.getMinX() && x <= primaryScreenBounds.getMaxX()
                && y >= primaryScreenBounds.getMinY() && y <= primaryScreenBounds.getMaxY();
    }

}
