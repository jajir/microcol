package org.microcol.gui.screen.statistics;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class StatisticsButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    private final Button buttonClose;

    @Inject
    StatisticsButtonsPanel(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n) {
        super(imageProvider, eventBus, Source.EUROPE, i18n);

        buttonClose = makeButon(ImageLoaderButtons.BUTTON_EXIT, Stats.buttonCancel);
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
