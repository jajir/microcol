package org.akropia.gui.screen.colony;

import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

final class DialogWarehouseTransfer extends AbstractWarningDialog {

    @Inject
    DialogWarehouseTransfer(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, ColonyMsg.unloadCaption);
    }

    void show(final String message) {
        final Label label = new Label(message);
        label.setId("warehouseTransferMessage");
        label.setWrapText(true);
        label.setMaxWidth(420);
        getContext().getChildren().setAll(label);
        getScene().getWindow().sizeToScene();
        showAndWait();
    }
}
