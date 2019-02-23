package org.microcol.gui.screen.europe;

import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.PanelDock;
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
public final class EuropePanel implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final StackPane mainPanel;

    private final Label labelTitle;

    private final PanelDock europeDock;

    private final PanelHighSeas<Europe> shipsTravelingToNewWorld;

    private final PanelHighSeas<Europe> shipsTravelingToEurope;

    private final PanelPortPier panelPortPier;

    private final PanelEuropeGoods panelGoods;

    private final BooleanProperty propertyShiftWasPressed;

    @Inject
    public EuropePanel(final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final PanelHighSeas<Europe> shipsTravelingToNewWorld,
            final PanelHighSeas<Europe> shipsTravelingToEurope, final PanelPortPier panelPortPier,
            final PanelEuropeDockBehavior panelEuropeDockBehavior,
            final PanelEuropeGoods panelEuropeGoods, final EuropeButtonsPanel europeButtonsPanel,
            final EventBus eventBus, final I18n i18n) {
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        this.panelPortPier = Preconditions.checkNotNull(panelPortPier);

        labelTitle = new Label();
        labelTitle.getStyleClass().add("label-title");

        this.shipsTravelingToEurope = Preconditions.checkNotNull(shipsTravelingToEurope);
        this.shipsTravelingToEurope.setShownShipsTravelingToEurope(true);
        this.shipsTravelingToEurope.setTitleKey(Europe.shipsTravelingToEurope);
        this.shipsTravelingToEurope.setOnMouseEnteredKey(Europe.statusBarShipsToEurope);
        this.shipsTravelingToEurope.addStyle("to-europe");
        this.shipsTravelingToNewWorld = Preconditions.checkNotNull(shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.setShownShipsTravelingToEurope(false);
        this.shipsTravelingToNewWorld.setTitleKey(Europe.shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.setOnMouseEnteredKey(Europe.statusBarShipsToNewWorld);
        this.shipsTravelingToNewWorld.addStyle("to-new-world");

        europeDock = new PanelDock(imageProvider, panelEuropeDockBehavior);
        europeDock.getContent().setOnMouseEntered(e -> {
            eventBus.post(
                    new StatusBarMessageEvent(i18n.get(Europe.statusBarEuropeDock), Source.EUROPE));
        });
        europeDock.getContent()
                .setOnMouseExited(event -> eventBus.post(new StatusBarMessageEvent(Source.EUROPE)));

        this.panelGoods = Preconditions.checkNotNull(panelEuropeGoods);

        mainPanel = new StackPane();
        mainPanel.getStyleClass().add("main-panel");
        mainPanel.getChildren().add(labelTitle);
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
        shipsTravelingToNewWorld.updateLanguage(i18n);
        shipsTravelingToEurope.updateLanguage(i18n);
        panelGoods.updateLanguage(i18n);
        panelPortPier.updateLanguage(i18n);
        europeDock.updateLanguage(i18n);
    }

    @Override
    public void repaint() {
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
