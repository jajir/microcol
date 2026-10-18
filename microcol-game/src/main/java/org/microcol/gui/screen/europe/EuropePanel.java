package org.microcol.gui.screen.europe;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Panel with Europe port.
 */
@Singleton
public final class EuropePanel
        implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final EuropeButtonsPanel europeButtonsPanel;

    private final StackPane mainPanel;

    private final Label labelTitle;

    private final Label immigration = new Label();

    private final GameModelController controller;

    private final I18n i18n;

    private final PanelDockEurope europeDock;

    private final PanelHighSeas<Europe> shipsTravelingToNewWorld;

    private final PanelHighSeas<Europe> shipsTravelingToEurope;

    private final PanelPortPier panelPortPier;

    private final PanelEuropeGoods panelGoods;

    private final BooleanProperty propertyShiftWasPressed;

    @Inject
    public EuropePanel(final GameModelController gameModelController,
            final PanelHighSeas<Europe> shipsTravelingToNewWorld,
            final PanelHighSeas<Europe> shipsTravelingToEurope,
            final PanelPortPier panelPortPier,
            final PanelEuropeGoods panelEuropeGoods,
            final EuropeButtonsPanel europeButtonsPanel,
            final EventBus eventBus, final I18n i18n,
            final PanelDockEurope panelDockEurope) {
        this.europeButtonsPanel = europeButtonsPanel;
        this.controller = gameModelController;
        this.i18n = i18n;
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        Preconditions.checkNotNull(gameModelController);
        this.panelPortPier = Preconditions.checkNotNull(panelPortPier);
        this.europeDock = Preconditions.checkNotNull(panelDockEurope);

        labelTitle = new Label();
        labelTitle.getStyleClass().add("label-title");

        this.shipsTravelingToEurope = Preconditions
                .checkNotNull(shipsTravelingToEurope);
        this.shipsTravelingToEurope.setShownShipsTravelingToEurope(true);
        this.shipsTravelingToEurope.setTitleKey(Europe.shipsTravelingToEurope);
        this.shipsTravelingToEurope
                .setOnMouseEnteredKey(Europe.statusBarShipsToEurope);
        this.shipsTravelingToEurope.addStyle("to-europe");
        this.shipsTravelingToNewWorld = Preconditions
                .checkNotNull(shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.setShownShipsTravelingToEurope(false);
        this.shipsTravelingToNewWorld
                .setTitleKey(Europe.shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld
                .setOnMouseEnteredKey(Europe.statusBarShipsToNewWorld);
        this.shipsTravelingToNewWorld.addStyle("to-new-world");

        europeDock.getContent().setOnMouseEntered(e -> {
            eventBus.post(new StatusBarMessageEvent(
                    i18n.get(Europe.statusBarEuropeDock), Source.EUROPE));
        });
        europeDock.getContent().setOnMouseExited(event -> eventBus
                .post(new StatusBarMessageEvent(Source.EUROPE)));

        this.panelGoods = Preconditions.checkNotNull(panelEuropeGoods);

        mainPanel = new StackPane();
        mainPanel.getStyleClass().add("main-panel");
        mainPanel.getChildren().add(labelTitle);
        immigration.setId("immigration-progress");
        immigration.setWrapText(true);
        immigration.getStyleClass().add("immigration-progress");
        mainPanel.getChildren().add(immigration);
        mainPanel.getChildren().add(shipsTravelingToNewWorld.getContent());
        mainPanel.getChildren().add(shipsTravelingToEurope.getContent());
        mainPanel.getChildren().add(panelPortPier.getContent());
        mainPanel.getChildren().add(europeDock.getContent());
        mainPanel.getChildren().add(panelGoods.getContent());
        mainPanel.getChildren().add(europeButtonsPanel.getContent());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        labelTitle.setText(i18n.get(Europe.title));
        europeButtonsPanel.updateLanguage(i18n);
        shipsTravelingToNewWorld.updateLanguage(i18n);
        shipsTravelingToEurope.updateLanguage(i18n);
        panelGoods.updateLanguage(i18n);
        panelPortPier.updateLanguage(i18n);
        europeDock.updateLanguage(i18n);
    }

    @Override
    public void repaint() {
        final org.microcol.model.Player player = controller.getHumanPlayer();
        final boolean earnedChoice = controller.getModel()
                .isAwaitingImmigrationChoice();
        final boolean taxChoice = controller.getModel().isAwaitingTaxDecision();
        final boolean pendingChoice = earnedChoice || taxChoice;
        europeDock.getContent().setDisable(pendingChoice);
        shipsTravelingToEurope.getContent().setDisable(pendingChoice);
        shipsTravelingToNewWorld.getContent().setDisable(pendingChoice);
        panelPortPier.getContent().setDisable(pendingChoice);
        panelGoods.getContent().setDisable(pendingChoice);
        europeButtonsPanel.getButtonBuyUnit().setDisable(pendingChoice);
        europeButtonsPanel.getButtonRecruit().setDisable(taxChoice);
        europeButtonsPanel.getButtonTax().setVisible(taxChoice);
        europeButtonsPanel.getButtonTax().setManaged(taxChoice);
        europeButtonsPanel.getButtonRecruit().setText(i18n
                .get(earnedChoice ? Europe.earnedOpen : Europe.recruitOpen));
        immigration
                .setText(i18n.get(Europe.immigrationProgress,
                        player.getImmigrationCrosses(), player
                                .getImmigrationTarget())
                        + (player.hasPendingImmigrant()
                                ? "\n" + i18n
                                        .get(earnedChoice ? Europe.earnedPending
                                                : Europe.immigrationPending)
                                : "")
                        + (taxChoice ? "\n" + i18n.get(Europe.taxPending)
                                : ""));
        europeDock.repaint();
        shipsTravelingToEurope.repaint();
        shipsTravelingToNewWorld.repaint();
        panelPortPier.repaint();
        panelGoods.repaint();
    }

    public void repaintAfterGoodMoving() {
        europeDock.repaintCurrectShipsCrates();
        panelPortPier.repaint();
    }

    public BooleanProperty getPropertyShiftWasPressed() {
        return propertyShiftWasPressed;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
