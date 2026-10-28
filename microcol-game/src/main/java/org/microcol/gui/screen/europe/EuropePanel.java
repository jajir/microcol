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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * Panel with Europe port.
 */
@Singleton
public final class EuropePanel
        implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final EuropeButtonsPanel europeButtonsPanel;

    private final BorderPane mainPanel;

    private final Label labelTitle;

    private final Label immigration = new Label();
    private final Label dockTitle = new Label();
    private final Label pierTitle = new Label();

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
            final PanelDockEurope panelDockEurope,
            final EuropeBackground background) {
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

        mainPanel = new BorderPane();
        mainPanel.getStyleClass().add("europe-harbor");
        mainPanel.setMinWidth(950);
        final Pane scene = new Pane() {
            @Override
            protected void layoutChildren() {
                final double width = getWidth();
                final double height = getHeight();
                final double half = width / 2;
                background.getContent().resizeRelocate(0, 0, width, height);
                labelTitle.resizeRelocate(32, 18, width - 64, 30);
                immigration.resizeRelocate(32, 54, width - 64,
                        immigration.prefHeight(width - 64));
                shipsTravelingToEurope.getContent().resizeRelocate(32, 134,
                        half - 48, 220);
                shipsTravelingToNewWorld.getContent().resizeRelocate(half + 16,
                        134, half - 48, 220);
                dockTitle.resizeRelocate(32, height - 226, half - 48, 26);
                europeDock.getContent().resizeRelocate(32, height - 198,
                        half - 48, 190);
                pierTitle.resizeRelocate(half + 16, height - 226, half - 48,
                        26);
                panelPortPier.getContent().resizeRelocate(half + 16,
                        height - 198, half - 48, 96);
            }
        };
        scene.setId("europe-harbor-scene");
        scene.setMinHeight(600);
        scene.setPrefHeight(600);
        dockTitle.setId("europe-dock-count");
        pierTitle.setId("europe-pier-count");
        dockTitle.getStyleClass().add("harbor-heading");
        pierTitle.getStyleClass().add("harbor-heading");
        immigration.setId("immigration-progress");
        immigration.setWrapText(true);
        immigration.getStyleClass().add("immigration-progress");
        scene.getChildren().addAll(background.getContent(), labelTitle,
                immigration, shipsTravelingToNewWorld.getContent(),
                shipsTravelingToEurope.getContent(), dockTitle, pierTitle,
                panelPortPier.getContent(), europeDock.getContent());
        mainPanel.setCenter(scene);
        mainPanel.setBottom(panelGoods.getContent());
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
        // A declared nation's home port is closed to hiring and purchases
        // (R-0187); the model refuses them too.
        final boolean closed = !player.canTradeInEurope();
        europeButtonsPanel.getButtonBuyUnit()
                .setDisable(pendingChoice || closed);
        europeButtonsPanel.getButtonRecruit().setDisable(taxChoice || closed);
        europeButtonsPanel.getButtonTax().setVisible(taxChoice);
        europeButtonsPanel.getButtonTax().setManaged(taxChoice);
        final boolean backTaxes = !player.getTaxArrears().isEmpty();
        europeButtonsPanel.getButtonBackTax().setVisible(backTaxes);
        europeButtonsPanel.getButtonBackTax().setManaged(backTaxes);
        europeButtonsPanel.getButtonBackTax()
                .setDisable(pendingChoice || !player.canTradeInEurope());
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
        updateCounts();
    }

    public void repaintAfterGoodMoving() {
        europeDock.repaintCurrectShipsCrates();
        panelPortPier.repaint();
        updateCounts();
    }

    private void updateCounts() {
        dockTitle.setText(i18n.get(Europe.harborDock,
                controller.getModel().getEurope().getPort()
                        .getShipsInPort(controller.getHumanPlayer()).size()));
        pierTitle.setText(i18n.get(Europe.harborPier,
                controller.getModel().getEurope().getPier()
                        .getUnits(controller.getHumanPlayer()).size()));
    }

    public BooleanProperty getPropertyShiftWasPressed() {
        return propertyShiftWasPressed;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
