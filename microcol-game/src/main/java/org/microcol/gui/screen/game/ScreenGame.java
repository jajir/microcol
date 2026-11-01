package org.microcol.gui.screen.game;

import java.util.function.Consumer;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.util.GameScreenHeader;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.RightPanel;
import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.screen.game.components.TradeRoutePlanningPanel;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.microcol.gui.screen.game.gamepanel.TradeRoutePlanning;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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
    private final ButtonsGamePanel buttonGamePanel;

    private final StackPane mainPanel;

    private final GameScreenHeader header = new GameScreenHeader();

    private final HBox hBox = new HBox();

    private final TradeRoutePlanning tradeRoutePlanning;

    private final TradeRoutePlanningPanel tradeRoutePlanningPanel;

    @Inject
    ScreenGame(final @Named("GamePanel") StatusBar statusBar,
            final RightPanel rightPanel, final ButtonsGamePanel buttonGamePanel,
            final GamePanelComponent gamePanelComponents, final I18n i18n,
            final TradeRoutePlanning tradeRoutePlanning,
            final TradeRoutePlanningPanel tradeRoutePlanningPanel) {
        this.statusBar = Preconditions.checkNotNull(statusBar);
        this.rightPanel = Preconditions.checkNotNull(rightPanel);
        this.buttonGamePanel = Preconditions.checkNotNull(buttonGamePanel);
        this.gamePanelComponents = Preconditions
                .checkNotNull(gamePanelComponents);
        this.tradeRoutePlanning = Preconditions
                .checkNotNull(tradeRoutePlanning);
        this.tradeRoutePlanningPanel = Preconditions
                .checkNotNull(tradeRoutePlanningPanel);
        this.statusBar.setShowEventsFromSource(Source.GAME);

        hBox.setId("mainBox");
        VBox.setVgrow(hBox, javafx.scene.layout.Priority.ALWAYS);
        hBox.setMinHeight(0);
        hBox.getChildren().add(gamePanelComponents.getContent());
        hBox.getChildren().add(rightPanel.getContent());
        // The planner switch sits in the map's upper-left corner; the planner
        // panel takes the place of the normal right panel while it is on.
        gamePanelComponents.addMapControl(tradeRoutePlanningPanel.getToggle());
        tradeRoutePlanning.addListener(this::showRightPanel);

        mainBox = new VBox();
        mainBox.setId("mainPanel");
        // Reserve the toolbar layout; its ink may overlap the map edge.
        header.setActions(buttonGamePanel.getContent());
        header.getTitle().setId("mapHeading");
        header.getTitle().setText("Mappa Mundi");
        header.getTitle().setFocusTraversable(true);
        statusBar.bindHint(header.getTitle(),
                () -> i18n.get(Buttons.mapHeadingExplanation));
        mainBox.paddingProperty()
                .bind(javafx.beans.binding.Bindings
                        .createObjectBinding(
                                () -> new javafx.geometry.Insets(
                                        header.getHeight(), 0, 0, 0),
                                header.heightProperty()));
        mainBox.getChildren().add(hBox);
        mainBox.getChildren().add(statusBar.getContent());

        mainPanel = new StackPane();
        mainPanel.getStyleClass().add("game-screen");
        mainPanel.getStylesheets().add(STYLE_SHEET_GAME_PANEL);
        mainPanel.getChildren().add(mainBox);
        mainPanel.getChildren().add(header);
        StackPane.setAlignment(header, javafx.geometry.Pos.TOP_CENTER);
    }

    private void showRightPanel() {
        final Node wanted = tradeRoutePlanning.isActive()
                ? tradeRoutePlanningPanel.getContent()
                : rightPanel.getContent();
        if (hBox.getChildren().get(1) != wanted) {
            hBox.getChildren().set(1, wanted);
            if (!tradeRoutePlanning.isActive()) {
                rightPanel.refresh();
            }
        }
    }

    /**
     * Handles unit shortcuts before focused controls can consume them. Space
     * acts on release, so holding it does not skip multiple units. The route
     * planner gives no unit orders, so Tab and Space keep their ordinary
     * keyboard meaning there.
     *
     * @param handler required unit-order handler
     */
    void setOnUnitOrder(final Consumer<KeyEvent> handler) {
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() || event.isAltDown() || event.isMetaDown()
                    || event.isShiftDown() || isNavigationControlFocused()
                    || tradeRoutePlanning.isActive())
                return;
            if (KeyCode.TAB == event.getCode()) {
                handler.accept(event);
                event.consume();
            } else if (KeyCode.SPACE == event.getCode()) {
                event.consume();
            }
        });
        mainPanel.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (KeyCode.SPACE == event.getCode() && !event.isControlDown()
                    && !event.isAltDown() && !event.isMetaDown()
                    && !event.isShiftDown() && !isNavigationControlFocused()
                    && !tradeRoutePlanning.isActive()) {
                handler.accept(event);
                event.consume();
            }
        });
    }

    private boolean isNavigationControlFocused() {
        for (Node node = mainPanel.getScene()
                .getFocusOwner(); node != null; node = node.getParent()) {
            if (node == header || "worldMinimap".equals(node.getId())
                    || node == tradeRoutePlanningPanel.getToggle()) {
                return true;
            }
        }
        return false;
    }

    /**
     * it's called when key is pressed on game screen.
     *
     * @param eventHandler required key event handler
     */
    void setOnKeyPressed(EventHandler<? super KeyEvent> eventHandler) {
        // A focused menu must not consume cancellation of a map preview.
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                eventHandler.handle(event);
            }
        });
        mainPanel.setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.ESCAPE) {
                eventHandler.handle(event);
            }
        });
    }

    /**
     * it's called when key is release on game screen.
     *
     * @param eventHandler required key event handler
     */
    void setOnKeyReleased(EventHandler<? super KeyEvent> eventHandler) {
        mainPanel.setOnKeyReleased(eventHandler);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        statusBar.updateLanguage(i18n);
        rightPanel.updateLanguage(i18n);
        buttonGamePanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        if (!tradeRoutePlanning.isActive()) {
            rightPanel.refresh();
        }
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
