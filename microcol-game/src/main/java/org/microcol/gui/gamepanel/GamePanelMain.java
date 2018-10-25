package org.microcol.gui.gamepanel;

import org.microcol.gui.RightPanel;
import org.microcol.gui.StatusBar;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.mainmenu.MainMenuView;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class GamePanelMain implements JavaFxComponent, UpdatableLanguage {

    private final VBox box;

    private final MainMenuView mainMenuView;

    private final StatusBar statusBar;

    private final RightPanel rightPanel;

    @Inject
    GamePanelMain(final MainMenuView mainMenuView, final @Named("GamePanel") StatusBar statusBar,
            final RightPanel rightPanel, final PaneCanvas paneCanvas) {
        this.mainMenuView = Preconditions.checkNotNull(mainMenuView);
        this.statusBar = Preconditions.checkNotNull(statusBar);
        this.rightPanel = Preconditions.checkNotNull(rightPanel);
        this.statusBar.setShowEventsFromSource(Source.GAME);
        
        final Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        final Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        final HBox hBox = new HBox();
        hBox.setId("mainBox");
        hBox.getChildren().add(paneCanvas.getCanvasPane());
        hBox.getChildren().add(rightPanel.getContent());

        box = new VBox();
        box.setId("mainPanel");
        box.getChildren().add(mainMenuView.getContent());
        box.getChildren().add(hBox);
        box.getChildren().add(statusBar.getContent());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        mainMenuView.updateLanguage(i18n);
        statusBar.updateLanguage(i18n);
        rightPanel.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return box;
    }

}
