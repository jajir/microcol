package org.microcol.gui;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.GoodsType;

/**
 * Convert {@link GoodsType} to localized goods names.
 */
public enum GoodsTypeName implements MessageKeyResource {
    /*
     * Declension - 1 case - Nominative - singular
     */
    corn,
    sugar,
    tobacco,
    cotton,
    fur,
    lumber,
    ore,
    silver,
    horse,
    rum,
    cigars,
    silk,
    coat,
    goods,
    tools,
    musket,
    hammers,
    cross,
    bell,

    /*
     * Declension - 2 case - Accusative - singular
     * 
     * 
     * Was not needed.
     */

    /*
     * Declension - 2 case - genitiv - plurar
     */
    corn_dc2_pl,
    sugar_dc2_pl,
    tobacco_dc2_pl,
    cotton_dc2_pl,
    fur_dc2_pl,
    lumber_dc2_pl,
    ore_dc2_pl,
    silver_dc2_pl,
    horse_dc2_pl,
    rum_dc2_pl,
    cigars_dc2_pl,
    silk_dc2_pl,
    coat_dc2_pl,
    goods_dc2_pl,
    tools_dc2_pl,
    musket_dc2_pl,
    hammers_dc2_pl,
    cross_dc2_pl,
    bell_dc2_pl,

    ;

    public static GoodsTypeName getKey(final GoodsType goodsType, final int declensionCase,
            final boolean singular) {
        final String key = goodsType.name().toLowerCase() + "_dc" + declensionCase + "_"
                + (singular ? "sg" : "pl");
        return valueOf(key);
    }

    public static GoodsTypeName getNameForGoodsType(final GoodsType goodsType) {
        return valueOf(goodsType.name().toLowerCase());
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
