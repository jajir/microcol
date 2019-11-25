package org.microcol.gui.screen.europe;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum Europe implements MessageKeyResource {

    title,
    shipsTravelingToNewWorld,
    shipsTravelingToEurope,
    buyUnitDialog_title,
    buyUnitDialog_buttonBuyUnit,
    goodsPanelTitle,
    recruitUnitDialogTitle,
    recruitUnitDialogButton,
    statusBarEuropeDock,
    statusBarShipsToNewWorld,
    statusBarShipsToEurope,
    statusBarPier,
    goodsToSell,
    buttonClose,
    buttonBuyUnit;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
