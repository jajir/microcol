package org.akropia.gui.screen.goals;

import org.akropia.gui.Loc;

import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.buttonpanel.AbstractButtonsPanel;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class GoalsButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    private final Button buttonClose;

    @Inject
    GoalsButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n) {
        super(imageProvider, eventBus, Source.EUROPE, i18n);

        buttonClose = makeButton(ButtonIcon.BACK, Loc.backToGame);
        buttonClose.setId(CLOSE_BUTTON_ID);

        getButtonPanel().getChildren().add(buttonClose);
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonClose() {
        return buttonClose;
    }

}
