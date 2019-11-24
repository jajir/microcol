package org.microcol.gui.util;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;

import com.google.common.base.Preconditions;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Page with floating buttons. Buttons are floating at the top of the screen.
 */
public final class ButtonedPage implements JavaFxComponent {

    private final StackPane mainPanel = new StackPane();

    private final AbstractButtonsPanel buttonsPanel;

    public ButtonedPage(final AbstractButtonsPanel buttonsPanel) {
        this.buttonsPanel = Preconditions.checkNotNull(buttonsPanel);
        mainPanel.getChildren().add(buttonsPanel.getContent());
    }

    public void setContent(final JavaFxComponent contentPanel) {
        Preconditions.checkNotNull(contentPanel);
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(contentPanel.getContent());
        mainPanel.getChildren().add(buttonsPanel.getContent());
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
