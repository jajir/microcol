package org.microcol.gui.screen.europe;

import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
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

    @Inject
    public BuyUnitsDialog(final ViewUtil viewUtil, final I18n i18n,
            final ImageProvider imageProvider, final GameModelController gameModelController,
            final LocalizationHelper localizationHelper,
            final EuropeCallback europeDialogCallback,
            final DialogNotEnoughGold dialogNotEnoughGold) {
        super(viewUtil, i18n);
        this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        labelCaption = new Label();

        final VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        final GridPane gridWithUnits = new GridPane();

        AtomicInteger column = new AtomicInteger(0);
        AtomicInteger row = new AtomicInteger(0);
        UnitType.UNIT_TYPES.stream().filter(unitType -> unitType.getEuropePrice() > 0)
                .forEach(unitType -> {
                    final BuyUnitPanel buyUnitPanel = new BuyUnitPanel(unitType, imageProvider,
                            gameModelController, localizationHelper, i18n, this,
                            dialogNotEnoughGold);
                    GridPane.setMargin(buyUnitPanel, new Insets(10, 10, 10, 10));
                    gridWithUnits.add(buyUnitPanel, column.intValue(), row.intValue());
                    column.incrementAndGet();
                    if (column.intValue() > MAX_UNITS_IN_ROW) {
                        column.set(0);
                        row.incrementAndGet();
                    }
                });

        final ButtonsBar buttonBar = new ButtonsBar(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        root.getChildren().addAll(labelCaption, gridWithUnits, buttonBar);
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
