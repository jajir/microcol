package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Foreign colony trade (R-0167) on the free-play map. Dutch carriers stand
 * beside the French colony Quebec; Jan de Witt, the peace or alliance, Europe
 * closure and the carried goods vary per test. The approach square east of
 * Quebec is made open sea, as in the port tests, so that a ship can stand
 * beside the colony. Expected gold comes from the Europe prices. Warehouse
 * fixtures never exceed the real storage capacity, and every refused offer must
 * leave carriers, warehouse, treasuries, position and ownership unchanged.
 *
 * <p>
 * Hold arithmetic (a wagon has {@code WAGON_HOLDS} holds of
 * {@link CargoSlot#MAX_CARGO_SLOT_CAPACITY}): the goods given leave in slot
 * order, so only a hold they empty completely is free for the goods received; a
 * hold that keeps some of them, a hold of other goods and a hold taken by a
 * unit give no room.
 * </p>
 */
class ForeignColonyTradeTest {

    private static final String MAP = "/maps/free-play.microcol";
    private static final Location QUEBEC = Location.of(17, 10);
    /** Open sea east of Quebec, made explicit for the ship. */
    private static final Location APPROACH = Location.of(18, 10);
    private static final int WAGON_ID = 900;
    private static final int SHIP_ID = 901;
    private static final int FAR_WAGON_ID = 902;
    private static final int TRAIN_ID = 903;
    private static final int START_GOLD = 1000;
    private static final int RICH = 10_000_000;
    private static final int HOLD = CargoSlot.MAX_CARGO_SLOT_CAPACITY;
    private static final int WAGON_HOLDS = UnitType.WAGON.getCargoCapacity();
    private static final int GALLEON_HOLDS = UnitType.GALLEON
            .getCargoCapacity();

    @Test
    void sale_moves_goods_into_the_colony_and_pays_the_bid_without_home_tax() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 5);
        final Colony quebec = quebec(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final int bid = bid(model, GoodsType.CORN);
        final int dutchGold = dutch.getGold();
        final int franceGold = france.getGold();
        final int stock = stock(quebec, GoodsType.CORN);
        final Location where = wagon.getLocation();
        final int moves = wagon.getMovementThirds();

        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 5));
        assertTrue(quote.isLegal());
        assertTrue(quote.isSale());
        assertEquals(WAGON_ID, quote.getVisitorId());
        assertEquals(quebec.getId(), quote.getColonyId());
        assertEquals(bid, quote.getIncomingUnitPrice());
        assertEquals(5 * bid, quote.getNetGold());
        model.tradeWithForeignColony(wagon, quebec, quote);

        assertEquals(dutchGold + 5 * bid, dutch.getGold());
        assertEquals(franceGold - 5 * bid, france.getGold());
        assertEquals(0, carried(wagon, GoodsType.CORN));
        assertEquals(stock + 5, stock(quebec, GoodsType.CORN));
        // The visit is no movement, attack or capture.
        assertEquals(where, wagon.getLocation());
        assertEquals(moves, wagon.getMovementThirds());
        assertEquals(france, quebec.getOwner());
        // Repeating the confirmed offer finds the carrier already emptied.
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void barter_gold_is_paid_by_the_colony_when_its_incoming_goods_are_worth_more() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Colony quebec = quebec(model);
        final int bid = bid(model, GoodsType.CORN);
        final int ask = ask(model, GoodsType.TOBACCO);
        // Enough corn that its bid total exceeds one piece of tobacco's ask.
        final int incoming = ask / bid + 1;
        load(wagon, GoodsType.CORN, incoming);
        stockUp(quebec, GoodsType.TOBACCO, 4);
        final int tobacco = stock(quebec, GoodsType.TOBACCO);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final int dutchGold = dutch.getGold();
        final int franceGold = france.getGold();
        final int net = incoming * bid - ask;

        final ForeignColonyTradeQuote quote = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.CORN, incoming),
                Goods.of(GoodsType.TOBACCO, 1));
        assertTrue(quote.isLegal());
        assertTrue(net > 0);
        assertEquals(net, quote.getNetGold());
        model.tradeWithForeignColony(wagon, quebec, quote);

        assertEquals(dutchGold + net, dutch.getGold());
        assertEquals(franceGold - net, france.getGold());
        assertEquals(dutchGold + franceGold,
                dutch.getGold() + france.getGold());
        assertEquals(0, carried(wagon, GoodsType.CORN));
        assertEquals(1, carried(wagon, GoodsType.TOBACCO));
        assertEquals(tobacco - 1, stock(quebec, GoodsType.TOBACCO));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void barter_gold_is_paid_by_the_visitor_when_its_outgoing_goods_are_worth_more() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Colony quebec = quebec(model);
        final int bid = bid(model, GoodsType.CORN);
        final int ask = ask(model, GoodsType.TOBACCO);
        final int outgoing = bid / ask + 1;
        load(wagon, GoodsType.CORN, 1);
        stockUp(quebec, GoodsType.TOBACCO, outgoing + 3);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final int dutchGold = dutch.getGold();
        final int franceGold = france.getGold();
        final int net = bid - outgoing * ask;

        final ForeignColonyTradeQuote quote = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.CORN, 1),
                Goods.of(GoodsType.TOBACCO, outgoing));
        assertTrue(quote.isLegal());
        assertTrue(net < 0);
        assertEquals(net, quote.getNetGold());
        model.tradeWithForeignColony(wagon, quebec, quote);

        assertEquals(dutchGold + net, dutch.getGold());
        assertEquals(franceGold - net, france.getGold());
        assertEquals(outgoing, carried(wagon, GoodsType.TOBACCO));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void founder_absent_refuses_the_trade_without_effects() {
        final Model model = setup(false, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 3);
        final Colony quebec = quebec(model);

        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 3));
        assertEquals(ForeignColonyTradeProblem.FOUNDER_ABSENT,
                quote.getProblem());
        assertEquals(ForeignColonyTradeProblem.FOUNDER_ABSENT,
                model.getForeignColonyTradeAccess(wagon, quebec));
        assertEquals(0, model.getForeignColonyTradeLimit(wagon, quebec,
                GoodsType.CORN, true));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void war_refuses_the_trade_and_alliance_permits_it() {
        final Model war = setup(true, EuropeanRelationState.WAR, false);
        final Unit warWagon = war.getUnitById(WAGON_ID);
        load(warWagon, GoodsType.CORN, 2);
        final ForeignColonyTradeQuote refused = war.quoteForeignColonySale(
                warWagon, quebec(war), Goods.of(GoodsType.CORN, 2));
        assertEquals(ForeignColonyTradeProblem.AT_WAR, refused.getProblem());
        assertRejectedWithoutEffects(war, warWagon, quebec(war), refused);

        final Model allied = setup(true, EuropeanRelationState.ALLIANCE, false);
        final Unit allyWagon = allied.getUnitById(WAGON_ID);
        load(allyWagon, GoodsType.CORN, 2);
        assertTrue(allied.quoteForeignColonySale(allyWagon, quebec(allied),
                Goods.of(GoodsType.CORN, 2)).isLegal());
    }

    @Test
    void loaded_ship_at_sea_trades_like_a_wagon() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit ship = model.getUnitById(SHIP_ID);
        load(ship, GoodsType.CORN, 2);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(ship,
                quebec(model), Goods.of(GoodsType.CORN, 2));
        assertTrue(quote.isLegal());
        model.tradeWithForeignColony(ship, quebec(model), quote);
        assertEquals(0, carried(ship, GoodsType.CORN));
    }

    @Test
    void rebellion_closes_europe_but_keeps_the_foreign_colony_trade() {
        final Model model = setup(true, EuropeanRelationState.PEACE, true);
        final Player dutch = model.getPlayerByName("Dutch");
        assertFalse(dutch.canTradeInEurope());
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 3);
        final Colony quebec = quebec(model);
        final int dutchGold = dutch.getGold();
        final int bid = bid(model, GoodsType.CORN);

        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 3));
        assertTrue(quote.isLegal());
        model.tradeWithForeignColony(wagon, quebec, quote);
        assertEquals(dutchGold + 3 * bid, dutch.getGold());
        assertEquals(0, carried(wagon, GoodsType.CORN));
        // The foreign trade is its own permission; Europe stays closed.
        assertFalse(dutch.canTradeInEurope());
        // The same founder and relation checks still apply under rebellion.
        final Model withoutFounder = setup(false, EuropeanRelationState.PEACE,
                true);
        final Unit other = withoutFounder.getUnitById(WAGON_ID);
        load(other, GoodsType.CORN, 3);
        assertEquals(ForeignColonyTradeProblem.FOUNDER_ABSENT, withoutFounder
                .getForeignColonyTradeAccess(other, quebec(withoutFounder)));
    }

    @Test
    void empty_cargo_and_distant_access_are_refused() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit empty = model.getUnitById(WAGON_ID);
        assertEquals(ForeignColonyTradeProblem.EMPTY_CARGO,
                model.quoteForeignColonySale(empty, quebec(model),
                        Goods.of(GoodsType.CORN, 1)).getProblem());

        final Unit far = model.getUnitById(FAR_WAGON_ID);
        load(far, GoodsType.CORN, 2);
        assertEquals(ForeignColonyTradeProblem.NO_ACCESS,
                model.getForeignColonyTradeAccess(far, quebec(model)));
        assertFalse(model.quoteForeignColonySale(far, quebec(model),
                Goods.of(GoodsType.CORN, 2)).isLegal());
    }

    @Test
    void full_colony_warehouse_refuses_incoming_goods() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.TOBACCO, 1);
        final Colony quebec = quebec(model);
        // Tobacco has a real storage limit; fill the warehouse exactly to it.
        stockUp(quebec, GoodsType.TOBACCO, quebec.getWarehouse()
                .getStorageCapacity(GoodsType.TOBACCO).getAmount());
        assertEquals(0,
                quebec.getWarehouse().getFreeStorage(GoodsType.TOBACCO));
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.TOBACCO, 1));
        assertEquals(ForeignColonyTradeProblem.HOST_CAPACITY,
                quote.getProblem());
        assertEquals(0, model.getForeignColonyTradeLimit(wagon, quebec,
                GoodsType.TOBACCO, true));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void short_colony_stock_refuses_the_barter() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 1);
        final Colony quebec = quebec(model);
        final int tobacco = stock(quebec, GoodsType.TOBACCO);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.CORN, 1),
                Goods.of(GoodsType.TOBACCO, tobacco + 1));
        assertEquals(ForeignColonyTradeProblem.HOST_STOCK, quote.getProblem());
        assertEquals(tobacco,
                model.getForeignColonyStock(quebec, GoodsType.TOBACCO));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void a_full_carrier_refuses_a_barter_that_empties_no_whole_hold() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Colony quebec = quebec(model);
        richTreasuries(model);
        fillWithSugar(wagon);
        clear(quebec, GoodsType.SUGAR);
        stockUp(quebec, GoodsType.CORN, 1000);

        // Half of the first hold leaves: it still holds sugar, so no room.
        assertEquals(0, model.getForeignColonyReceiveLimit(wagon, quebec,
                Goods.of(GoodsType.SUGAR, HOLD / 2), GoodsType.CORN));
        final ForeignColonyTradeQuote partial = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.SUGAR, HOLD / 2),
                Goods.of(GoodsType.CORN, 1));
        assertEquals(ForeignColonyTradeProblem.CARRIER_CAPACITY,
                partial.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, partial);
        // A sale needs no room back, so the same partial hold may be sold.
        assertTrue(model.quoteForeignColonySale(wagon, quebec,
                Goods.of(GoodsType.SUGAR, HOLD / 2)).isLegal());
    }

    @Test
    void giving_a_whole_hold_frees_exactly_that_hold_for_the_received_goods() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Colony quebec = quebec(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        richTreasuries(model);
        fillWithSugar(wagon);
        clear(quebec, GoodsType.SUGAR);
        stockUp(quebec, GoodsType.CORN, 1000);
        final int corn = stock(quebec, GoodsType.CORN);
        final int totalGold = dutch.getGold() + france.getGold();

        assertEquals(HOLD, model.getForeignColonyReceiveLimit(wagon, quebec,
                Goods.of(GoodsType.SUGAR, HOLD), GoodsType.CORN));
        final ForeignColonyTradeQuote tooMuch = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.SUGAR, HOLD),
                Goods.of(GoodsType.CORN, HOLD + 1));
        assertEquals(ForeignColonyTradeProblem.CARRIER_CAPACITY,
                tooMuch.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, tooMuch);

        final ForeignColonyTradeQuote full = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.SUGAR, HOLD),
                Goods.of(GoodsType.CORN, HOLD));
        assertTrue(full.isLegal());
        model.tradeWithForeignColony(wagon, quebec, full);

        // Goods and gold are conserved between the two sides.
        assertEquals((WAGON_HOLDS - 1) * HOLD, carried(wagon, GoodsType.SUGAR));
        assertEquals(HOLD, carried(wagon, GoodsType.CORN));
        assertEquals(HOLD, stock(quebec, GoodsType.SUGAR));
        assertEquals(corn - HOLD, stock(quebec, GoodsType.CORN));
        assertEquals(totalGold, dutch.getGold() + france.getGold());
        assertRejectedWithoutEffects(model, wagon, quebec, full);
    }

    @Test
    void a_partly_filled_hold_of_the_received_goods_adds_only_its_free_part() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Colony quebec = quebec(model);
        richTreasuries(model);
        final List<CargoSlot> holds = ((UnitWithCargo) wagon).getCargo()
                .getSlots();
        holds.get(0).addGoods(Goods.of(GoodsType.CORN, 60));
        holds.get(1).addGoods(Goods.of(GoodsType.SUGAR, HOLD));
        clear(quebec, GoodsType.SUGAR);
        stockUp(quebec, GoodsType.CORN, 1000);

        // 40 free in the corn hold plus the whole hold that the sugar empties.
        final int room = HOLD - 60 + HOLD;
        assertEquals(room, model.getForeignColonyReceiveLimit(wagon, quebec,
                Goods.of(GoodsType.SUGAR, HOLD), GoodsType.CORN));
        assertEquals(ForeignColonyTradeProblem.CARRIER_CAPACITY,
                model.quoteForeignColonyBarter(wagon, quebec,
                        Goods.of(GoodsType.SUGAR, HOLD),
                        Goods.of(GoodsType.CORN, room + 1)).getProblem());
        final ForeignColonyTradeQuote fits = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.SUGAR, HOLD),
                Goods.of(GoodsType.CORN, room));
        assertTrue(fits.isLegal());
        model.tradeWithForeignColony(wagon, quebec, fits);
        assertEquals(60 + room, carried(wagon, GoodsType.CORN));
        assertEquals(0, carried(wagon, GoodsType.SUGAR));
    }

    @Test
    void a_treasure_train_reserves_all_six_holds_and_leaves_no_room_for_goods() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false,
                UnitType.GALLEON, true);
        final UnitWithCargo galleon = (UnitWithCargo) model
                .getUnitById(SHIP_ID);
        final Colony quebec = quebec(model);
        final List<CargoSlot> holds = galleon.getCargo().getSlots();
        assertEquals(GALLEON_HOLDS, holds.size());
        assertEquals(1, holds.stream().filter(CargoSlot::isLoadedUnit).count());
        assertEquals(GALLEON_HOLDS - 1,
                holds.stream().filter(CargoSlot::isReservedHold).count());
        final String before = snapshot(model, galleon, quebec);

        // The train carries no goods, so there is nothing to trade or receive.
        assertEquals(ForeignColonyTradeProblem.EMPTY_CARGO,
                model.getForeignColonyTradeAccess(galleon, quebec));
        final ForeignColonyTradeQuote sale = model.quoteForeignColonySale(
                galleon, quebec, Goods.of(GoodsType.CORN, 1));
        assertEquals(ForeignColonyTradeProblem.EMPTY_CARGO, sale.getProblem());
        assertEquals(0, model.getForeignColonyReceiveLimit(galleon, quebec,
                Goods.of(GoodsType.CORN, 1), GoodsType.TOBACCO));
        assertRejectedWithoutEffects(model, galleon, quebec, sale);
        assertEquals(before, snapshot(model, galleon, quebec));
        assertEquals(1, holds.stream().filter(CargoSlot::isLoadedUnit).count());
        assertEquals(GALLEON_HOLDS - 1,
                holds.stream().filter(CargoSlot::isReservedHold).count());
    }

    @Test
    void without_the_train_a_galleon_offers_all_six_holds_to_the_received_goods() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false,
                UnitType.GALLEON, false);
        final Unit galleon = model.getUnitById(SHIP_ID);
        final Colony quebec = quebec(model);
        load(galleon, GoodsType.SUGAR, 1);
        clear(quebec, GoodsType.SUGAR);
        stockUp(quebec, GoodsType.CORN, GALLEON_HOLDS * HOLD);
        // Giving the single sugar empties the first hold; all six holds fit.
        assertEquals(GALLEON_HOLDS * HOLD, model.getForeignColonyReceiveLimit(
                galleon, quebec, Goods.of(GoodsType.SUGAR, 1), GoodsType.CORN));
    }

    @Test
    void short_treasury_on_either_side_refuses_without_effects() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 1);
        final Colony quebec = quebec(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        france.setGold(0);
        final ForeignColonyTradeQuote hostShort = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 1));
        assertEquals(ForeignColonyTradeProblem.HOST_GOLD,
                hostShort.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, hostShort);

        france.setGold(START_GOLD);
        dutch.setGold(0);
        stockUp(quebec, GoodsType.TOBACCO, 100);
        final int outgoing = bid(model, GoodsType.CORN)
                / ask(model, GoodsType.TOBACCO) + 1;
        final ForeignColonyTradeQuote visitorShort = model
                .quoteForeignColonyBarter(wagon, quebec,
                        Goods.of(GoodsType.CORN, 1),
                        Goods.of(GoodsType.TOBACCO, outgoing));
        assertEquals(ForeignColonyTradeProblem.VISITOR_GOLD,
                visitorShort.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, visitorShort);
        assertEquals(0, dutch.getGold());
        assertEquals(START_GOLD, france.getGold());
    }

    @Test
    void gold_overflow_of_either_treasury_is_refused() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 1);
        final Colony quebec = quebec(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        // The visitor would receive gold beyond the limit.
        dutch.setGold(Integer.MAX_VALUE);
        final ForeignColonyTradeQuote visitorFull = model
                .quoteForeignColonySale(wagon, quebec,
                        Goods.of(GoodsType.CORN, 1));
        assertEquals(ForeignColonyTradeProblem.VISITOR_GOLD,
                visitorFull.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, visitorFull);

        // The colony owner would receive gold beyond the limit.
        dutch.setGold(START_GOLD);
        france.setGold(Integer.MAX_VALUE);
        stockUp(quebec, GoodsType.TOBACCO, 100);
        final int outgoing = bid(model, GoodsType.CORN)
                / ask(model, GoodsType.TOBACCO) + 1;
        final ForeignColonyTradeQuote hostFull = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.CORN, 1),
                Goods.of(GoodsType.TOBACCO, outgoing));
        assertEquals(ForeignColonyTradeProblem.HOST_GOLD,
                hostFull.getProblem());
        assertRejectedWithoutEffects(model, wagon, quebec, hostFull);
    }

    @Test
    void invalid_quantities_and_same_goods_are_refused() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        assertEquals(ForeignColonyTradeProblem.INVALID_QUANTITY,
                model.quoteForeignColonySale(wagon, quebec,
                        Goods.of(GoodsType.CORN, 0)).getProblem());
        assertEquals(ForeignColonyTradeProblem.INVALID_QUANTITY,
                model.quoteForeignColonySale(wagon, quebec,
                        Goods.of(GoodsType.CORN, 3)).getProblem());
        assertEquals(ForeignColonyTradeProblem.SAME_GOODS,
                model.quoteForeignColonyBarter(wagon, quebec,
                        Goods.of(GoodsType.CORN, 1),
                        Goods.of(GoodsType.CORN, 1)).getProblem());
        assertEquals(2, model.getForeignColonyTradeLimit(wagon, quebec,
                GoodsType.CORN, true));
    }

    @Test
    void offer_of_another_carrier_is_refused_before_any_change() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        final Unit ship = model.getUnitById(SHIP_ID);
        load(wagon, GoodsType.CORN, 2);
        load(ship, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        final ForeignColonyTradeQuote fromWagon = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        assertRejectedWithoutEffects(model, ship, quebec, fromWagon);
        assertEquals(2, carried(wagon, GoodsType.CORN));
    }

    @Test
    void offer_of_another_colony_is_refused_before_any_change() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        final Colony other = model.getColonies(model.getPlayerByName("France"))
                .stream().filter(colony -> colony != quebec).findFirst()
                .orElseThrow();
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        assertNotEquals(quote.getColonyId(), other.getId());
        assertRejectedWithoutEffects(model, wagon, other, quote);
        assertEquals(2, carried(wagon, GoodsType.CORN));
    }

    @Test
    void changed_colony_stock_rejects_the_confirmation() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        stockUp(quebec, GoodsType.TOBACCO, 4);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonyBarter(
                wagon, quebec, Goods.of(GoodsType.CORN, 2),
                Goods.of(GoodsType.TOBACCO, 1));
        assertTrue(quote.isLegal());
        quebec.getWarehouse().removeGoods(Goods.of(GoodsType.TOBACCO, 1));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void changed_europe_price_rejects_the_confirmation() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        final GoodsTrade trade = model.getEurope()
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.FRENCH);
        trade.setSellPrice(trade.getSellPrice() + 1);
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void changed_cargo_or_treasury_rejects_the_confirmation() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        // More of the same goods came aboard: the shown terms are stale.
        ((UnitWithCargo) wagon).getCargo().getSlots().get(1)
                .addGoods(Goods.of(GoodsType.CORN, 1));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);

        final ForeignColonyTradeQuote fresh = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        model.getPlayerByName("France").setGold(START_GOLD - 1);
        assertRejectedWithoutEffects(model, wagon, quebec, fresh);
    }

    @Test
    void war_declared_after_quoting_rejects_the_confirmation() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 2);
        final Colony quebec = quebec(model);
        final ForeignColonyTradeQuote quote = model.quoteForeignColonySale(
                wagon, quebec, Goods.of(GoodsType.CORN, 2));
        model.declareHostilities(model.getPlayerByName("Dutch"),
                model.getPlayerByName("France"));
        assertRejectedWithoutEffects(model, wagon, quebec, quote);
    }

    @Test
    void completed_trade_is_saved_once_and_survives_reload() {
        final Model model = setup(true, EuropeanRelationState.PEACE, false);
        final Unit wagon = model.getUnitById(WAGON_ID);
        load(wagon, GoodsType.CORN, 4);
        final Colony quebec = quebec(model);
        final int stock = stock(quebec, GoodsType.CORN);
        model.tradeWithForeignColony(wagon, quebec,
                model.quoteForeignColonySale(wagon, quebec,
                        Goods.of(GoodsType.CORN, 4)));
        final int dutchGold = model.getPlayerByName("Dutch").getGold();
        final int franceGold = model.getPlayerByName("France").getGold();

        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(0,
                carried(reloaded.getUnitById(WAGON_ID), GoodsType.CORN));
        assertEquals(dutchGold, reloaded.getPlayerByName("Dutch").getGold());
        assertEquals(franceGold, reloaded.getPlayerByName("France").getGold());
        assertEquals(stock + 4, stock(quebec(reloaded), GoodsType.CORN));
    }

    /**
     * Rejects the offer and proves that no carrier, warehouse, treasury,
     * position, movement or owner changed.
     */
    private static void assertRejectedWithoutEffects(final Model model,
            final Unit carrier, final Colony colony,
            final ForeignColonyTradeQuote offered) {
        final String before = snapshot(model, carrier, colony);
        assertThrows(ForeignColonyTermsChangedException.class,
                () -> model.tradeWithForeignColony(carrier, colony, offered));
        assertEquals(before, snapshot(model, carrier, colony));
    }

    /** Everything a trade may change, and what a visit must not change. */
    private static String snapshot(final Model model, final Unit carrier,
            final Colony colony) {
        final StringBuilder out = new StringBuilder();
        out.append(model.getPlayerByName("Dutch").getGold()).append('/')
                .append(model.getPlayerByName("France").getGold()).append('@')
                .append(carrier.getLocation()).append('#')
                .append(carrier.getMovementThirds()).append('$')
                .append(colony.getOwner().getName());
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            out.append('|').append(type).append('=')
                    .append(carried(carrier, type)).append(':')
                    .append(stock(colony, type));
        }
        return out.toString();
    }

    /**
     * The free-play map with the approach square made open sea, Jan de Witt in
     * the Dutch Congress when asked, the relation set, and optionally Dutch
     * independence declared (Europe closed).
     */
    private static ModelPo data(final boolean deWitt,
            final EuropeanRelationState relation, final boolean rebellion) {
        final ModelPo data = new GameModelPoDao().loadFromClassPath(MAP)
                .getModel();
        final var terrain = data.getMap().getTerrainMap();
        terrain.put(APPROACH, TerrainType.OCEAN);
        data.getMap().setTerrainType(terrain);
        final Set<Location> fields = data.getMap().getFieldSet();
        if (fields.remove(APPROACH)) {
            data.getMap().setFields(fields);
        }
        final Set<Location> trees = data.getMap().getTreeSet();
        if (trees.remove(APPROACH)) {
            data.getMap().setTrees(trees);
        }
        for (final EuropeanRelationPo row : data.getEuropeanRelations()) {
            if (Set.of(row.getFirstPlayer(), row.getSecondPlayer())
                    .equals(Set.of("Dutch", "France"))) {
                row.setState(relation);
            }
        }
        if (rebellion) {
            RebellionFixtures.holdFreePlayMontreal(data);
        }
        for (final PlayerPo player : data.getGameManager().getPlayers()) {
            if (!player.getName().equals("Dutch")) {
                continue;
            }
            RebellionFixtures.set(data, player, rebellion);
            if (deWitt) {
                final CongressPo congress = new CongressPo();
                congress.setActivated(true);
                congress.setBells(0);
                congress.setGeneration(1);
                congress.setMembers(List.of(Founder.JAN_DE_WITT));
                congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                        Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                        Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
                congress.setJonesDeliveryPending(false);
                player.setCongress(congress);
            }
        }
        return data;
    }

    private static Model setup(final boolean deWitt,
            final EuropeanRelationState relation, final boolean rebellion) {
        return setup(deWitt, relation, rebellion, UnitType.FRIGATE, false);
    }

    /**
     * A started Dutch turn with a wagon and a ship beside Quebec and a second
     * wagon two squares away. Gold is equal for both nations. With a train the
     * ship stores a Dutch treasure train in all its holds, as a saved game
     * does.
     */
    private static Model setup(final boolean deWitt,
            final EuropeanRelationState relation, final boolean rebellion,
            final UnitType shipType, final boolean trainAboard) {
        final Model terrain = Model.make(data(false, relation, false),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ModelPo data = data(deWitt, relation, rebellion);
        data.getUnits()
                .add(unit(WAGON_ID, UnitType.WAGON, landBeside(terrain)));
        final UnitPo ship = unit(SHIP_ID, shipType, seaBeside(terrain));
        data.getUnits().add(ship);
        data.getUnits()
                .add(unit(FAR_WAGON_ID, UnitType.WAGON, landTwoAway(terrain)));
        if (trainAboard) {
            data.getUnits().add(train());
            ship.getCargo().getSlots()
                    .forEach(hold -> hold.setUnitId(TRAIN_ID));
        }
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        model.getPlayerByName("Dutch").setGold(START_GOLD);
        model.getPlayerByName("France").setGold(START_GOLD);
        return model;
    }

    private static UnitPo unit(final int id, final UnitType type,
            final Location at) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId("Dutch");
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        unit.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int i = 0; i < type.getCargoCapacity(); i++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        unit.setCargo(cargo);
        return unit;
    }

    /** A Dutch treasure train stored in a ship, as a saved game writes it. */
    private static UnitPo train() {
        final UnitPo train = unit(TRAIN_ID, UnitType.TREASURE_TRAIN,
                Location.of(0, 0));
        train.setPlaceMap(null);
        train.setPlaceCargoSlot(new PlaceCargoSlotPo());
        train.setTreasureValue(500);
        return train;
    }

    private static boolean open(final Model terrain, final UnitType type,
            final Location at) {
        return terrain.getMap().isValid(at)
                && type.canMoveAtTerrain(terrain.getMap().getTerrainTypeAt(at))
                && terrain.getUnitsAt(at).isEmpty();
    }

    private static Location landBeside(final Model terrain) {
        return QUEBEC.getNeighbors().stream()
                .filter(at -> open(terrain, UnitType.WAGON, at)).findFirst()
                .orElseThrow();
    }

    private static Location seaBeside(final Model terrain) {
        return QUEBEC.getNeighbors().stream()
                .filter(at -> open(terrain, UnitType.FRIGATE, at)).findFirst()
                .orElseThrow();
    }

    private static Location landTwoAway(final Model terrain) {
        return QUEBEC.getNeighbors().stream()
                .flatMap(at -> at.getNeighbors().stream())
                .filter(at -> !at.equals(QUEBEC) && !at.isNeighbor(QUEBEC)
                        && open(terrain, UnitType.WAGON, at))
                .findFirst().orElseThrow();
    }

    private static Colony quebec(final Model model) {
        return model.getColonyAt(QUEBEC).orElseThrow();
    }

    private static int stock(final Colony colony, final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    /**
     * Raise a warehouse stock to the wanted amount. The fixture must fit the
     * real storage capacity, which is never raised or bypassed here.
     */
    private static void stockUp(final Colony colony, final GoodsType type,
            final int wanted) {
        assertTrue(
                colony.getWarehouse().getStorageCapacity(type)
                        .getAmount() >= wanted,
                "The fixture must fit the warehouse capacity of " + type);
        final int missing = wanted - stock(colony, type);
        if (missing > 0) {
            colony.getWarehouse().addGoods(Goods.of(type, missing));
        }
    }

    private static void clear(final Colony colony, final GoodsType type) {
        final int amount = stock(colony, type);
        if (amount > 0) {
            colony.getWarehouse().removeGoods(Goods.of(type, amount));
        }
    }

    private static void richTreasuries(final Model model) {
        model.getPlayerByName("Dutch").setGold(RICH);
        model.getPlayerByName("France").setGold(RICH);
    }

    /** Every hold of the carrier holds a full load of sugar. */
    private static void fillWithSugar(final Unit carrier) {
        for (final CargoSlot slot : ((UnitWithCargo) carrier).getCargo()
                .getSlots()) {
            slot.addGoods(Goods.of(GoodsType.SUGAR, HOLD));
        }
    }

    private static void load(final Unit unit, final GoodsType type,
            final int amount) {
        ((UnitWithCargo) unit).getCargo().getSlots().get(0)
                .addGoods(Goods.of(type, amount));
    }

    private static int carried(final Unit unit, final GoodsType type) {
        if (!(unit instanceof UnitWithCargo)) {
            return 0;
        }
        return ((UnitWithCargo) unit).getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType().equals(type))
                .mapToInt(Goods::getAmount).sum();
    }

    private static int bid(final Model model, final GoodsType type) {
        return model.getEurope()
                .getGoodsTradeForType(type, EuropeanNation.FRENCH)
                .getSellPrice();
    }

    private static int ask(final Model model, final GoodsType type) {
        return model.getEurope()
                .getGoodsTradeForType(type, EuropeanNation.FRENCH)
                .getBuyPrice();
    }
}
