package org.microcol.gui;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;

/**
 * Convert {@link GoodType} to localized goods names.
 */
public enum GoodsTypeName implements MessageKeyResource {

    corn(GoodType.CORN),
    sugar(GoodType.SUGAR),
    tobacco(GoodType.TOBACCO),
    cotton(GoodType.COTTON),
    fur(GoodType.FUR),
    lumber(GoodType.LUMBER),
    ore(GoodType.ORE),
    silver(GoodType.SILVER),
    horse(GoodType.HORSE),
    rum(GoodType.RUM),
    cigars(GoodType.CIGARS),
    silk(GoodType.SILK),
    coat(GoodType.COAT),
    goods(GoodType.GOODS),
    tools(GoodType.TOOLS),
    musket(GoodType.MUSKET),
    hammers(GoodType.HAMMERS),
    cross(GoodType.CROSS),
    bell(GoodType.BELL);
    
    private final GoodType goodType;

    GoodsTypeName(final GoodType goodType) {
        this.goodType = Preconditions.checkNotNull(goodType);
    }

    public static GoodsTypeName getNameForGoodType(final GoodType goodType) {
        return Arrays.stream(values()).filter(gtName -> gtName.goodType == goodType).findFirst()
                .orElseThrow((() -> new IllegalArgumentException()));
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
