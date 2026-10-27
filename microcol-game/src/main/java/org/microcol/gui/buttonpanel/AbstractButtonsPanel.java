package org.microcol.gui.buttonpanel;

import org.microcol.gui.image.ButtonIcon;
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
import javafx.scene.layout.FlowPane;
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
    private final FlowPane buttonPanel;

    protected AbstractButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final Source source, final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.i18n = Preconditions.checkNotNull(i18n);

        // Wrap controls before shrinking their labels into unreadable text.
        buttonPanel = new FlowPane();
        buttonPanel.getStyleClass().add("buttonPanel");

        mainPanel = new VBox();
        mainPanel.getStyleClass().add("buttons-panel-container");
        mainPanel.getChildren().add(buttonPanel);
        mainPanel.setPickOnBounds(false);
        mainPanel.getStylesheets().add(STYLE_SHEET_BUTTONS_PANEL);
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
    }

    protected <T extends Enum<T> & MessageKeyResource> Button makeButton(
            final ButtonIcon icon, final T buttonsKey) {
        final ButtonImage<T> buttonImage = new ButtonImage<T>(eventBus, source,
                i18n, buttonsKey, icon);
        return buttonImage.getContent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    protected boolean isContaining(final Node node) {
        return buttonPanel.getChildren().stream().filter(n -> n.equals(node))
                .findFirst().isPresent();
    }

    protected void remove(final Node node) {
        buttonPanel.getChildren().remove(node);
    }

    /**
     * Add a further row below the first one. The first row keeps its positions,
     * so its controls do not move when a screen adds more commands.
     *
     * @param row required row of controls
     */
    protected void addButtonRow(final FlowPane row) {
        mainPanel.getChildren().add(Preconditions.checkNotNull(row));
    }

    /**
     * @return the buttonPanel
     */
    public FlowPane getButtonPanel() {
        return buttonPanel;
    }

}
