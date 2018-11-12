package org.microcol.gui.gamepanel;

import org.microcol.gui.RightPanel;
import org.microcol.gui.StatusBar;
import org.microcol.gui.buttonpanel.ButtonsPanel;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.mainmenu.MainMenuView;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@Singleton
public class GamePanelMain implements JavaFxComponent, UpdatableLanguage {

    public static final String STYLE_SHEET_GAME_PANEL = GamePanelMain.class
            .getResource("/gui/GamePanel.css").toExternalForm();

    private final VBox mainBox;

    private final MainMenuView mainMenuView;

    private final StatusBar statusBar;

    private final RightPanel rightPanel;
    
    private final StackPane mainPanel;
    
    //TODO move it to controller
    private final EventBus eventBus;

    @Inject
    GamePanelMain(final MainMenuView mainMenuView, final @Named("GamePanel") StatusBar statusBar,
            final RightPanel rightPanel, final PaneCanvas paneCanvas,
            final ButtonsPanel buttonPanel, final EventBus eventBus) {
        this.mainMenuView = Preconditions.checkNotNull(mainMenuView);
        this.statusBar = Preconditions.checkNotNull(statusBar);
        this.rightPanel = Preconditions.checkNotNull(rightPanel);
        this.statusBar.setShowEventsFromSource(Source.GAME);
        this.eventBus = Preconditions.checkNotNull(eventBus);

        final Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        final Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        final HBox hBox = new HBox();
        hBox.setId("mainBox");
        hBox.getChildren().add(paneCanvas.getCanvasPane());
        hBox.getChildren().add(rightPanel.getContent());

        mainBox = new VBox();
        mainBox.setId("mainPanel");
        //FIXME remove mainMenuView
//        mainBox.getChildren().add(mainMenuView.getContent());
        mainBox.getChildren().add(hBox);
        mainBox.getChildren().add(statusBar.getContent());
        
        mainPanel = new StackPane();
        mainPanel.getStylesheets().add(STYLE_SHEET_GAME_PANEL);
        mainPanel.getChildren().add(mainBox);
        mainPanel.getChildren().add(buttonPanel.getContent());
        mainPanel.setOnKeyPressed(this::onKeyPressed);
    }
    
    private void onKeyPressed(final KeyEvent event){
        eventBus.post(event);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        mainMenuView.updateLanguage(i18n);
        statusBar.updateLanguage(i18n);
        rightPanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
