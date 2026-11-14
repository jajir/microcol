package org.akropia.gui.screen.game;

import java.util.function.Consumer;

import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.buttonpanel.Buttons;
import org.akropia.gui.util.GameScreenHeader;
import org.akropia.gui.screen.game.components.ButtonsGamePanel;
import org.akropia.gui.screen.game.components.RightPanel;
import org.akropia.gui.screen.game.components.WorldMinimap;
import org.akropia.gui.screen.game.components.StatusBar;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.gui.screen.game.components.TradeRoutePlanningPanel;
import org.akropia.gui.screen.game.gamepanel.GamePanelComponent;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ReleaseGesture;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutInput;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.SVGPath;
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

    private final StackPane mapArea = new StackPane();
    private final VBox sidebar = new VBox(8);
    private final StackPane panelContent = new StackPane();

    private final TradeRoutePlanning tradeRoutePlanning;

    private final TradeRoutePlanningPanel tradeRoutePlanningPanel;

    @Inject
    ScreenGame(final @Named("GamePanel") StatusBar statusBar,
            final RightPanel rightPanel, final ButtonsGamePanel buttonGamePanel,
            final GamePanelComponent gamePanelComponents, final I18n i18n,
            final TradeRoutePlanning tradeRoutePlanning,
            final TradeRoutePlanningPanel tradeRoutePlanningPanel,
            final WorldMinimap minimap,
            final @Named("game") VisibleAreaService visibleArea) {
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

        mapArea.setId("mainBox");
        VBox.setVgrow(mapArea, javafx.scene.layout.Priority.ALWAYS);
        mapArea.setMinHeight(0);
        mapArea.getChildren().add(gamePanelComponents.getContent());
        final StackPane overlay = new StackPane(sidebar);
        overlay.getStyleClass().add("screen-content-limit");
        overlay.setPickOnBounds(false);
        overlay.setMinHeight(0);
        StackPane.setAlignment(sidebar, Pos.TOP_RIGHT);
        overlay.maxWidthProperty().bind(javafx.beans.binding.Bindings.min(1440,
                mapArea.widthProperty().subtract(32)));
        StackPane.setMargin(sidebar, new Insets(16, 0, 16, 0));
        sidebar.setId("floatingMapPanel");
        sidebar.setMinWidth(260);
        sidebar.setPrefWidth(260);
        sidebar.setMaxWidth(260);
        sidebar.setMaxHeight(Region.USE_PREF_SIZE);
        sidebar.setPadding(new Insets(16));
        final SVGPath outline = new SVGPath();
        outline.setContent("M2 2 Q65 0 130 2 T258 2 Q260 150 258 300 "
                + "T258 598 Q195 600 130 598 T2 598 Q0 450 2 300 T2 2 Z");
        sidebar.setShape(outline);
        sidebar.setScaleShape(true);
        sidebar.setCenterShape(true);
        // The projection's 4px safety margin is internal to the canvas.
        minimap.setTranslateX(-4);
        minimap.setTranslateY(-4);
        final StackPane minimapSlot = new StackPane(minimap);
        minimapSlot.setId("floatingMinimapSlot");
        minimapSlot.setAlignment(Pos.TOP_LEFT);
        minimapSlot.setMinHeight(180);
        minimapSlot.setPrefHeight(180);
        minimapSlot.setMaxHeight(180);
        panelContent.setMinHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(panelContent, javafx.scene.layout.Priority.ALWAYS);
        panelContent.getChildren().add(rightPanel.getContent());
        sidebar.getChildren().addAll(minimapSlot, panelContent);
        sidebar.addEventHandler(MouseEvent.ANY, MouseEvent::consume);
        sidebar.addEventHandler(ScrollEvent.ANY, ScrollEvent::consume);
        mapArea.getChildren().add(overlay);
        sidebar.boundsInParentProperty()
                .addListener((p, old, bounds) -> visibleArea.setFocusWidth(
                        (int) (overlay.getLayoutX() + bounds.getMinX())));
        overlay.layoutXProperty()
                .addListener((p, old, value) -> visibleArea
                        .setFocusWidth((int) (value.doubleValue()
                                + sidebar.getBoundsInParent().getMinX())));
        // The planner switch sits in the map's upper-left corner; the planner
        // panel takes the place of the normal right panel while it is on.
        gamePanelComponents.addMapControl(tradeRoutePlanningPanel.getToggle(),
                header.getTitle());
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
        mainBox.getChildren().add(mapArea);
        mainBox.getChildren().add(statusBar.getContent());
        mapArea.heightProperty()
                .addListener((p, old, value) -> updateWindowMinimum());
        header.heightProperty()
                .addListener((p, old, value) -> updateWindowMinimum());

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
        if (panelContent.getChildren().get(0) != wanted) {
            panelContent.getChildren().set(0, wanted);
            panelContent.applyCss();
            updateWindowMinimum();
            javafx.application.Platform.runLater(this::updateWindowMinimum);
            if (!tradeRoutePlanning.isActive()) {
                rightPanel.refresh();
            }
        }
    }

    /**
     * Handles unit shortcuts before focused controls can consume them. Skip
     * acts once on release of its accepted press, so holding it does not skip
     * multiple units. The route planner gives no unit orders, so Tab and Space
     * keep their ordinary keyboard meaning there; a focused text field keeps
     * its typing keys.
     *
     * @param shortcuts effective shortcuts
     * @param handler   required unit-order handler receiving
     *                  {@link ShortcutAction#NEXT_UNIT} or
     *                  {@link ShortcutAction#SKIP_UNIT}
     */
    void setOnUnitOrder(final KeyboardShortcuts shortcuts,
            final Consumer<ShortcutAction> handler) {
        final ReleaseGesture skip = new ReleaseGesture(shortcuts,
                ShortcutAction.SKIP_UNIT);
        // Moving focus or entering the planner forgets an accepted press.
        tradeRoutePlanning.addListener(skip::cancel);
        mainPanel.sceneProperty().addListener((property, before, after) -> {
            if (after != null) {
                after.focusOwnerProperty()
                        .addListener((owner, old, focused) -> skip.cancel());
            }
        });
        mainPanel.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (isNavigationControlFocused() || tradeRoutePlanning.isActive()
                    || ShortcutInput.isOwnedByText(
                            mainPanel.getScene().getFocusOwner(), event))
                return;
            if (shortcuts.matches(ShortcutAction.NEXT_UNIT, event)) {
                handler.accept(ShortcutAction.NEXT_UNIT);
                event.consume();
            } else if (skip.onPressed(event)) {
                event.consume();
            }
        });
        mainPanel.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (skip.onReleased(event)) {
                event.consume();
                if (!isNavigationControlFocused()
                        && !tradeRoutePlanning.isActive()) {
                    handler.accept(ShortcutAction.SKIP_UNIT);
                }
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

    private void updateWindowMinimum() {
        if (mapArea.getScene() == null || !(mapArea.getScene()
                .getWindow() instanceof javafx.stage.Stage)) {
            return;
        }
        final javafx.stage.Stage stage = (javafx.stage.Stage) mapArea.getScene()
                .getWindow();
        // Reserve the fixed minimap, a 120px scroll area and the planner
        // actions.
        final double details = Math.max(120,
                tradeRoutePlanningPanel.getContent().minHeight(228));
        final double decorations = Math.max(0,
                stage.getHeight() - mapArea.getScene().getHeight());
        final double minimum = Math
                .ceil(header.getHeight() + statusBar.getContent().getHeight()
                        + 32 + 32 + 180 + 8 + details + decorations);
        stage.setMinHeight(minimum);
        if (stage.getHeight() < minimum) {
            stage.setHeight(minimum);
        }
    }

    @Override
    public void beforeShow() {
        gamePanelComponents.beforeShow();
        javafx.application.Platform.runLater(this::updateWindowMinimum);
    }

    @Override
    public void beforeHide() {
        gamePanelComponents.beforeHide();
    }

}
