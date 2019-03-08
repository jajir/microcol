package org.microcol.gui;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.GoodsType;

import com.google.common.base.Preconditions;

/**
 * Convert {@link GoodsType} to localized goods names.
 */
public enum GoodsTypeName implements MessageKeyResource {

    corn(GoodsType.CORN),
    sugar(GoodsType.SUGAR),
    tobacco(GoodsType.TOBACCO),
    cotton(GoodsType.COTTON),
    fur(GoodsType.FUR),
    lumber(GoodsType.LUMBER),
    ore(GoodsType.ORE),
    silver(GoodsType.SILVER),
    horse(GoodsType.HORSE),
    rum(GoodsType.RUM),
    cigars(GoodsType.CIGARS),
    silk(GoodsType.SILK),
    coat(GoodsType.COAT),
    goods(GoodsType.GOODS),
    tools(GoodsType.TOOLS),
    musket(GoodsType.MUSKET),
    hammers(GoodsType.HAMMERS),
    cross(GoodsType.CROSS),
    bell(GoodsType.BELL);
    
    private final GoodsType goodsType;

    GoodsTypeName(final GoodsType goodsType) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
    }

    public static GoodsTypeName getNameForGoodsType(final GoodsType goodsType) {
        return Arrays.stream(values()).filter(gtName -> gtName.goodsType == goodsType).findFirst()
                .orElseThrow((() -> new IllegalArgumentException()));
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
