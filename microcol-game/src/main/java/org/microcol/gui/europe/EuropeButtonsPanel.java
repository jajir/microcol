package org.microcol.gui.europe;

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
public class EuropeButtonsPanel extends AbstractButtonsPanel {

    private final Button buttonClose;
    private final Button buttonBuyUnit;

    @Inject
    EuropeButtonsPanel(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n) {
        super(imageProvider, eventBus, Source.EUROPE, i18n);

        buttonClose = makeButon(ImageLoaderButtons.BUTTON_EXIT, Europe.buttonClose);
        buttonClose.setOnAction(evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));

        buttonBuyUnit = makeButon(ImageLoaderButtons.BUTTON_BUY, Europe.buttonBuyUnit);

        getButtonPanel().getChildren().add(buttonBuyUnit);
        getButtonPanel().getChildren().add(buttonClose);
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonClose() {
        return buttonClose;
    }

    /**
     * @return the buttonRecruiteUnit
     */
    public Button getButtonBuyUnit() {
        return buttonBuyUnit;
    }

}
