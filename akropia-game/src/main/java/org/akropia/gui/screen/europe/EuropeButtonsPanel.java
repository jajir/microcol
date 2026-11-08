package org.akropia.gui.screen.europe;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutButtons;
import org.akropia.gui.Loc;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.buttonpanel.AbstractButtonsPanel;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;

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
            final org.akropia.gui.dialog.TreasureDialog treasureDialog,
            final KeyboardShortcuts shortcuts) {
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
        final Map<ShortcutAction, Supplier<? extends Button>> commands = new EnumMap<>(
                ShortcutAction.class);
        commands.put(ShortcutAction.EUROPE_RECRUIT, () -> buttonRecruit);
        commands.put(ShortcutAction.EUROPE_BUY_UNIT, () -> buttonBuyUnit);
        commands.put(ShortcutAction.EUROPE_REPAIRS, () -> buttonRepairs);
        commands.put(ShortcutAction.EUROPE_TREASURE, () -> treasure);
        commands.put(ShortcutAction.EUROPE_TAX, () -> buttonTax);
        commands.put(ShortcutAction.EUROPE_BACK_TAX, () -> buttonBackTax);
        ShortcutButtons.install(buttonClose, shortcuts, commands);
        final Map<ShortcutAction, Button> hinted = new EnumMap<>(
                ShortcutAction.class);
        commands.forEach((action, button) -> hinted.put(action, button.get()));
        ShortcutButtons.statusHints(shortcuts, hinted,
                text -> eventBus
                        .post(new StatusBarMessageEvent(text, Source.EUROPE)),
                () -> eventBus.post(new StatusBarMessageEvent(Source.EUROPE)));
    }

    /** Update the text of the recruitment action. */
    public void updateLanguage(final I18n i18n) {
        treasure.setText(i18n.get(org.akropia.gui.dialog.TreasureText.open));
        buttonRecruit.setText(i18n.get(Europe.recruitOpen));
        buttonBuyUnit.setText(i18n.get(Europe.buttonBuyUnit));
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
