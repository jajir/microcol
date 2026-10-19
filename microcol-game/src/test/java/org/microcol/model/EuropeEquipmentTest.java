package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFreeColonist.EquipmentProblem;

/** Pier equipment uses the shared market without replacing the colonist. */
class EuropeEquipmentTest {
    private final Model model = mock(Model.class);
    private Europe europe;
    private Player player;
    private UnitFreeColonist unit;

    @BeforeEach
    void prepareCurrentPlayerAndPierColonist() {
        europe = new Europe(model);
        when(model.getEurope()).thenReturn(europe);
        player = new PlayerHuman("Dutch", false, 1000, model, false,
                new HashMap<>(), new HashSet<>());
        unit = makeUnit(player);
        when(model.isGameRunning()).thenReturn(true);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(model.getUnitsOwnedBy(player, true)).thenReturn(List.of(unit));
    }

    private UnitFreeColonist makeUnit(Player owner) {
        return new UnitFreeColonist(model, 4, PlaceEuropePier::new, owner,
                MovementPoints.ofPoints(1), new UnitActionNoAction(), 0, false,
                false);
    }

    static Stream<Arguments> equipmentLots() {
        return Stream.of(Arguments.of(GoodsType.MUSKET, 50, 150, 100),
                Arguments.of(GoodsType.HORSE, 50, 250, 100),
                Arguments.of(GoodsType.TOOLS, 100, 200, 100));
    }

    @ParameterizedTest
    @MethodSource("equipmentLots")
    void purchaseAndSaleRetainIdentityAndPlace(GoodsType type, int amount,
            int purchasePrice, int salePrice) {
        final Place originalPlace = unit.getPlace();
        final EuropeTradeQuote offer = unit.getEuropeEquipmentQuote(type, true);
        assertEquals(Goods.of(type, amount), offer.getGoods());
        assertEquals(purchasePrice, offer.getTotal());
        assertEquals(Optional.empty(),
                unit.getEuropeEquipmentProblem(type, true));
        assertEquals(1000, player.getGold());
        assertEquals(0, equipment(type));
        unit.tradeEquipmentInEurope(type, true, offer);
        assertEquals(amount, equipment(type));
        assertEquals(1000 - purchasePrice, player.getGold());
        unit.tradeEquipmentInEurope(type, false,
                unit.getEuropeEquipmentQuote(type, false));
        assertEquals(0, equipment(type));
        assertEquals(1000 - purchasePrice + salePrice, player.getGold());
        assertSame(originalPlace, unit.getPlace());
        assertSame(player, unit.getOwner());
        assertEquals(4, unit.getId());
        assertEquals(UnitType.COLONIST, unit.getType());
        assertEquals(1, unit.getActionPoints());
    }

    @Test
    void dragoonEquipmentCanBeSoldSeparately() {
        buy(GoodsType.MUSKET);
        buy(GoodsType.HORSE);
        assertTrue(unit.isHoldingGuns());
        assertTrue(unit.isMounted());
        assertEquals(600, player.getGold());
        unit.tradeEquipmentInEurope(GoodsType.MUSKET, false,
                unit.getEuropeEquipmentQuote(GoodsType.MUSKET, false));
        assertFalse(unit.isHoldingGuns());
        assertTrue(unit.isMounted());
        assertEquals(700, player.getGold());
        assertEquals(0, unit.getTools());
        assertTrue(unit.save().isMounted());
        assertFalse(unit.save().isHoldingGuns());
    }

    @Test
    void partiallyUsedToolsMustBeSoldBeforeBuyingFreshSupply() {
        unit.setTools(37);
        final EuropeTradeQuote purchase = unit
                .getEuropeEquipmentQuote(GoodsType.TOOLS, true);
        assertRejected(GoodsType.TOOLS, true, purchase,
                EquipmentProblem.ALREADY_SET);
        when(model.getKingsTaxForPlayer(player)).thenReturn(17);
        final EuropeTradeQuote sale = unit
                .getEuropeEquipmentQuote(GoodsType.TOOLS, false);
        assertEquals(37, sale.getGoods().getAmount());
        assertEquals(30, sale.getTotal());
        unit.tradeEquipmentInEurope(GoodsType.TOOLS, false, sale);
        assertEquals(1030, player.getGold());
        assertEquals(0, unit.getTools());
        buy(GoodsType.TOOLS);
        assertEquals(100, unit.getTools());
        assertEquals(796, player.getGold());
    }

    @ParameterizedTest
    @MethodSource("equipmentLots")
    void repeatedOperationsCannotChargeOrCreditAgain(GoodsType type, int amount,
            int purchasePrice, int salePrice) {
        final EuropeTradeQuote buyOffer = unit.getEuropeEquipmentQuote(type,
                true);
        unit.tradeEquipmentInEurope(type, true, buyOffer);
        assertRejected(type, true, buyOffer, EquipmentProblem.ALREADY_SET);
        final EuropeTradeQuote sellOffer = unit.getEuropeEquipmentQuote(type,
                false);
        unit.tradeEquipmentInEurope(type, false, sellOffer);
        assertRejected(type, false, sellOffer, EquipmentProblem.ALREADY_SET);
    }

    @ParameterizedTest
    @MethodSource("equipmentLots")
    void incompatibleEquipmentIsNeverDiscarded(GoodsType type, int amount,
            int purchasePrice, int salePrice) {
        if (type == GoodsType.TOOLS) {
            unit.setMounted(true);
            unit.setHoldingGuns(true);
        } else {
            unit.setTools(37);
        }
        assertRejected(type, true, unit.getEuropeEquipmentQuote(type, true),
                EquipmentProblem.INCOMPATIBLE_EQUIPMENT);
    }

    @Test
    void exactTaxedFundsWorkButOneGoldShortDoesNot() {
        when(model.getKingsTaxForPlayer(player)).thenReturn(17);
        player.setGold(174);
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        assertEquals(175, offer.getTotal());
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.NOT_ENOUGH_GOLD);
        player.setGold(175);
        unit.tradeEquipmentInEurope(GoodsType.MUSKET, true, offer);
        assertEquals(0, player.getGold());
        assertTrue(unit.isHoldingGuns());
    }

    @Test
    void priceChangeRejectsOldOfferUntilNewOfferIsConfirmed() {
        final EuropeTradeQuote oldOffer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        europe.getGoodsTradeForType(GoodsType.MUSKET).setBuyPrice(4);
        assertRejected(GoodsType.MUSKET, true, oldOffer, null);
        buy(GoodsType.MUSKET);
        assertEquals(800, player.getGold());
        assertTrue(unit.isHoldingGuns());
    }

    @Test
    void equalRoundedTotalsStillRejectChangedTaxOrQuantity() {
        unit.setTools(1);
        when(model.getKingsTaxForPlayer(player)).thenReturn(1);
        final EuropeTradeQuote oldOffer = unit
                .getEuropeEquipmentQuote(GoodsType.TOOLS, false);
        when(model.getKingsTaxForPlayer(player)).thenReturn(2);
        assertEquals(0, oldOffer.getTotal());
        assertEquals(0, unit.getEuropeEquipmentQuote(GoodsType.TOOLS, false)
                .getTotal());
        assertRejected(GoodsType.TOOLS, false, oldOffer, null);
        unit.setTools(37);
        final EuropeTradeQuote quantityOffer = unit
                .getEuropeEquipmentQuote(GoodsType.TOOLS, false);
        unit.setTools(36);
        assertRejected(GoodsType.TOOLS, false, quantityOffer, null);
    }

    @Test
    void foreignTurnRejectsWithoutMutatingTheOwner() {
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        final PlayerHuman french = new PlayerHuman("French", false, 1000, model,
                false, new HashMap<>(), new HashSet<>());
        when(model.getCurrentPlayer()).thenReturn(french);
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.UNIT_UNAVAILABLE);
    }

    @Test
    void leavingPierInvalidatesAnOpenOffer() {
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        unit.placeToLocation(Location.of(3, 3));
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.UNIT_UNAVAILABLE);
    }

    @Test
    void removedOrReplacedObjectCannotUseItsOldIdentity() {
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        when(model.getUnitsOwnedBy(player, true))
                .thenReturn(List.of(makeUnit(player)));
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.UNIT_UNAVAILABLE);
        when(model.getUnitsOwnedBy(player, true)).thenReturn(List.of());
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.UNIT_UNAVAILABLE);
    }

    @Test
    void stoppedGameRejectsTrade() {
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        when(model.isGameRunning()).thenReturn(false);
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.UNIT_UNAVAILABLE);
    }

    @Test
    void independenceClosesBothEquipmentTradeDirections() {
        player = new PlayerHuman("Dutch", false, 1000, model, true,
                new HashMap<>(), new HashSet<>());
        unit = makeUnit(player);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(model.getUnitsOwnedBy(player, true)).thenReturn(List.of(unit));
        assertRejected(GoodsType.MUSKET, true,
                unit.getEuropeEquipmentQuote(GoodsType.MUSKET, true),
                EquipmentProblem.TRADE_RESTRICTED);
        unit.setHoldingGuns(true);
        assertRejected(GoodsType.MUSKET, false,
                unit.getEuropeEquipmentQuote(GoodsType.MUSKET, false),
                EquipmentProblem.TRADE_RESTRICTED);
    }

    @Test
    void saleWouldOverflowTreasuryAndIsRejectedAtomically() {
        unit.setTools(37);
        player.setGold(Integer.MAX_VALUE);
        assertRejected(GoodsType.TOOLS, false,
                unit.getEuropeEquipmentQuote(GoodsType.TOOLS, false),
                EquipmentProblem.GOLD_LIMIT);
    }

    @Test
    void overflowingQuoteIsReportedBeforeEquipmentMutation() {
        final EuropeTradeQuote offer = unit
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        europe.getGoodsTradeForType(GoodsType.MUSKET)
                .setBuyPrice(Integer.MAX_VALUE);
        assertRejected(GoodsType.MUSKET, true, offer,
                EquipmentProblem.GOLD_LIMIT);
    }

    @Test
    void unsupportedTypeAndNullOfferDoNotChangeState() {
        assertThrows(IllegalArgumentException.class,
                () -> unit.getEuropeEquipmentQuote(GoodsType.CORN, true));
        assertThrows(IllegalArgumentException.class,
                () -> unit.getEuropeEquipmentProblem(GoodsType.CORN, true));
        assertThrows(NullPointerException.class, () -> unit
                .tradeEquipmentInEurope(GoodsType.MUSKET, true, null));
        assertEquals(1000, player.getGold());
        assertFalse(unit.isHoldingGuns());
    }

    private void buy(GoodsType type) {
        unit.tradeEquipmentInEurope(type, true,
                unit.getEuropeEquipmentQuote(type, true));
    }

    private int equipment(GoodsType type) {
        return type == GoodsType.TOOLS ? unit.getTools()
                : type == GoodsType.MUSKET ? (unit.isHoldingGuns() ? 50 : 0)
                        : (unit.isMounted() ? 50 : 0);
    }

    private void assertRejected(GoodsType type, boolean buy,
            EuropeTradeQuote offer, EquipmentProblem expectedProblem) {
        final int gold = player.getGold();
        final int tools = unit.getTools();
        final boolean guns = unit.isHoldingGuns();
        final boolean horses = unit.isMounted();
        final Place place = unit.getPlace();
        assertEquals(Optional.ofNullable(expectedProblem),
                unit.getEuropeEquipmentProblem(type, buy));
        assertThrows(IllegalStateException.class,
                () -> unit.tradeEquipmentInEurope(type, buy, offer));
        assertEquals(gold, player.getGold());
        assertEquals(tools, unit.getTools());
        assertEquals(guns, unit.isHoldingGuns());
        assertEquals(horses, unit.isMounted());
        assertSame(place, unit.getPlace());
        assertSame(player, unit.getOwner());
        assertEquals(4, unit.getId());
        assertEquals(1, unit.getActionPoints());
    }
}
