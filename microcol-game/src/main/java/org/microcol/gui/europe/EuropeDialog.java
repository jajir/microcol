package org.microcol.gui.europe;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class EuropeDialog extends AbstractMessageWindow implements EuropeDialogCallback {

    final Logger logger = LoggerFactory.getLogger(EuropeDialog.class);

    private final PanelDock europeDock;

    private final PanelHighSeas shipsTravelingToNewWorld;

    private final PanelHighSeas shipsTravelingToEurope;

    private final PanelPortPier panelPortPier;

    private final PanelEuropeGoods panelGoods;

    private final BooleanProperty propertyShiftWasPressed;

    @Inject
    public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final PanelHighSeas shipsTravelingToNewWorld,
            final PanelHighSeas shipsTravelingToEurope, final PanelPortPier panelPortPier,
            final RecruiteUnitsDialog recruiteUnitsDialog, final BuyUnitsDialog buyUnitsDialog,
            final PanelEuropeDockBehavior panelEuropeDockBehavior,
            final PanelEuropeGoods panelEuropeGoods) {
        super(viewUtil);
        propertyShiftWasPressed = new SimpleBooleanProperty(false);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        getDialog().setTitle(text.get("europe.title"));

        final Label label = new Label(text.get("europe.title"));

        this.shipsTravelingToEurope = Preconditions.checkNotNull(shipsTravelingToEurope);
        this.shipsTravelingToEurope.setTitle(text.get("europe.shipsTravelingToEurope"));
        this.shipsTravelingToEurope.setShownShipsTravelingToEurope(true);
        this.shipsTravelingToNewWorld = Preconditions.checkNotNull(shipsTravelingToNewWorld);
        this.shipsTravelingToNewWorld.setTitle(text.get("europe.shipsTravelingToNewWorld"));
        this.shipsTravelingToNewWorld.setShownShipsTravelingToEurope(false);

        europeDock = new PanelDock(imageProvider, panelEuropeDockBehavior);
        final VBox panelShips = new VBox();
        panelShips.getChildren().addAll(shipsTravelingToNewWorld, shipsTravelingToEurope,
                europeDock);

        this.panelPortPier = Preconditions.checkNotNull(panelPortPier);

        final Button recruiteButton = new Button(text.get("europe.recruit"));
        recruiteButton.setVisible(false);
        recruiteButton.setOnAction(event -> recruiteUnitsDialog.showAndWait());
        final Button buyButton = new Button(text.get("europe.buy"));

        buyButton.setOnAction(event -> buyUnitsDialog.showAndWait());
        final Button buttonOk = new Button(text.get("dialog.ok"));
        buttonOk.setOnAction(e -> {
            getDialog().close();
        });
        buttonOk.requestFocus();
        final VBox panelButtons = new VBox();
        panelButtons.getChildren().addAll(recruiteButton, buyButton, buttonOk);

        final HBox panelMiddle = new HBox();
        panelMiddle.getChildren().addAll(panelShips, panelPortPier, panelButtons);

        this.panelGoods = Preconditions.checkNotNull(panelEuropeGoods);

        final VBox mainPanel = new VBox();
        mainPanel.getChildren().addAll(label, panelMiddle, panelGoods);
        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
        /**
         * TODO there is a bug, keyboard events are not send during dragging.
         * TODO copy of this code is n colonyDialog
         */
        getScene().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                propertyShiftWasPressed.set(false);
            }
        });
        getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            logger.debug("wasShiftPressed " + event);
            if (event.getCode() == KeyCode.SHIFT) {
                propertyShiftWasPressed.set(true);
            }
        });
    }

    public void show() {
        repaint();
        getDialog().showAndWait();
    }

    @Override
    public void repaint() {
        europeDock.repaint();
        shipsTravelingToEurope.repaint();
        shipsTravelingToNewWorld.repaint();
        panelPortPier.repaint();
        panelGoods.repaint();
    }

    @Override
    public void repaintAfterGoodMoving() {
        europeDock.repaintCurrectShipsCrates();
        panelPortPier.repaint();
    }

    @Override
    public BooleanProperty getPropertyShiftWasPressed() {
        return propertyShiftWasPressed;
    }

}
