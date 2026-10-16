package org.microcol.gui.screen.europe;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class EuropeButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    public static final String BUY_BUTTON_ID = "buyButtonId";

    private final Button buttonRecruit = new Button();
    private final Button buttonRepairs = new Button();

    private final Button buttonClose;
    private final Button buttonBuyUnit;

    @Inject
    EuropeButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final ShipRepairDialog repairDialog) {
        super(imageProvider, eventBus, Source.EUROPE, i18n);

        buttonClose = makeButon(ImageLoaderButtons.BUTTON_EXIT,
                Europe.buttonClose);
        buttonClose.setOnAction(
                evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));
        buttonClose.setId(CLOSE_BUTTON_ID);

        buttonBuyUnit = makeButon(ImageLoaderButtons.BUTTON_BUY,
                Europe.buttonBuyUnit);
        buttonBuyUnit.setId(BUY_BUTTON_ID);

        buttonRecruit.setId("recruitButtonId");
        buttonRepairs.setId("shipRepairsButton");
        buttonRepairs.setOnAction(event -> repairDialog.showRepairs());
        updateLanguage(i18n);
        getButtonPanel().getChildren().add(buttonRecruit);
        getButtonPanel().getChildren().add(buttonBuyUnit);
        getButtonPanel().getChildren().add(buttonRepairs);
        getButtonPanel().getChildren().add(buttonClose);
    }

    /** Update the text of the recruitment action. */
    public void updateLanguage(final I18n i18n) {
        buttonRecruit.setText(i18n.get(Europe.recruitOpen));
        buttonRepairs.setText(i18n.get(Europe.repairsOpen));
    }

    public Button getButtonRecruit() {
        return buttonRecruit;
    }

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
