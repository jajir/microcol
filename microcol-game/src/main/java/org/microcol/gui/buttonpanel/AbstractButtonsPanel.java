package org.microcol.gui.buttonpanel;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.ScreenColony;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Simplify creating of top panel with list of buttons.
 */
public class AbstractButtonsPanel implements JavaFxComponent {

    private static final String STYLE_SHEET_BUTTONS_PANEL = ScreenColony.class
            .getResource("/gui/ButtonsPanel.css").toExternalForm();

    private final Source source;
    private final ImageProvider imageProvider;
    private final EventBus eventBus;
    private final I18n i18n;

    private final VBox mainPanel;
    private final HBox buttonPanel;

    protected AbstractButtonsPanel(final ImageProvider imageProvider, final EventBus eventBus,
            final Source source, final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.i18n = Preconditions.checkNotNull(i18n);

        buttonPanel = new HBox();
        buttonPanel.getStyleClass().add("buttonPanel");

        mainPanel = new VBox();
        mainPanel.getStyleClass().add("buttons-panel-container");
        mainPanel.getChildren().add(buttonPanel);
        mainPanel.setPickOnBounds(false);
        mainPanel.getStylesheets().add(STYLE_SHEET_BUTTONS_PANEL);
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
    }

    protected <T extends Enum<T> & MessageKeyResource> Button makeButon(final String imgName,
            final T buttonsKey) {
        final ButtonImage<T> buttonImage = new ButtonImage<T>(eventBus, source, i18n, buttonsKey,
                imageProvider.getImage(imgName));
        return buttonImage.getContent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    protected boolean isContaining(final Node node) {
        return buttonPanel.getChildren().stream().filter(n -> n.equals(node)).findFirst()
                .isPresent();
    }

    protected void remove(final Node node) {
        buttonPanel.getChildren().remove(node);
    }

    /**
     * @return the buttonPanel
     */
    public HBox getButtonPanel() {
        return buttonPanel;
    }

}
