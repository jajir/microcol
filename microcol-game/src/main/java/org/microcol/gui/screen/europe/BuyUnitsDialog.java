package org.microcol.gui.screen.europe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.gui.dialog.DialogMessage;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public final class BuyUnitsDialog extends AbstractMessageWindow {

    private final static int MAX_UNITS_IN_ROW = 2;

    private final EuropeCallback europeDialogCallback;

    private final Label labelCaption;
    private final GameModelController gameModelController;
    private final DialogNotEnoughGold dialogNotEnoughGold;
    private final DialogMessage rejection;
    private final I18n i18n;
    private final List<BuyUnitPanel> panels = new ArrayList<>();
    private Player buyer;
    private Model openedModel;
    private boolean actionTaken;

    @Inject
    public BuyUnitsDialog(final ViewUtil viewUtil, final I18n i18n,
            final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final LocalizationHelper localizationHelper,
            final EuropeCallback europeDialogCallback,
            final DialogNotEnoughGold dialogNotEnoughGold,
            final DialogMessage rejection) {
        super(viewUtil, i18n);
        this.europeDialogCallback = Preconditions
                .checkNotNull(europeDialogCallback);
        Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.dialogNotEnoughGold = dialogNotEnoughGold;
        this.rejection = rejection;
        this.i18n = i18n;
        labelCaption = new Label();

        final VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        final GridPane gridWithUnits = new GridPane();

        AtomicInteger column = new AtomicInteger(0);
        AtomicInteger row = new AtomicInteger(0);
        UnitType.UNIT_TYPES.stream()
                .filter(unitType -> unitType.getEuropePrice() > 0)
                .forEach(unitType -> {
                    final BuyUnitPanel buyUnitPanel = new BuyUnitPanel(unitType,
                            imageProvider, localizationHelper, i18n, this);
                    panels.add(buyUnitPanel);
                    GridPane.setMargin(buyUnitPanel,
                            new Insets(10, 10, 10, 10));
                    gridWithUnits.add(buyUnitPanel, column.intValue(),
                            row.intValue());
                    column.incrementAndGet();
                    if (column.intValue() >= MAX_UNITS_IN_ROW) {
                        column.set(0);
                        row.incrementAndGet();
                    }
                });

        final ButtonBarOk buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        root.getChildren().addAll(labelCaption, gridWithUnits,
                buttonBar.getContent());
    }

    @Override
    public void showAndWait() {
        buyer = gameModelController.getHumanPlayer();
        openedModel = gameModelController.getModel();
        actionTaken = false;
        panels.forEach(panel -> panel.refresh(buyer));
        try {
            super.showAndWait();
        } finally {
            buyer = null;
            openedModel = null;
        }
    }

    void buy(final UnitType type) {
        if (buyer == null || actionTaken) {
            return;
        }
        actionTaken = true;
        try {
            Preconditions.checkState(
                    gameModelController.getModel() == openedModel,
                    "Purchase belongs to a replaced game");
            buyer.buy(type);
        } catch (NotEnoughtGoldException e) {
            actionTaken = false;
            dialogNotEnoughGold.showAndWait();
            panels.forEach(panel -> panel.refresh(buyer));
            return;
        } catch (IllegalStateException e) {
            actionTaken = false;
            rejection.setTitle(i18n.get(Europe.buyUnitDialog_title));
            rejection.setText(i18n.get(Europe.buyUnitDialog_unavailable));
            rejection.showAndWait();
            close();
            return;
        }
        closeAndRepaint();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Europe.buyUnitDialog_title));
        labelCaption.setText(i18n.get(Europe.buyUnitDialog_title));
    }

    public void closeAndRepaint() {
        europeDialogCallback.repaint();
        close();
    }

}
