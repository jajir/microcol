package org.akropia.gui.screen.menu;

import org.akropia.gui.screen.AbstractScreenMenu;
import org.akropia.gui.screen.GameScreen;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

/**
 * In main class of main menu. Compose first button panel and background.
 */
@Singleton
public final class ScreenMenu extends AbstractScreenMenu implements GameScreen {

    private final javafx.scene.control.Label caption = new javafx.scene.control.Label();
    private final org.akropia.gui.screen.menu.demo.DemoView demo;

    private final MenuHolderPanel menuHolderPanel;

    private final ButtonsPanelView buttonsPanelView;
    private final ButtonsPanelPresenter buttonsPanelPresenter;

    @Inject
    ScreenMenu(final org.akropia.gui.screen.menu.demo.DemoView demo,
            final ButtonsPanelView buttonsPanelView,
            final MenuHolderPanel menuHolderPanel,
            final ButtonsPanelPresenter buttonsPanelPresenter) {
        this.demo = demo;
        this.buttonsPanelView = Preconditions.checkNotNull(buttonsPanelView);
        this.buttonsPanelPresenter = Preconditions
                .checkNotNull(buttonsPanelPresenter);
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        menuHolderPanel.getContent().getStylesheets()
                .add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.getContent().getStyleClass().add("welcome-screen");
        javafx.scene.layout.VBox presentation = new javafx.scene.layout.VBox(8,
                demo, caption);
        presentation.setMinWidth(0);
        caption.setWrapText(true);
        if (System.getProperty("akropia.demo.directory") != null) {
            javafx.scene.control.Button reload = org.akropia.gui.util.ActionButtons
                    .button(org.akropia.gui.image.ButtonIcon.REPLACE,
                            "Reload preview");
            reload.setOnAction(event -> demo.start());
            presentation.getChildren().add(reload);
        }
        javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(24,
                buttonsPanelView.getContent(), presentation);
        javafx.scene.layout.HBox.setHgrow(presentation,
                javafx.scene.layout.Priority.ALWAYS);
        buttonsPanelView.getContent().setMinWidth(Region.USE_PREF_SIZE);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(content.prefWidthProperty());
        content.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        content.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(1408,
                menuHolderPanel.getContent().widthProperty().subtract(32)));
        menuHolderPanel.setMenuPanel(() -> content);
    }

    @Override
    public Region getContent() {
        return menuHolderPanel.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(GameMenu.mainMenu));
        caption.setText(i18n.get(GameMenu.demoCaption));
        buttonsPanelView.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        buttonsPanelPresenter.refresh();
        demo.start();
        menuHolderPanel.beforeShow();
    }

    @Override
    public void beforeHide() {
        demo.stop();
        menuHolderPanel.beforeHide();
    }
}
