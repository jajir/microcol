package org.microcol.gui.screen.game.components;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.image.ImageProvider;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class StatusBarView implements JavaFxComponent {

    public static final String STATUS_BAR_LABEL_ID = "statusBarLabel";

    private final StackPane statusBar;

    private final Label statusBarDescription;

    private final Label labelEra;

    private final Label labelGold;

    @Inject
    public StatusBarView() {
        statusBarDescription = new Label();
        statusBarDescription.setId(STATUS_BAR_LABEL_ID);
        statusBarDescription.setWrapText(true);
        statusBarDescription.setMinHeight(Region.USE_PREF_SIZE);
        final VBox pane = new VBox(statusBarDescription);
        pane.setMinWidth(0);
        pane.setId("description");
        HBox.setHgrow(pane, Priority.ALWAYS);

        labelEra = new Label();
        labelEra.setId("labelEra");
        labelEra.setMinWidth(Region.USE_PREF_SIZE);

        labelGold = new Label();
        labelGold.setId("labelGold");
        labelGold.setMinWidth(Region.USE_PREF_SIZE);
        final ImageView goldIcon = new ImageView(
                ImageProvider.getRawImage("status-gold.png"));
        goldIcon.setId("statusGoldIcon");
        goldIcon.setFitWidth(32);
        goldIcon.setFitHeight(26);
        goldIcon.setMouseTransparent(true);
        labelGold.setGraphic(goldIcon);
        labelGold.setGraphicTextGap(8);

        final HBox content = new HBox(pane, labelGold, labelEra);
        content.getStyleClass().add("game-bar-content");
        statusBar = new StackPane(content);
        statusBar.setMinHeight(Region.USE_PREF_SIZE);
        statusBar.setId("statusBar");
    }

    public Label getStatusBarDescription() {
        return statusBarDescription;
    }

    public Label getLabelEra() {
        return labelEra;
    }

    public Label getLabelGold() {
        return labelGold;
    }

    @Override
    public Region getContent() {
        return statusBar;
    }
}
