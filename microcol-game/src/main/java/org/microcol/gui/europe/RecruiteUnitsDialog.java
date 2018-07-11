package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
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

    @Inject
    public RecruiteUnitsDialog(final ViewUtil viewUtil, final Text text,
            final ImageProvider imageProvider, final GameModelController gameModelController,
            final LocalizationHelper localizationHelper,
            final DialogNotEnoughGold dialogNotEnoughGold) {
        super(viewUtil);
        Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(gameModelController);
        setTitle(text.get("recruitUnitDialog.title"));
        final Label labelCaption = new Label(text.get("recruitUnitDialog.title"));

        final VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        final HBox gridWithUnits = new HBox();

        UnitType.UNIT_TYPES.stream().filter(unitType -> unitType.getEuropePrice() > 0)
                .forEach(unitType -> {
                    final RecruiteUnitPanel buyUnitPanel = new RecruiteUnitPanel(unitType,
                            imageProvider, gameModelController, localizationHelper, text, this,
                            dialogNotEnoughGold);
                    HBox.setMargin(buyUnitPanel, new Insets(10, 10, 10, 10));
                    gridWithUnits.getChildren().add(buyUnitPanel);
                });

        final ButtonsBar buttonBar = new ButtonsBar(text);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        root.getChildren().addAll(labelCaption, gridWithUnits, buttonBar);
    }

}
