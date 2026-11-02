package org.akropia.gui.screen.menu;

import org.akropia.gui.screen.ScreenLifeCycle;
import org.akropia.gui.util.CenteredPage;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.TitledPage;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * In main area shows game welcome page.
 */
public final class MenuHolderPanel implements JavaFxComponent, ScreenLifeCycle {

    /**
     * Gap between the title, the sea panel and the window edges.
     */
    private static final double SEA_GAP = 16;

    private final TitledPage titledPage;

    private final CenteredPage centeredPage;

    private final GameMenuBackground background;

    private final Label seaTitle = new Label();

    private final ScrollPane seaScroll = new ScrollPane();

    private final HBox seaActions = new HBox();

    private final VBox seaPanel = new VBox(8, seaScroll, seaActions);

    private final StackPane seaPage = new StackPane(seaPanel, seaTitle);

    @Inject
    MenuHolderPanel(final GameMenuBackground background,
            final TitledPage titledPage, final CenteredPage centeredPages) {
        this.background = Preconditions.checkNotNull(background);
        this.centeredPage = Preconditions.checkNotNull(centeredPages);
        this.titledPage = Preconditions.checkNotNull(titledPage);

        centeredPages.setBackground(background);
        centeredPages.setMainPanel(titledPage);

        seaTitle.getStyleClass().add(TitledPage.MAIN_TITLE_STYLE_CLASS);
        seaTitle.translateYProperty()
                .bind(seaTitle.heightProperty().divide(-2)
                        .add(GameMenuBackground.SEA_SHIFT
                                - GameMenuBackground.SEA_HEIGHT / 2d
                                - SEA_GAP));
        seaScroll.getStyleClass().addAll("screen-scroll", "sea-scroll");
        seaScroll.setFitToWidth(true);
        seaScroll.setFitToHeight(true);
        seaScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(seaScroll, Priority.ALWAYS);
        seaActions.setAlignment(Pos.CENTER_LEFT);
        seaPanel.getStyleClass().add("sea-panel");
        seaPanel.setMinHeight(GameMenuBackground.SEA_HEIGHT);
        seaPanel.setPrefHeight(GameMenuBackground.SEA_HEIGHT);
        seaPanel.setMaxHeight(GameMenuBackground.SEA_HEIGHT);
        seaPanel.setTranslateY(GameMenuBackground.SEA_SHIFT);
        seaPanel.setMinWidth(0);
        seaPage.getStyleClass().add("sea-page");
    }

    public void setMenuPanel(final JavaFxComponent menuPanel) {
        Preconditions.checkNotNull(menuPanel);
        titledPage.setContent(menuPanel);
    }

    public void stackTitleAboveContent() {
        titledPage.stackTitleAboveContent();
    }

    public void setTitle(final String menuTitle) {
        titledPage.setTitle(menuTitle);
        seaTitle.setText(menuTitle);
    }

    /**
     * Show the content in a panel bounded by the background's sea stripe. Only
     * the content scrolls vertically; the title above the sea and the return
     * control below the content stay visible.
     *
     * @param content required panel content
     * @param back    required return control
     * @param width   preferred panel width, reduced in a narrow window
     */
    public void showInSea(final Region content, final Node back,
            final double width) {
        // The in-game host may have adopted the same content node meanwhile.
        seaScroll.setContent(null);
        seaScroll.setContent(content);
        seaActions.getChildren().setAll(back);
        seaPanel.prefWidthProperty().bind(Bindings.min(width, centeredPage
                .getContent().widthProperty().subtract(2 * SEA_GAP)));
        seaPanel.maxWidthProperty().bind(seaPanel.prefWidthProperty());
        centeredPage.setMainPanel(() -> seaPage);
    }

    @Override
    public Region getContent() {
        return centeredPage.getContent();
    }

    @Override
    public void beforeShow() {
        background.beforeShow();
    }

    @Override
    public void beforeHide() {
        background.beforeHide();
    }

}
