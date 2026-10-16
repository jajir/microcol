package org.microcol.gui.screen.game.components;

import org.microcol.gui.util.JavaFxComponent;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class StatusBarView implements JavaFxComponent {

    public static final String STATUS_BAR_LABEL_ID = "statusBarLabel";

    private final HBox statusBar;

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

        labelGold = new Label();
        labelGold.setId("labelGold");

        statusBar = new HBox();
        statusBar.setMinHeight(Region.USE_PREF_SIZE);
        statusBar.getChildren().addAll(pane, labelGold, labelEra);
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
