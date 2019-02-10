package org.microcol.gui.colony;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class ColonyButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    private final Button buttonClose;

    @Inject
    ColonyButtonsPanel(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n) {
        super(imageProvider, eventBus, Source.COLONY, i18n);

        buttonClose = makeButon(ImageLoaderButtons.BUTTON_EXIT, ColonyMsg.buttonClose);
        buttonClose.setId(CLOSE_BUTTON_ID);
        buttonClose.setOnAction(evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));

        getButtonPanel().getChildren().add(buttonClose);
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonClose() {
        return buttonClose;
    }

}
