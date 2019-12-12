package org.microcol.gui;

import org.microcol.i18n.MessageKeyResource;
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

    /*
     * Declension - 4 case - akuzativ - plurar
     */
    corn_dc7_pl,
    sugar_dc7_pl,
    tobacco_dc7_pl,
    cotton_dc7_pl,
    fur_dc7_pl,
    lumber_dc7_pl,
    ore_dc7_pl,
    silver_dc7_pl,
    horse_dc7_pl,
    rum_dc7_pl,
    cigars_dc7_pl,
    silk_dc7_pl,
    coat_dc7_pl,
    goods_dc7_pl,
    tools_dc7_pl,
    musket_dc7_pl,
    hammers_dc7_pl,
    cross_dc7_pl,
    bell_dc7_pl,
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

}
