package org.microcol.gui.screen.colony;

import org.microcol.gui.Loc;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.event.model.GameModelController;
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
public class ColonyButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    private final Button buttonClose;
    private final Button treasure = ActionButtons.button(ButtonIcon.GOLD);
    private final Button exports = ActionButtons.button(ButtonIcon.EXPORT);
    private final Button education = ActionButtons.button(ButtonIcon.EDUCATION);
    private final Button tradeRoute = ActionButtons.button(ButtonIcon.TRADE);
    private final Button tradeRouteCargo = ActionButtons
            .button(ButtonIcon.CARGO);
    private final I18n i18n;
    private final GameModelController game;
    private org.microcol.model.Colony colony;

    @Inject
    ColonyButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final GameModelController game, final DialogCustomHouse customHouse,
            final DialogEducation school, final DialogTradeRoute routes,
            final DialogTradeRouteCargo routeCargo,
            final org.microcol.gui.dialog.TreasureDialog treasureDialog) {
        super(imageProvider, eventBus, Source.COLONY, i18n);

        this.i18n = i18n;
        treasure.setId("treasureOpen");
        treasure.setOnAction(event -> treasureDialog.showFor(colony));
        this.game = game;
        getButtonPanel().setId("colonyButtons");
        education.setId("educationOpen");
        education.setOnAction(event -> school.showFor(colony));
        education.setDisable(true);
        exports.setId("customHouseOpen");
        exports.setOnAction(event -> customHouse.showFor(colony));
        exports.setDisable(true);
        tradeRoute.setId("tradeRouteOpen");
        tradeRoute.setOnAction(event -> routes.showFor(colony));
        tradeRoute.setDisable(true);
        tradeRouteCargo.setId("tradeRouteCargoOpen");
        tradeRouteCargo.setOnAction(event -> routeCargo.showFor(colony));
        tradeRouteCargo.setDisable(true);
        buttonClose = makeButton(ButtonIcon.BACK, Loc.backToGame);
        buttonClose.setId(CLOSE_BUTTON_ID);
        buttonClose.setOnAction(
                evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));

        getButtonPanel().getChildren().addAll(exports, education, buttonClose);
    }

    void setColony(org.microcol.model.Colony colony) {
        this.colony = colony;
        treasure.setText(i18n.get(org.microcol.gui.dialog.TreasureText.open));
        treasure.setDisable(colony.getOwner() != game.getHumanPlayer());
        education.setText(i18n.get(EducationText.open));
        education.setDisable(false);
        tradeRoute.setText(i18n.get(TradeRouteText.open));
        tradeRoute.setDisable(!game.isGameModelReady()
                || colony.getOwner() != game.getHumanPlayer());
        tradeRouteCargo.setText(i18n.get(TradeRouteText.cargoOpen));
        tradeRouteCargo.setDisable(tradeRoute.isDisabled());
        exports.setText(i18n.get(CustomHouseText.open));
        exports.setDisable(!colony.isContainsConstructionByType(
                org.microcol.model.ConstructionType.CUSTOM_HOUSE));
        exports.setAccessibleHelp(
                i18n.get(exports.isDisabled() ? CustomHouseText.unavailable
                        : CustomHouseText.rule));
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonClose() {
        return buttonClose;
    }

    void addQuickCargoButtons(final List<Button> buttons) {
        getButtonPanel().getChildren().remove(buttonClose);
        getButtonPanel().getChildren().addAll(treasure, tradeRouteCargo,
                tradeRoute);
        getButtonPanel().getChildren().addAll(buttons);
        getButtonPanel().getChildren().add(buttonClose);
    }
}
