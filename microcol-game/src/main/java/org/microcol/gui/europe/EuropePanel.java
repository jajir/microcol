package org.microcol.gui.europe;

import org.microcol.gui.Loc;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel with Europe port.
 */
@Singleton
public final class EuropePanel implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final VBox mainPanel;

    private final Label labelTitle;

    private final PanelDock europeDock;

    private final PanelHighSeas<Europe> shipsTravelingToNewWorld;

    private final PanelHighSeas<Europe> shipsTravelingToEurope;

    private final PanelPortPier panelPortPier;

    private final PanelEuropeGoods panelGoods;

    private final BooleanProperty propertyShiftWasPressed;

    private final Button recruiteButton;

    private final Button buyButton;

    private final Button buttonOk;

    @Inject
    public EuropePanel(final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final PanelHighSeas<Europe> shipsTravelingToNewWorld,
            final PanelHighSeas<Europe> shipsTravelingToEurope, final PanelPortPier panelPortPier,
            final RecruiteUnitsDialog recruiteUnitsDialog, final BuyUnitsDialog buyUnitsDialog,
            final PanelEuropeDockBehavior panelEuropeDockBehavior,
            final PanelEuropeGoods panelEuropeGoods, final EuropeCallback europeCallback) {
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        this.panelPortPier = Preconditions.checkNotNull(panelPortPier);

        labelTitle = new Label();
        labelTitle.getStyleClass().add("label-title");

        this.shipsTravelingToEurope = Preconditions.checkNotNull(shipsTravelingToEurope);
        this.shipsTravelingToEurope.setShownShipsTravelingToEurope(true);
        this.shipsTravelingToEurope.setTitleKey(Europe.shipsTravelingToEurope);
        this.shipsTravelingToEurope.addStyle("to-europe");
        this.shipsTravelingToNewWorld = Preconditions.checkNotNull(shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.setShownShipsTravelingToEurope(false);
        this.shipsTravelingToNewWorld.setTitleKey(Europe.shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.addStyle("to-new-world");

        europeDock = new PanelDock(imageProvider, panelEuropeDockBehavior);
        final VBox panelLeft = new VBox();
        panelLeft.getChildren().addAll(shipsTravelingToEurope.getContent(),
                shipsTravelingToNewWorld.getContent(), europeDock.getContent());

        recruiteButton = new Button();
        recruiteButton.setOnAction(event -> recruiteUnitsDialog.showAndWait());

        buyButton = new Button();
        buyButton.setOnAction(event -> buyUnitsDialog.showAndWait());

        buttonOk = new Button();
        buttonOk.setOnAction(e -> {
            europeCallback.close();
        });
        buttonOk.requestFocus();

        final VBox panelButtons = new VBox();
        panelButtons.getStyleClass().add("buttons");
        VBox.setVgrow(panelButtons, Priority.ALWAYS);
        // TODO add recruiteButton,
        panelButtons.getChildren().addAll(buyButton, buttonOk);

        final VBox panelRight = new VBox();
        panelRight.getStyleClass().add("right-panel");
        VBox.setVgrow(panelRight, Priority.ALWAYS);
        HBox.setHgrow(panelRight, Priority.ALWAYS);

        final HBox portPierContainer = new HBox(panelPortPier.getContent());
        portPierContainer.setMinWidth(GamePanelView.TILE_WIDTH_IN_PX * 3);
        portPierContainer.setMinHeight(GamePanelView.TILE_WIDTH_IN_PX);
        portPierContainer.getStyleClass().add("port-pier-container");

        panelRight.getChildren().addAll(panelButtons, portPierContainer);

        final HBox panelScenery = new HBox();
        panelScenery.getChildren().addAll(panelLeft, panelRight);

        this.panelGoods = Preconditions.checkNotNull(panelEuropeGoods);

        final BackgroundImage europeImage = new BackgroundImage(
                imageProvider.getImage(ImageProvider.IMG_EUROPE), BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.RIGHT, 0, false, Side.TOP, 80, false),
                BackgroundSize.DEFAULT);
        mainPanel = new VBox();
        mainPanel.getChildren().addAll(labelTitle, panelScenery, panelGoods.getContent());
        mainPanel.setBackground(new Background(europeImage));
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        labelTitle.setText(i18n.get(Europe.title));
        recruiteButton.setText(i18n.get(Europe.recruit));
        buyButton.setText(i18n.get(Europe.buy));
        buttonOk.setText(i18n.get(Loc.ok));
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
