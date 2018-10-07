package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.LocalizationHelper;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public final class RecruiteUnitsDialog extends AbstractMessageWindow {

    private final Label labelCaption;

    @Inject
    public RecruiteUnitsDialog(final ViewUtil viewUtil, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final LocalizationHelper localizationHelper,
            final DialogNotEnoughGold dialogNotEnoughGold, final I18n i18n) {
        super(viewUtil, i18n);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        labelCaption = new Label();

        final VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        final HBox gridWithUnits = new HBox();

        UnitType.UNIT_TYPES.stream().filter(unitType -> unitType.getEuropePrice() > 0)
                .forEach(unitType -> {
                    final RecruiteUnitPanel buyUnitPanel = new RecruiteUnitPanel(unitType,
                            imageProvider, gameModelController, localizationHelper, i18n, this,
                            dialogNotEnoughGold);
                    HBox.setMargin(buyUnitPanel, new Insets(10, 10, 10, 10));
                    gridWithUnits.getChildren().add(buyUnitPanel);
                });

        final ButtonsBar buttonBar = new ButtonsBar(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        root.getChildren().addAll(labelCaption, gridWithUnits, buttonBar);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Europe.recruitUnitDialogTitle));
        labelCaption.setText(i18n.get(Europe.recruitUnitDialogTitle));
    }

}
