package org.microcol.gui;

import java.awt.Rectangle;

import org.microcol.gui.event.QuitGameEvent;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.MainPanelView;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.model.MicroColException;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.geometry.Rectangle2D;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class MainStageBuilder {

    public static final String STYLE_SHEET_MICROCOL = MainStageBuilder.class
            .getResource("/gui/MicroCol.css").toExternalForm();

    public static final String STYLE_SHEET_DIALOGS = MainStageBuilder.class
            .getResource("/gui/dialogs.css").toExternalForm();

    public static final String STYLE_SHEET_RIGHT_PANEL = MainStageBuilder.class
            .getResource("/gui/rightPanel.css").toExternalForm();

    private final EventBus eventBus;

    private final MainPanelView mainPanelView;

    private final GamePreferences gamePreferences;

    private final I18n i18n;

    @Inject
    public MainStageBuilder(final MainPanelView mainPanelView,
            final GamePreferences gamePreferences, final EventBus eventBus,
            final I18n i18n) {
        this.mainPanelView = Preconditions.checkNotNull(mainPanelView);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Pass primary stage to builder.
     *
     * @param primaryStage required primary stage
     */
    public void buildPrimaryStage(final Stage primaryStage) {
        if (Platform.isSupported(ConditionalFeature.UNIFIED_WINDOW)
                && primaryStage.getStyle() != StageStyle.UNIFIED) {
            primaryStage.initStyle(StageStyle.UNIFIED);
        }
        primaryStage.setTitle(i18n.get(Loc.gameTitle));
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            eventBus.post(new QuitGameEvent());
        });
        primaryStage.xProperty().addListener((object, oldValue, newValue) -> {
            final Rectangle rectangle = gamePreferences.getMainFramePosition();
            rectangle.x = newValue.intValue();
            rememberFramePosition(rectangle);
        });
        primaryStage.yProperty().addListener((object, oldValue, newValue) -> {
            final Rectangle rectangle = gamePreferences.getMainFramePosition();
            rectangle.y = newValue.intValue();
            rememberFramePosition(rectangle);
        });
        primaryStage.widthProperty()
                .addListener((object, oldValue, newValue) -> {
                    final Rectangle rectangle = gamePreferences
                            .getMainFramePosition();
                    rectangle.width = newValue.intValue();
                    rememberFramePosition(rectangle);
                });
        primaryStage.heightProperty()
                .addListener((object, oldValue, newValue) -> {
                    final Rectangle rectangle = gamePreferences
                            .getMainFramePosition();
                    rectangle.height = newValue.intValue();
                    rememberFramePosition(rectangle);
                });
        final Rectangle rectangle = gamePreferences.getMainFramePosition();
        if (isOnScreen(rectangle)) {
            primaryStage.setX(rectangle.getX());
            primaryStage.setY(rectangle.getY());
            primaryStage.setWidth(rectangle.getWidth());
            primaryStage.setHeight(rectangle.getHeight());
        } else {
            // use default game size
            primaryStage.setWidth(GamePreferences.DEFAULT_WINDOW_WIDTH);
            primaryStage.setHeight(GamePreferences.DEFAULT_WINDOW_HEIGHT);
        }
        final VBox mainBox = new VBox();
        final Scene scene = new Scene(mainBox);
        scene.setFill(Color.web("#FFF9EC"));
        scene.getStylesheets().add(STYLE_SHEET_MICROCOL);
        mainBox.getChildren().add(mainPanelView.getContent());

        eventBus.post(new ShowScreenEvent(org.microcol.gui.screen.Screen.MENU));

        primaryStage.setScene(scene);
    }

    /**
     * Window geometry is cosmetic: when the setting storage is unavailable the
     * window keeps working and the position is stored again by a later change
     * once the storage can be written. Other failed saves are not hidden.
     *
     * @param rectangle required current window geometry
     */
    private void rememberFramePosition(final Rectangle rectangle) {
        try {
            gamePreferences.setMainFramePosition(rectangle);
        } catch (MicroColException unavailable) {
            // SettingService already reported the unavailable storage once.
        }
    }

    /**
     * Compute if given rectangle is whole on player's screen.
     *
     * @param rectangle required rectangle
     * @return Return <code>true</code> when whole rectangle is on screen
     *         otherwise return <code>false</code>.
     */
    private boolean isOnScreen(final Rectangle rectangle) {
        final Rectangle2D primaryScreenBounds = Screen.getPrimary()
                .getVisualBounds();
        return isPointOnScreen(rectangle.getMinX(), rectangle.getMinY(),
                primaryScreenBounds)
                && isPointOnScreen(rectangle.getMaxX(), rectangle.getMaxY(),
                        primaryScreenBounds);
    }

    private boolean isPointOnScreen(final double x, final double y,
            final Rectangle2D primaryScreenBounds) {
        return x >= primaryScreenBounds.getMinX()
                && x <= primaryScreenBounds.getMaxX()
                && y >= primaryScreenBounds.getMinY()
                && y <= primaryScreenBounds.getMaxY();
    }

}
