package org.microcol.gui.screen.market;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;

public enum Market implements MessageKeyResource {

    title,
    titleBuy,
    titleSell,
    buying,
    howMuch,
    howMuchSell,
    pricePerPiece,
    selected,
    selectedSell,
    tax,
    total,
    totalSell,
    price,
    buttonBuy,
    buttonSell,
    buttonCancel,
    ;
    
    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
