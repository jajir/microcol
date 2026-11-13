package org.akropia.gui.screen.turnreport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.ConstructionType;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.UnitType;
import org.akropia.model.store.QueueItemType;
import org.akropia.model.store.TurnEventPo;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventGoodsWasThrownAway;
import org.akropia.model.turnevent.TurnEventNewUnitInColony;
import org.akropia.model.turnevent.TurnEventProjectCompleted;
import org.akropia.model.turnevent.TurnEventTradeRouteTransfer;

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
                i18n(Locale.ROOT)).apply(thrownAway(GoodsType.GRAPES, 4));
        assertEquals(
                "Colony Delft: not enough warehouse space, 4 units of Grapes were thrown away.",
                english.getMessage());
        verifyOpensColony(english);

        final TeItemSimple czech = new TeProcessorGoodsWasThrownAway(
                i18n(CZECH)).apply(thrownAway(GoodsType.GRAPES, 4));
        assertEquals(
                "V kolonii Delft došlo místo, proto se muselo 4 kusů hroznů vyhodit.",
                czech.getMessage());
        verifyOpensColony(czech);
    }

    @Test
    void oneThrownAwayUnitReadsTheSameForEveryGrammaticalGender() {
        final I18n english = i18n(Locale.ROOT);
        for (final GoodsType type : new GoodsType[] { GoodsType.GRAPES,
                GoodsType.ORE, GoodsType.GRAIN }) {
            final String goods = english
                    .get(org.akropia.gui.GoodsTypeName.getKey(type, 2, false));
            assertEquals(
                    "Colony Delft: not enough warehouse space, 1 unit of "
                            + goods + " was thrown away.",
                    new TeProcessorGoodsWasThrownAway(english)
                            .apply(thrownAway(type, 1)).getMessage());
        }
    }

    private static TurnEvent routeTransfer(final boolean europe,
            final boolean unload, final Goods goods, final int gold) {
        final TurnEventPo po = new TurnEventPo();
        po.setType(TurnEventTradeRouteTransfer.class.getSimpleName());
        po.setPlayerName("Dutch");
        po.setTradeRouteId(2);
        po.setTradeRouteName("Oil Run");
        po.setTradeRouteCarrierId(3);
        po.setTradeRouteCarrierType(UnitType.GALLEON);
        po.setTradeRouteStopIndex(europe ? 0 : 1);
        if (!europe) {
            po.setColonyId(7);
            po.setColonyName("Delft");
        }
        po.setTradeRouteUnload(unload);
        po.setGoods(goods);
        po.setTradeRouteGold(gold);
        return TurnEventTradeRouteTransfer.tryLoad(po);
    }

    private static String routeText(final Locale locale,
            final TurnEvent event) {
        return new TeProcessorTradeRouteTransfer(i18n(locale)).apply(event)
                .getMessage();
    }

    @Test
    void routeColonyTransfersNameRouteCarrierStopAndQuantity() {
        final TurnEvent unloaded = routeTransfer(false, true,
                Goods.of(GoodsType.ORE, 30), 0);
        final TurnEvent loaded = routeTransfer(false, false,
                Goods.of(GoodsType.HORSE, 20), 0);
        assertEquals(
                "Trade route Oil Run, Galleon #3, stop 2 (Delft): unloaded 30 Ore.",
                routeText(Locale.ROOT, unloaded));
        assertEquals(
                "Trade route Oil Run, Galleon #3, stop 2 (Delft): loaded 20 Horse.",
                routeText(Locale.ROOT, loaded));
        assertEquals(
                "Obchodní trasa Oil Run, galeona č. 3, zastávka 2 (Delft): vyloženo 30 × Železná ruda.",
                routeText(CZECH, unloaded));
        assertEquals(
                "Obchodní trasa Oil Run, galeona č. 3, zastávka 2 (Delft): naloženo 20 × Koně.",
                routeText(CZECH, loaded));
    }

    @Test
    void routeEuropeTradesShowTheSignedTreasuryChange() {
        final TurnEvent sold = routeTransfer(true, true,
                Goods.of(GoodsType.ORE, 60), 120);
        final TurnEvent bought = routeTransfer(true, false,
                Goods.of(GoodsType.WEAPONS, 40), -50);
        assertEquals(
                "Trade route Oil Run, Galleon #3, stop 1 (Home port): sold 60 Ore, +120 gold.",
                routeText(Locale.ROOT, sold));
        assertEquals(
                "Trade route Oil Run, Galleon #3, stop 1 (Home port): bought 40 Weapons, -50 gold.",
                routeText(Locale.ROOT, bought));
        assertEquals(
                "Obchodní trasa Oil Run, galeona č. 3, zastávka 1 (Domovský přístav): prodáno 60 × Železná ruda, +120 zlata.",
                routeText(CZECH, sold));
        assertEquals(
                "Obchodní trasa Oil Run, galeona č. 3, zastávka 1 (Domovský přístav): nakoupeno 40 × Zbraně, -50 zlata.",
                routeText(CZECH, bought));
        assertNull(new TeProcessorTradeRouteTransfer(i18n(Locale.ROOT))
                .apply(thrownAway(GoodsType.ORE, 3)));
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

    @Test
    void travelAndRepairLinksRetainDestinationIdentityAfterSave() {
        final I18n language = i18n(Locale.ROOT);
        final var colonyArrival = new org.akropia.model.turnevent.TurnEventGoTo(
                "Dutch", org.akropia.model.turnevent.TurnEventGoTo.Kind.ARRIVED,
                3, UnitType.MERCHANTMAN, "Delft").destination(7, false);
        verifyOpensColony(new TeProcessorGoTo(language)
                .apply(org.akropia.model.turnevent.TurnEventGoTo
                        .tryLoad(colonyArrival.save())));
        final var homeArrival = new org.akropia.model.turnevent.TurnEventGoTo(
                "Dutch",
                org.akropia.model.turnevent.TurnEventGoTo.Kind.ARRIVED_EUROPE,
                3, UnitType.MERCHANTMAN, "Home port").destination(null, true);
        assertTrue(new TeProcessorGoTo(language)
                .apply(org.akropia.model.turnevent.TurnEventGoTo
                        .tryLoad(homeArrival.save())) instanceof TeItemEurope);
        final var repair = new org.akropia.model.turnevent.TurnEventShipRepair(
                "Dutch",
                org.akropia.model.turnevent.TurnEventShipRepair.Kind.COMPLETED,
                3, UnitType.MERCHANTMAN, "Delft", false, 0, java.util.List.of())
                .atColony(7);
        verifyOpensColony(new TeProcessorShipRepair(language)
                .apply(org.akropia.model.turnevent.TurnEventShipRepair
                        .tryLoad(repair.save())));
    }

    @Test
    void colonyAndHomePortNoticesOfferTheirDestination() {
        final I18n language = i18n(Locale.ROOT);
        final var famine = org.mockito.Mockito.mock(
                org.akropia.model.turnevent.TurnEventFamineWillPlagueColony.class);
        org.mockito.Mockito.when(famine.getColonyId()).thenReturn(7);
        org.mockito.Mockito.when(famine.getColonyName()).thenReturn("Delft");
        verifyOpensColony(
                new TeProcessorFamineWillPlagueColony(language).apply(famine));
        final var mercenary = colonyPo("TurnEventMercenaryArrival");
        verifyOpensColony(new TeProcessorMercenaryArrival(language)
                .apply(org.akropia.model.turnevent.TurnEventMercenaryArrival
                        .tryLoad(mercenary)));
        final var intervention = colonyPo("TurnEventInterventionArrival");
        verifyOpensColony(new TeProcessorInterventionArrival(language)
                .apply(org.akropia.model.turnevent.TurnEventInterventionArrival
                        .tryLoad(intervention)));
    }

    @Test
    void fishermanNoticeRejectsInvalidSavedColonyIdentity() {
        final TurnEventPo po = colonyPo("TurnEventFishermanLost");
        po.setFishermanUnitId(3);
        po.setFishermanLossReason("NO_SAFE_LAND");
        po.setClearedX(2);
        po.setClearedY(3);
        po.setColonyId(-1);
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> org.akropia.model.turnevent.TurnEventFishermanLost
                        .tryLoad(po));
    }

}
