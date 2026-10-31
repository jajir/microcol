package org.microcol.gui.screen.europe;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Panel with Europe port.
 */
@Singleton
public final class EuropePanel
        implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final EuropeButtonsPanel europeButtonsPanel;

    private final BorderPane mainPanel;

    private final Label immigration = new Label();
    private final Label dockTitle = new Label();
    private final Label pierTitle = new Label();
    private final Button depart = ActionButtons.button(ButtonIcon.BACK);

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
            final EuropeBackground background,
            final ImageProvider imageProvider) {
        this.europeButtonsPanel = europeButtonsPanel;
        this.controller = gameModelController;
        this.i18n = i18n;
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        Preconditions.checkNotNull(gameModelController);
        this.panelPortPier = Preconditions.checkNotNull(panelPortPier);
        this.europeDock = Preconditions.checkNotNull(panelDockEurope);

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

        depart.setId("europe-depart");
        depart.getStyleClass().add("toolbar-action");
        depart.getGraphic().setScaleX(-1);
        depart.setOnAction(event -> europeDock.getSelectedShip()
                .ifPresent(shipsTravelingToNewWorld::depart));
        europeDock.setHarborAction(depart);

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
        final ImageView arrival = new ImageView(
                imageProvider.getImage("europe-arrival-route.png"));
        final ImageView departure = new ImageView(
                imageProvider.getImage("europe-departure-route.png"));
        arrival.setMouseTransparent(true);
        departure.setMouseTransparent(true);
        final Pane content = new Pane() {
            @Override
            protected void layoutChildren() {
                final double width = getWidth();
                final double height = getHeight();
                final double half = width / 2;
                arrival.setFitWidth(half / 2 - 48);
                arrival.setFitHeight(height - 480);
                arrival.relocate(16, 236);
                departure.setFitWidth(width - 180);
                departure.setFitHeight(height - 345);
                departure.relocate(180, 205);
                immigration.resizeRelocate(0, 54, width,
                        immigration.prefHeight(width));
                shipsTravelingToEurope.getContent().resizeRelocate(0, 134,
                        half - 16, 220);
                shipsTravelingToNewWorld.getContent().resizeRelocate(half + 16,
                        134, half - 16, 220);
                dockTitle.resizeRelocate(0, height - 226, half - 16, 26);
                europeDock.getContent().resizeRelocate(0, height - 198,
                        half - 16, 190);
                pierTitle.resizeRelocate(half + 16, height - 226, half - 16,
                        26);
                panelPortPier.getContent().resizeRelocate(half + 16,
                        height - 198, half - 16, 96);
            }
        };
        content.getStyleClass().add("game-bar-content");
        StackPane.setMargin(content, new Insets(0, 16, 0, 16));
        final StackPane scene = new StackPane(background.getContent(), content);
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
        content.getChildren().addAll(arrival, departure, immigration,
                shipsTravelingToNewWorld.getContent(),
                shipsTravelingToEurope.getContent(), dockTitle, pierTitle,
                panelPortPier.getContent(), europeDock.getContent());
        mainPanel.setCenter(scene);
        mainPanel.setBottom(panelGoods.getContent());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        depart.setText(i18n.get(Europe.sailToNewWorld));
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
        dockTitle.setText(i18n.get(Europe.harborDock));
        pierTitle.setText(i18n.get(Europe.harborPier));
    }

    public BooleanProperty getPropertyShiftWasPressed() {
        return propertyShiftWasPressed;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
