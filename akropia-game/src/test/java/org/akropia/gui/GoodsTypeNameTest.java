package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.ConstructionType;
import org.akropia.model.GoodsType;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.UnitType;
import org.akropia.model.store.GsonGoodsTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * R-0394: the 19 goods keep their order and rules under the Mediterranean
 * names, in English and Czech, in saved data, without legacy aliases.
 */
class GoodsTypeNameTest {

    private static final List<String> IDS = List.of("GRAIN", "OLIVES", "GRAPES",
            "WOOL", "CLAY", "LUMBER", "ORE", "SILVER", "HORSE", "OLIVE_OIL",
            "WINE", "CLOTH", "AMPHORAE", "GOODS", "TOOLS", "WEAPONS", "HAMMERS",
            "TORCHES", "OWLS");

    private static final List<String> ENGLISH = List.of("Grain", "Olives",
            "Grapes", "Wool", "Clay", "Lumber", "Ore", "Silver", "Horse",
            "Olive oil", "Wine", "Cloth", "Amphorae", "Goods", "Tools",
            "Weapons", "Hammers", "Torches", "Owls");

    /** Unchanged goods keep their existing Czech names. */
    private static final List<String> CZECH = List.of("Obilí", "Olivy",
            "Hrozny", "Vlna", "Hlína", "Dříví", "Železná ruda", "Stříbro",
            "Koně", "Olivový olej", "Víno", "Látka", "Amfory", "Tretky",
            "Nářadí", "Zbraně", "Kladiva", "Pochodně", "Sovy");

    private static final List<String> LEGACY_GOODS = List.of("CORN", "SUGAR",
            "TOBACCO", "COTTON", "FUR", "RUM", "CIGARS", "SILK", "COAT",
            "MUSKET", "CROSS", "BELL");

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    @Test
    void allNineteenGoodsKeepTheirOrderUnderUniqueCurrentIds() {
        final List<String> names = GoodsType.GOOD_TYPES.stream()
                .map(GoodsType::name).collect(Collectors.toList());
        assertEquals(IDS, names);
        assertEquals(19, new HashSet<>(names).size());
        final Set<String> notBuyable = GoodsType.GOOD_TYPES.stream()
                .filter(type -> !type.isCanBeBought()).map(GoodsType::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("HAMMERS", "TORCHES", "OWLS"), notBuyable);
    }

    @Test
    void englishAndCzechNamesMatchTheMapping() {
        final I18n english = i18n(Locale.ROOT);
        final I18n czech = i18n(new Locale("cs", "CZ"));
        for (int i = 0; i < IDS.size(); i++) {
            final GoodsType type = GoodsType.valueOf(IDS.get(i));
            final GoodsTypeName key = GoodsTypeName.getNameForGoodsType(type);
            assertEquals(ENGLISH.get(i), english.get(key), type.name());
            assertEquals(CZECH.get(i), czech.get(key), type.name());
        }
    }

    @Test
    void savedGoodsUseCurrentIdsAndRejectLegacyNames() {
        final Gson gson = new GsonBuilder().registerTypeAdapter(GoodsType.class,
                new GsonGoodsTypeAdapter()).create();
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            final String json = gson.toJson(type, GoodsType.class);
            assertEquals('"' + type.name() + '"', json);
            assertEquals(type, gson.fromJson(json, GoodsType.class));
        }
        for (final String legacy : LEGACY_GOODS) {
            assertThrows(IllegalArgumentException.class,
                    () -> GoodsType.valueOf(legacy), legacy);
            assertThrows(IllegalArgumentException.class,
                    () -> gson.fromJson('"' + legacy + '"', GoodsType.class),
                    legacy);
        }
    }

    @Test
    void renamedProfessionsBuildingsAndResourcesHaveNoLegacyIds() {
        for (final String legacy : List.of("EXPERT_FUR_TRAPPER",
                "MASTER_SUGAR_PLANTER", "MASTER_COTTON_PLANTER",
                "MASTER_TOBACCO_PLANTER", "MASTER_DISTILLER",
                "MASTER_TOBACCONIST", "MASTER_FUR_TRADER", "MASTER_GUNSMITH")) {
            assertThrows(IllegalArgumentException.class,
                    () -> UnitType.valueOf(legacy), legacy);
        }
        for (final String legacy : List.of("TOBACCONISTS_HOUSE",
                "TOBACCONISTS_SHOP", "CIGAR_FACTORY", "RUM_DISTILLERS_HOUSE",
                "RUM_DISTILLERY", "RUM_FACTORY", "FUR_TRADERS_HOUSE",
                "FUR_TRADING_POST", "FUR_FACTORY")) {
            assertThrows(IllegalArgumentException.class,
                    () -> ConstructionType.valueOf(legacy), legacy);
        }
        for (final String legacy : List.of("TRAPPING", "PRIME_COTTON",
                "PRIME_TOBACCO", "PRIME_SUGAR")) {
            assertThrows(IllegalArgumentException.class,
                    () -> ResourceOverlay.valueOf(legacy), legacy);
        }
        assertEquals(GoodsType.OLIVES,
                UnitType.MASTER_OLIVE_GROWER.getExpertInProducing());
    }

}
