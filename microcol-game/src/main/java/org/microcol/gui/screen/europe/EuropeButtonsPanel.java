package org.microcol.gui.screen.europe;

import org.microcol.gui.Loc;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
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

    private final Button treasure = ActionButtons.button(ButtonIcon.GOLD);

    private final Button buttonRecruit = ActionButtons.button(ButtonIcon.BUY);
    private final Button buttonRepairs = ActionButtons
            .button(ButtonIcon.REPAIR);
    private final Button buttonTax = ActionButtons.button(ButtonIcon.TAX);
    private final Button buttonBackTax = ActionButtons.button(ButtonIcon.TAX);

    private final Button buttonClose;
    private final Button buttonBuyUnit;

    @Inject
    EuropeButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final ShipRepairDialog repairDialog,
            final org.microcol.gui.dialog.TreasureDialog treasureDialog) {
        super(imageProvider, eventBus, Source.EUROPE, i18n);

        buttonClose = makeButton(ButtonIcon.BACK, Loc.backToGame);
        buttonClose.setOnAction(
                evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));
        buttonClose.setId(CLOSE_BUTTON_ID);

        buttonBuyUnit = makeButton(ButtonIcon.BUY, Europe.buttonBuyUnit);
        buttonBuyUnit.setId(BUY_BUTTON_ID);

        treasure.setId("treasureOpen");
        treasure.setOnAction(event -> treasureDialog.showFor(null));
        buttonRecruit.setId("recruitButtonId");
        buttonRepairs.setId("shipRepairsButton");
        buttonRepairs.setOnAction(event -> repairDialog.showRepairs());
        buttonTax.setId("taxDemandButtonId");
        buttonTax.setVisible(false);
        buttonTax.setManaged(false);
        buttonBackTax.setId("backTaxButtonId");
        buttonBackTax.setVisible(false);
        buttonBackTax.setManaged(false);
        updateLanguage(i18n);
        getButtonPanel().getChildren().add(buttonTax);
        getButtonPanel().getChildren().add(buttonBackTax);
        getButtonPanel().getChildren().add(buttonRecruit);
        getButtonPanel().getChildren().add(buttonBuyUnit);
        getButtonPanel().getChildren().add(buttonRepairs);
        getButtonPanel().getChildren().add(treasure);
        getButtonPanel().getChildren().add(buttonClose);
    }

    /** Update the text of the recruitment action. */
    public void updateLanguage(final I18n i18n) {
        treasure.setText(i18n.get(org.microcol.gui.dialog.TreasureText.open));
        buttonRecruit.setText(i18n.get(Europe.recruitOpen));
        buttonRepairs.setText(i18n.get(Europe.repairsOpen));
        buttonTax.setText(i18n.get(Europe.taxOpen));
        buttonBackTax.setText(i18n.get(Europe.backTaxOpen));
    }

    /** @return the action that reopens an unresolved Crown tax demand */
    public Button getButtonTax() {
        return buttonTax;
    }

    /** @return the action that pays the back taxes of boycotted goods */
    public Button getButtonBackTax() {
        return buttonBackTax;
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
