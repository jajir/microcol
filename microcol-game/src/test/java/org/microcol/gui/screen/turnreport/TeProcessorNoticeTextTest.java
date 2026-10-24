package org.microcol.gui.screen.turnreport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;
import org.microcol.i18n.I18n;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.UnitType;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventGoodsWasThrownAway;
import org.microcol.model.turnevent.TurnEventNewUnitInColony;
import org.microcol.model.turnevent.TurnEventProjectCompleted;

/**
 * The completed-project, overflow and birth notices name their colony and
 * object in both languages and offer to open the colony. Each event is built
 * from its saved form, so a reloaded event reads the same.
 */
class TeProcessorNoticeTextTest {

    private static final Locale CZECH = new Locale("cs", "CZ");

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    private static TurnEventPo colonyPo(final String type) {
        final TurnEventPo po = new TurnEventPo();
        po.setType(type);
        po.setPlayerName("Dutch");
        po.setColonyId(7);
        po.setColonyName("Delft");
        return po;
    }

    private static TurnEvent constructionDone() {
        final TurnEventPo po = colonyPo(
                TurnEventProjectCompleted.class.getSimpleName());
        po.setProjectKind(QueueItemType.CONSTRUCTION);
        po.setProjectConstructionType(ConstructionType.BLACKSMITHS_SHOP);
        return TurnEventProjectCompleted.tryLoad(po);
    }

    private static TurnEvent unitDone() {
        final TurnEventPo po = colonyPo(
                TurnEventProjectCompleted.class.getSimpleName());
        po.setProjectKind(QueueItemType.UNIT);
        po.setProjectUnitType(UnitType.WAGON);
        return TurnEventProjectCompleted.tryLoad(po);
    }

    private static TurnEvent thrownAway(final GoodsType type,
            final int amount) {
        final TurnEventPo po = colonyPo(
                TurnEventGoodsWasThrownAway.class.getSimpleName());
        po.setGoods(Goods.of(type, amount));
        return TurnEventGoodsWasThrownAway.tryLoad(po);
    }

    private static void verifyOpensColony(final TeItemSimple item) {
        assertTrue(item instanceof TeItemColony);
        assertEquals(7, ((TeItemColony) item).getColonyId());
    }

    @Test
    void finishedConstructionNamesColonyAndProjectInBothLanguages() {
        final TeItemSimple english = new TeProcessorProjectCompleted(
                i18n(Locale.ROOT)).apply(constructionDone());
        assertEquals("Colony Delft finished Blacksmith shop.",
                english.getMessage());
        verifyOpensColony(english);

        final TeItemSimple czech = new TeProcessorProjectCompleted(i18n(CZECH))
                .apply(constructionDone());
        assertEquals("Kolonie Delft dokončila Kovářství.", czech.getMessage());
        verifyOpensColony(czech);
    }

    @Test
    void finishedUnitNamesColonyAndUnitInBothLanguages() {
        final TeItemSimple english = new TeProcessorProjectCompleted(
                i18n(Locale.ROOT)).apply(unitDone());
        assertEquals("Colony Delft finished Wagon.", english.getMessage());
        verifyOpensColony(english);

        final TeItemSimple czech = new TeProcessorProjectCompleted(i18n(CZECH))
                .apply(unitDone());
        assertEquals("Kolonie Delft dokončila Povoz.", czech.getMessage());
        verifyOpensColony(czech);
    }

    @Test
    void processorIgnoresOtherNotices() {
        assertNull(new TeProcessorProjectCompleted(i18n(Locale.ROOT))
                .apply(thrownAway(GoodsType.ORE, 3)));
    }

    @Test
    void thrownAwayGoodsNameColonyGoodsAndQuantityInBothLanguages() {
        final TeItemSimple english = new TeProcessorGoodsWasThrownAway(
                i18n(Locale.ROOT)).apply(thrownAway(GoodsType.TOBACCO, 4));
        assertEquals(
                "Colony Delft: not enough warehouse space, 4 units of Tobacco were thrown away.",
                english.getMessage());
        verifyOpensColony(english);

        final TeItemSimple czech = new TeProcessorGoodsWasThrownAway(
                i18n(CZECH)).apply(thrownAway(GoodsType.TOBACCO, 4));
        assertEquals(
                "V kolonii Delft došlo místo, proto se muselo 4 kusů tabáku vyhodit.",
                czech.getMessage());
        verifyOpensColony(czech);
    }

    @Test
    void oneThrownAwayUnitReadsTheSameForEveryGrammaticalGender() {
        final I18n english = i18n(Locale.ROOT);
        for (final GoodsType type : new GoodsType[] { GoodsType.TOBACCO,
                GoodsType.ORE, GoodsType.CORN }) {
            final String goods = english
                    .get(org.microcol.gui.GoodsTypeName.getKey(type, 2, false));
            assertEquals(
                    "Colony Delft: not enough warehouse space, 1 unit of "
                            + goods + " was thrown away.",
                    new TeProcessorGoodsWasThrownAway(english)
                            .apply(thrownAway(type, 1)).getMessage());
        }
    }

    @Test
    void birthNamesTheColonyAndOpensIt() {
        final TurnEventPo po = colonyPo(
                TurnEventNewUnitInColony.class.getSimpleName());
        final TurnEvent birth = TurnEventNewUnitInColony.tryLoad(po);

        final TeItemSimple english = new TeProcessorNewUnitInColony(
                i18n(Locale.ROOT)).apply(birth);
        assertEquals("There is a new unit in colony Delft.",
                english.getMessage());
        verifyOpensColony(english);

        final TeItemSimple czech = new TeProcessorNewUnitInColony(i18n(CZECH))
                .apply(birth);
        assertEquals("V kolonii Delft se objevila nová jednotka.",
                czech.getMessage());
        verifyOpensColony(czech);
    }

}
