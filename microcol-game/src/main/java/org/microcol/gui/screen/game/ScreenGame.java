package org.microcol.gui.screen.game;

import java.util.function.Consumer;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.RightPanel;
import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@Singleton
public class ScreenGame implements GameScreen {

    public static final String STYLE_SHEET_GAME_PANEL = ScreenGame.class
            .getResource("/gui/GamePanel.css").toExternalForm();

    private final VBox mainBox;

    private final GamePanelComponent gamePanelComponents;

    private final StatusBar statusBar;

    private final RightPanel rightPanel;

    private final StackPane mainPanel;

    @Inject
    ScreenGame(final @Named("GamePanel") StatusBar statusBar, final RightPanel rightPanel,
            final ButtonsGamePanel buttonGamePanel, final GamePanelComponent gamePanelComponents) {
        this.statusBar = Preconditions.checkNotNull(statusBar);
        this.rightPanel = Preconditions.checkNotNull(rightPanel);
        this.gamePanelComponents = Preconditions.checkNotNull(gamePanelComponents);
        this.statusBar.setShowEventsFromSource(Source.GAME);

        final Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        final Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        final HBox hBox = new HBox();
        hBox.setId("mainBox");
        hBox.getChildren().add(gamePanelComponents.getContent());
        hBox.getChildren().add(rightPanel.getContent());

        mainBox = new VBox();
        mainBox.setId("mainPanel");
        mainBox.getChildren().add(hBox);
        mainBox.getChildren().add(statusBar.getContent());

        mainPanel = new StackPane();
        mainPanel.getStylesheets().add(STYLE_SHEET_GAME_PANEL);
        mainPanel.getChildren().add(mainBox);
        mainPanel.getChildren().add(buttonGamePanel.getContent());
    }

    /**
     * Consumer is called when player press tabulator.
     * <p>
     * There is problem to catch pressed tabulator with usual event. It's
     * because components handle tabulator to move focus.
     * </p>
     * 
     * @param tabEventHandler
     *            required tabulator event handler
     */
    void setOnTabPressed(final Consumer<KeyEvent> tabEventHandler) {
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.TAB == event.getCode()) {
                tabEventHandler.accept(event);
            }
        });
    }

    /**
     * it's called when key is pressed on game screen.
     *
     * @param eventHandler
     *            required key event handler
     */
    void setOnKeyPressed(EventHandler<? super KeyEvent> eventHandler) {
        mainPanel.setOnKeyPressed(eventHandler);
    }

    /**
     * it's called when key is release on game screen.
     *
     * @param eventHandler
     *            required key event handler
     */
    void setOnKeyReleased(EventHandler<? super KeyEvent> eventHandler) {
        mainPanel.setOnKeyReleased(eventHandler);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        statusBar.updateLanguage(i18n);
        rightPanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        rightPanel.refresh();
        return mainPanel;
    }

    @Override
    public void beforeShow() {
        gamePanelComponents.beforeShow();
    }

    @Override
    public void beforeHide() {
        gamePanelComponents.beforeHide();
    }

}
