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
    grain, olives, grapes, wool, clay, lumber, ore, silver, horse, olive_oil,
    wine, cloth, amphorae, goods, tools, weapons, hammers, torches, owls,

    /*
     * Declension - 2 case - Accusative - singular
     *
     *
     * Was not needed.
     */

    /*
     * Declension - 2 case - genitiv - plurar
     */
    grain_dc2_pl, olives_dc2_pl, grapes_dc2_pl, wool_dc2_pl, clay_dc2_pl,
    lumber_dc2_pl, ore_dc2_pl, silver_dc2_pl, horse_dc2_pl, olive_oil_dc2_pl,
    wine_dc2_pl, cloth_dc2_pl, amphorae_dc2_pl, goods_dc2_pl, tools_dc2_pl,
    weapons_dc2_pl, hammers_dc2_pl, torches_dc2_pl, owls_dc2_pl,

    /*
     * Declension - 4 case - akuzativ - plurar
     */
    grain_dc7_pl, olives_dc7_pl, grapes_dc7_pl, wool_dc7_pl, clay_dc7_pl,
    lumber_dc7_pl, ore_dc7_pl, silver_dc7_pl, horse_dc7_pl, olive_oil_dc7_pl,
    wine_dc7_pl, cloth_dc7_pl, amphorae_dc7_pl, goods_dc7_pl, tools_dc7_pl,
    weapons_dc7_pl, hammers_dc7_pl, torches_dc7_pl, owls_dc7_pl,;

    public static GoodsTypeName getKey(final GoodsType goodsType,
            final int declensionCase, final boolean singular) {
        final String key = goodsType.name().toLowerCase() + "_dc"
                + declensionCase + "_" + (singular ? "sg" : "pl");
        return valueOf(key);
    }

    public static GoodsTypeName getNameForGoodsType(final GoodsType goodsType) {
        return valueOf(goodsType.name().toLowerCase());
    }

}
