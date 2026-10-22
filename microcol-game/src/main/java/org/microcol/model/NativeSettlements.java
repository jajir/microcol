package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.microcol.model.store.NativeChiefEncounterPo;
import org.microcol.model.store.NativeExpeditionPo;
import org.microcol.model.store.NativeGoodsStockPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.NativeTradePo;
import org.microcol.model.store.NativeTributeRecordPo;

import com.google.common.base.Preconditions;

/**
 * Registry of native settlements. It validates saved identity before the model
 * accepts it: unique IDs, land sites distinct from European colonies and other
 * settlements, the nation's own class and exactly one capital per nation that
 * has settlements.
 */
public final class NativeSettlements {

    private final List<NativeSettlement> settlements;

    private NativeSettlements(final List<NativeSettlement> settlements) {
        this.settlements = new ArrayList<>(settlements);
    }

    /**
     * Build the registry from saved entries. Missing or invalid identity is
     * rejected; an empty list is a valid world.
     *
     * @param saved       required saved entries, possibly empty
     * @param landSite    required test for a square where a settlement may
     *                    stand
     * @param colonySites required squares of European colonies
     * @return validated registry
     */
    public static NativeSettlements make(final List<NativeSettlementPo> saved,
            final Predicate<Location> landSite,
            final Set<Location> colonySites) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native settlements");
        Preconditions.checkNotNull(landSite);
        Preconditions.checkNotNull(colonySites);
        final Set<Integer> ids = new HashSet<>();
        final Set<Location> sites = new HashSet<>();
        final EnumMap<NativeNation, Integer> capitals = new EnumMap<>(
                NativeNation.class);
        final List<NativeSettlement> out = new ArrayList<>();
        final Set<Integer> missionaries = new HashSet<>();
        for (final NativeSettlementPo entry : saved) {
            Preconditions.checkArgument(entry != null,
                    "Unsupported save: missing native settlement");
            Preconditions.checkArgument(
                    entry.getId() != null && entry.getId() >= 0
                            && ids.add(entry.getId()),
                    "Unsupported save: missing or duplicate native settlement id");
            Preconditions.checkArgument(entry.getNation() != null,
                    "Unsupported save: missing native nation");
            Preconditions.checkArgument(
                    entry.getSettlementClass() == entry.getNation()
                            .getSettlementClass(),
                    "Unsupported save: native settlement class does not match its nation");
            Preconditions.checkArgument(entry.getCapital() != null,
                    "Unsupported save: missing native capital flag");
            Preconditions.checkArgument(
                    entry.getTreasury() != null && entry.getTreasury() >= 0,
                    "Unsupported save: missing or negative native treasury");
            final UnitType skill = acceptedSkill(entry.getTeachableSkill());
            final Location location = entry.getLocation();
            Preconditions.checkArgument(location != null,
                    "Unsupported save: missing native settlement location");
            Preconditions.checkArgument(landSite.test(location),
                    "Unsupported save: native settlement is not on land");
            Preconditions.checkArgument(!colonySites.contains(location),
                    "Unsupported save: native settlement shares a European colony square");
            Preconditions.checkArgument(sites.add(location),
                    "Unsupported save: two native settlements share a square");
            if (entry.getCapital()) {
                capitals.merge(entry.getNation(), 1, Integer::sum);
            }
            final NativeSettlement settlement = new NativeSettlement(
                    entry.getId(), entry.getNation(), entry.getCapital(),
                    location, entry.getTreasury(), skill,
                    readOffer(entry.getOfferedGoods()),
                    readWanted(entry.getWantedGoods()));
            restoreTeaching(entry, settlement);
            restoreTrade(entry, settlement);
            restoreReceived(entry.getReceivedGoods(), settlement);
            restoreChiefEncounters(entry.getChiefEncounters(), settlement);
            restoreTributes(entry.getTributes(), settlement);
            restoreMission(entry, settlement, missionaries);
            out.add(settlement);
        }
        for (final NativeSettlement settlement : out) {
            Preconditions.checkArgument(
                    capitals.getOrDefault(settlement.getNation(), 0) == 1,
                    "Unsupported save: a native nation with settlements needs exactly one capital");
        }
        return new NativeSettlements(out);
    }

    /**
     * Restore the saved native course of one settlement (R-0148). A free place
     * saves no pupil, skill or progress, so absent progress is zero there; a
     * taken place needs its progress and names the settlement's own offered
     * skill. The pupil itself is checked against the units later.
     *
     * @param entry      required saved settlement
     * @param settlement required settlement built from the same entry
     */
    private static void restoreTeaching(final NativeSettlementPo entry,
            final NativeSettlement settlement) {
        if (entry.getTeachingPupilId() == null) {
            Preconditions.checkArgument(
                    (entry.getTeachingUpdates() == null
                            || entry.getTeachingUpdates() == 0)
                            && entry.getTeachingSkill() == null
                            && entry.getTeachingStage() == null,
                    "Unsupported save: native teaching progress without a pupil");
            return;
        }
        Preconditions.checkArgument(entry.getTeachingUpdates() != null
                && entry.getTeachingUpdates() >= 0
                && entry.getTeachingUpdates() <= NativeTeachingCourse.REQUIRED_UPDATES,
                "Unsupported save: missing or invalid native teaching progress");
        Preconditions.checkArgument(
                entry.getTeachingPupilId() >= 0
                        && settlement.getTeachableSkill().name()
                                .equals(entry.getTeachingSkill()),
                "Unsupported save: native teaching skill does not match its settlement");
        final NativeTeachingCourse.Stage stage = savedStage(
                entry.getTeachingStage());
        final int updates = entry.getTeachingUpdates();
        // Each stage has one progress shape: learning counts up to three, a
        // finished course counts four, a canceled one discards its count.
        Preconditions.checkArgument(stage == NativeTeachingCourse.Stage.LEARNING
                && updates < NativeTeachingCourse.REQUIRED_UPDATES
                || stage == NativeTeachingCourse.Stage.COMPLETED
                        && updates == NativeTeachingCourse.REQUIRED_UPDATES
                || stage == NativeTeachingCourse.Stage.CANCELED && updates == 0,
                "Unsupported save: native teaching stage does not match its progress");
        settlement.restoreTeaching(entry.getTeachingPupilId(), updates, stage);
    }

    /**
     * @param saved saved offered goods, required; at most the offer slots
     * @return offered goods with their saved stock, in the saved order
     */
    private static Map<GoodsType, Integer> readOffer(
            final List<NativeGoodsStockPo> saved) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native offered goods");
        Preconditions.checkArgument(
                saved.size() <= NativeMarketRules.OFFER_SLOTS,
                "Unsupported save: too many native offered goods");
        final Map<GoodsType, Integer> out = new LinkedHashMap<>();
        for (final NativeGoodsStockPo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getStock() != null,
                    "Unsupported save: incomplete native offered goods");
            Preconditions.checkArgument(
                    out.put(tradableGoods(entry.getGoods()),
                            entry.getStock()) == null,
                    "Unsupported save: duplicate native offered goods");
        }
        Preconditions.checkArgument(NativeMarketRules.isValidStock(out),
                "Unsupported save: native stock is out of range");
        return out;
    }

    /**
     * @param saved saved wanted goods names, required; at most the wanted slots
     * @return wanted goods in the saved order
     */
    private static List<GoodsType> readWanted(final List<String> saved) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native wanted goods");
        Preconditions.checkArgument(
                saved.size() <= NativeMarketRules.WANTED_SLOTS,
                "Unsupported save: too many native wanted goods");
        final List<GoodsType> out = new ArrayList<>();
        for (final String name : saved) {
            final GoodsType goods = tradableGoods(name);
            Preconditions.checkArgument(!out.contains(goods),
                    "Unsupported save: duplicate native wanted goods");
            out.add(goods);
        }
        return out;
    }

    /**
     * Restore the saved trade of one settlement (R-0159). An expedition names
     * its carrier and anchor together, and the anchor must be beside the
     * settlement. The carrier is checked against the units later.
     *
     * @param entry      required saved settlement
     * @param settlement required settlement built from the same entry
     */
    private static void restoreTrade(final NativeSettlementPo entry,
            final NativeSettlement settlement) {
        Preconditions.checkArgument(entry.getTrade() != null,
                "Unsupported save: missing native trade");
        for (final NativeTradePo saved : entry.getTrade()) {
            Preconditions.checkArgument(
                    saved != null && saved.getEuropean() != null,
                    "Unsupported save: incomplete native trade");
            Preconditions.checkArgument(
                    settlement.findTrade(saved.getEuropean()).isEmpty(),
                    "Unsupported save: duplicate native trade");
            Preconditions.checkArgument(saved.getExpeditions() != null,
                    "Unsupported save: missing native expeditions");
            final NativeTradeState state = new NativeTradeState(
                    saved.getLastReceivedGoods() == null ? null
                            : tradableGoods(saved.getLastReceivedGoods()));
            state.restoreRevision(
                    saved.getRevision() == null ? 0 : saved.getRevision());
            for (final NativeExpeditionPo expedition : saved.getExpeditions()) {
                Preconditions.checkArgument(
                        expedition != null && expedition.getCarrierId() != null
                                && expedition.getAnchor() != null,
                        "Unsupported save: incomplete native expedition");
                Preconditions.checkArgument(
                        settlement.getLocation()
                                .isNeighbor(expedition.getAnchor()),
                        "Unsupported save: native expedition is not beside its settlement");
                Preconditions.checkArgument(
                        state.getExpeditions()
                                .get(expedition.getCarrierId()) == null,
                        "Unsupported save: duplicate native expedition");
                final NativeExpedition restored = new NativeExpedition(
                        expedition.getCarrierId(), expedition.getAnchor(),
                        expedition.getBlockedGoods() == null ? null
                                : tradableGoods(expedition.getBlockedGoods()));
                restoreHaggle(expedition, restored);
                restorePurchase(expedition, restored, state.getRevision(),
                        settlement);
                state.restoreExpedition(restored);
            }
            settlement.restoreTrade(saved.getEuropean(), state);
        }
    }

    /**
     * Restore the one haggle drawn in an expedition (R-0160). The goods and the
     * result belong together: a saved goods name needs its result, and a result
     * without goods is refused.
     *
     * @param saved      required saved expedition
     * @param expedition required expedition built from the same entry
     */
    private static void restoreHaggle(final NativeExpeditionPo saved,
            final NativeExpedition expedition) {
        if (saved.getHaggledGoods() == null) {
            Preconditions.checkArgument(saved.getHaggleAccepted() == null,
                    "Unsupported save: native haggle result without its goods");
            Preconditions.checkArgument(saved.getHaggledQuantity() == null,
                    "Unsupported save: native haggle pieces without its goods");
            return;
        }
        Preconditions.checkArgument(saved.getHaggleAccepted() != null,
                "Unsupported save: native haggle goods without its result");
        // The pieces of the shown offer are optional presentation state.
        Preconditions.checkArgument(
                saved.getHaggledQuantity() == null
                        || saved.getHaggledQuantity() > 0,
                "Unsupported save: native haggle pieces must be positive");
        expedition.restoreHaggle(tradableGoods(saved.getHaggledGoods()),
                saved.getHaggleAccepted());
        expedition.restoreHaggledQuantity(saved.getHaggledQuantity());
    }

    /**
     * Restore the pending second stage of an expedition (R-0161). A saved stage
     * names the revision of the sale or gift that opened it, which cannot be
     * later than the trade's revision, and its one request result; a result
     * without a stage is refused.
     *
     * @param saved         required saved expedition
     * @param expedition    required expedition built from the same entry
     * @param tradeRevision revision of the trade the expedition belongs to
     * @param settlement    required settlement whose offered goods the stage
     *                      may name
     */
    private static void restorePurchase(final NativeExpeditionPo saved,
            final NativeExpedition expedition, final int tradeRevision,
            final NativeSettlement settlement) {
        if (saved.getPurchaseRevision() == null) {
            Preconditions.checkArgument(
                    saved.getPurchaseHaggle() == null
                            && saved.getPurchaseGoods() == null
                            && saved.getPurchaseQuantity() == null,
                    "Unsupported save: native purchase result without its stage");
            return;
        }
        Preconditions.checkArgument(
                saved.getPurchaseRevision() >= 0
                        && saved.getPurchaseRevision() <= tradeRevision,
                "Unsupported save: native purchase stage is not in the trade history");
        final NativeSaleQuote.Haggle haggle = savedPurchaseHaggle(
                saved.getPurchaseHaggle());
        // The offer a request was made for exists exactly once the request was
        // drawn: an open request has no offer, a drawn one needs it.
        if (haggle == NativeSaleQuote.Haggle.OFFERED) {
            Preconditions.checkArgument(
                    saved.getPurchaseGoods() == null
                            && saved.getPurchaseQuantity() == null,
                    "Unsupported save: native purchase offer before its request");
            expedition.restorePurchase(saved.getPurchaseRevision(), haggle,
                    null, null);
            return;
        }
        Preconditions.checkArgument(
                saved.getPurchaseGoods() != null
                        && saved.getPurchaseQuantity() != null
                        && saved.getPurchaseQuantity() > 0,
                "Unsupported save: native purchase request without its offer");
        final GoodsType goods = tradableGoods(saved.getPurchaseGoods());
        Preconditions.checkArgument(
                settlement.getOfferedStock().containsKey(goods),
                "Unsupported save: native purchase offer names goods not sold here");
        expedition.restorePurchase(saved.getPurchaseRevision(), haggle, goods,
                saved.getPurchaseQuantity());
    }

    /**
     * @param name saved request state name, possibly missing
     * @return the request state with that name; an unavailable state is refused
     */
    private static NativeSaleQuote.Haggle savedPurchaseHaggle(
            final String name) {
        Preconditions.checkArgument(name != null,
                "Unsupported save: native purchase stage without its request result");
        final NativeSaleQuote.Haggle haggle;
        try {
            haggle = NativeSaleQuote.Haggle.valueOf(name);
        } catch (final IllegalArgumentException unknown) {
            throw new IllegalArgumentException(
                    "Unsupported save: unknown native purchase request result",
                    unknown);
        }
        Preconditions.checkArgument(
                haggle != NativeSaleQuote.Haggle.UNAVAILABLE,
                "Unsupported save: unknown native purchase request result");
        return haggle;
    }

    /**
     * Restore the goods a settlement has bought from carriers (R-0160). Absent
     * means none has been bought; each saved amount must be positive.
     *
     * @param saved      saved goods, possibly null
     * @param settlement required settlement built from the same entry
     */
    private static void restoreReceived(final List<NativeGoodsStockPo> saved,
            final NativeSettlement settlement) {
        if (saved == null) {
            return;
        }
        for (final NativeGoodsStockPo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getGoods() != null
                            && entry.getStock() != null && entry.getStock() > 0,
                    "Unsupported save: incomplete native received goods");
            final GoodsType goods = tradableGoods(entry.getGoods());
            Preconditions.checkArgument(
                    settlement.getReceivedStock().get(goods) == null,
                    "Unsupported save: duplicate native received goods");
            settlement.restoreReceived(goods, entry.getStock());
        }
    }

    /**
     * Restore the saved chief encounters of one settlement (R-0158). Absent
     * means none has happened; each nation may meet the chief here only once.
     *
     * @param saved      saved encounters, possibly null
     * @param settlement required settlement built from the same entry
     */
    private static void restoreChiefEncounters(
            final List<NativeChiefEncounterPo> saved,
            final NativeSettlement settlement) {
        if (saved == null) {
            return;
        }
        for (final NativeChiefEncounterPo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getEuropean() != null
                            && entry.getOutcome() != null,
                    "Unsupported save: incomplete native chief encounter");
            Preconditions.checkArgument(
                    settlement.findChiefEncounter(entry.getEuropean())
                            .isEmpty(),
                    "Unsupported save: duplicate native chief encounter");
            settlement.recordChiefEncounter(entry.getEuropean(),
                    entry.getOutcome());
        }
    }

    /**
     * Restore the saved tribute demands of one settlement (R-0162). Absent
     * means none has been made; each European has one saved turn, its last
     * demand. The turn is checked against the game turn by the model that loads
     * it.
     *
     * @param saved      saved demands, possibly null
     * @param settlement required settlement built from the same entry
     */
    private static void restoreTributes(final List<NativeTributeRecordPo> saved,
            final NativeSettlement settlement) {
        if (saved == null) {
            return;
        }
        for (final NativeTributeRecordPo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getEuropean() != null
                            && entry.getTurn() != null && entry.getTurn() >= 0,
                    "Unsupported save: incomplete native tribute demand");
            Preconditions.checkArgument(
                    settlement.findTributeTurn(entry.getEuropean()).isEmpty(),
                    "Unsupported save: duplicate native tribute demand");
            settlement.recordTribute(entry.getEuropean(), entry.getTurn());
        }
    }

    /**
     * Restore the saved mission of one settlement (R-0164). A settlement
     * without a missionary saves no clock, so absent progress is zero there. A
     * held mission needs a missionary no other settlement holds and a count
     * below the required updates. The missionary itself is checked against the
     * units later.
     *
     * @param entry        required saved settlement
     * @param settlement   required settlement built from the same entry
     * @param missionaries identities of the missionaries already restored
     */
    private static void restoreMission(final NativeSettlementPo entry,
            final NativeSettlement settlement,
            final Set<Integer> missionaries) {
        if (entry.getMissionaryId() == null) {
            Preconditions.checkArgument(
                    entry.getMissionUpdates() == null
                            || entry.getMissionUpdates() == 0,
                    "Unsupported save: native mission progress without a missionary");
            return;
        }
        Preconditions.checkArgument(
                entry.getMissionaryId() >= 0
                        && missionaries.add(entry.getMissionaryId()),
                "Unsupported save: missing or duplicate native mission missionary");
        Preconditions.checkArgument(entry.getMissionUpdates() != null
                && entry.getMissionUpdates() >= 0
                && entry.getMissionUpdates() < NativeMissionView.REQUIRED_UPDATES,
                "Unsupported save: missing or invalid native mission progress");
        settlement.restoreMission(entry.getMissionaryId(),
                entry.getMissionUpdates());
    }

    /**
     * @param name saved goods name, possibly missing
     * @return the tradable goods type with that name
     * @throws IllegalArgumentException when the name is missing or not tradable
     */
    private static GoodsType tradableGoods(final String name) {
        Preconditions.checkArgument(name != null,
                "Unsupported save: missing native goods");
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(goods -> goods.name().equals(name)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported save: native goods are not tradable"));
    }

    /**
     * @param name saved stage name, possibly missing
     * @return the stage with that name
     */
    private static NativeTeachingCourse.Stage savedStage(final String name) {
        Preconditions.checkArgument(name != null,
                "Unsupported save: missing native teaching stage");
        try {
            return NativeTeachingCourse.Stage.valueOf(name);
        } catch (final IllegalArgumentException unknown) {
            throw new IllegalArgumentException(
                    "Unsupported save: unknown native teaching stage", unknown);
        }
    }

    /**
     * @param name saved skill name, possibly missing
     * @return the accepted native-teachable skill with that name
     * @throws IllegalArgumentException when the name is missing or not accepted
     */
    private static UnitType acceptedSkill(final String name) {
        return UnitType.NATIVE_TEACHABLE_SKILLS.stream()
                .filter(skill -> skill.name().equals(name)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported save: missing or unaccepted native teachable skill"));
    }

    /**
     * @return every settlement in the saved order, unmodifiable
     */
    public List<NativeSettlement> getAll() {
        return List.copyOf(settlements);
    }

    /**
     * Retire a conquered settlement (R-0174). It leaves the registry at once,
     * so its claim and its map interaction end with it; the caller has checked
     * that the settlement belongs to this registry.
     *
     * @param settlement required settlement that was conquered
     */
    void remove(final NativeSettlement settlement) {
        Preconditions.checkArgument(settlements.remove(settlement),
                "Native settlement is not registered");
    }

    /**
     * @param location required square
     * @return settlement on the square, if any
     */
    public Optional<NativeSettlement> getAt(final Location location) {
        Preconditions.checkNotNull(location);
        return settlements.stream()
                .filter(settlement -> settlement.getLocation().equals(location))
                .findAny();
    }

    /**
     * Owner of the land claim on a square: the nearest settlement whose claim
     * covers it, then the lowest stable ID. A settlement's own square is never
     * claimed. The caller decides whether the square is land.
     *
     * @param location required square
     * @return claiming settlement, if any
     */
    public Optional<NativeSettlement> getClaimOwner(final Location location) {
        Preconditions.checkNotNull(location);
        if (getAt(location).isPresent()) {
            return Optional.empty();
        }
        return settlements.stream()
                .filter(settlement -> settlement.claims(location))
                .min(Comparator.comparingInt(
                        (NativeSettlement settlement) -> settlement
                                .getLocation().getDistanceChebyshev(location))
                        .thenComparingInt(NativeSettlement::getId));
    }

    /**
     * @param settlement required settlement
     * @return saved form of its trades, in European order
     */
    private static List<NativeTradePo> saveTrades(
            final NativeSettlement settlement,
            final Predicate<NativeExpedition> open) {
        final List<NativeTradePo> out = new ArrayList<>();
        settlement.getTrades().forEach((european, state) -> {
            final NativeTradePo po = new NativeTradePo();
            po.setEuropean(european);
            po.setLastReceivedGoods(
                    state.getLastReceived().map(GoodsType::name).orElse(null));
            po.setRevision(state.getRevision());
            final List<NativeExpeditionPo> expeditions = new ArrayList<>();
            for (final NativeExpedition expedition : state.getExpeditions()
                    .values()) {
                if (open.test(expedition)) {
                    final NativeExpeditionPo expeditionPo = new NativeExpeditionPo();
                    expeditionPo.setCarrierId(expedition.getCarrierId());
                    expeditionPo.setAnchor(expedition.getAnchor());
                    expeditionPo.setBlockedGoods(expedition.getBlockedGoods()
                            .map(GoodsType::name).orElse(null));
                    if (expedition.getHaggledGoods().isPresent()) {
                        expeditionPo.setHaggledGoods(
                                expedition.getHaggledGoods().get().name());
                        expeditionPo.setHaggleAccepted(
                                expedition.isHaggleAccepted());
                        expeditionPo.setHaggledQuantity(
                                expedition.getHaggledQuantity().orElse(null));
                    }
                    if (expedition.getPurchaseRevision().isPresent()) {
                        expeditionPo.setPurchaseRevision(
                                expedition.getPurchaseRevision().get());
                        expeditionPo.setPurchaseHaggle(
                                expedition.getPurchaseHaggle().name());
                        expeditionPo
                                .setPurchaseGoods(expedition.getPurchaseGoods()
                                        .map(GoodsType::name).orElse(null));
                        expeditionPo.setPurchaseQuantity(
                                expedition.getPurchaseQuantity().orElse(null));
                    }
                    expeditions.add(expeditionPo);
                }
            }
            po.setExpeditions(expeditions);
            out.add(po);
        });
        return out;
    }

    /**
     * @param settlement required settlement
     * @return saved form of its tribute demands, in European order
     */
    private static List<NativeTributeRecordPo> saveTributes(
            final NativeSettlement settlement) {
        final List<NativeTributeRecordPo> out = new ArrayList<>();
        settlement.getTributeTurns().forEach((european, turn) -> {
            final NativeTributeRecordPo po = new NativeTributeRecordPo();
            po.setEuropean(european);
            po.setTurn(turn);
            out.add(po);
        });
        return out;
    }

    /**
     * @param settlement required settlement
     * @return saved form of its chief encounters, in European order
     */
    private static List<NativeChiefEncounterPo> saveChiefEncounters(
            final NativeSettlement settlement) {
        final List<NativeChiefEncounterPo> out = new ArrayList<>();
        settlement.getChiefEncounters().forEach((european, outcome) -> {
            final NativeChiefEncounterPo po = new NativeChiefEncounterPo();
            po.setEuropean(european);
            po.setOutcome(outcome);
            out.add(po);
        });
        return out;
    }

    /**
     * @return saved form of every settlement, in the same order
     */
    public List<NativeSettlementPo> toPo(
            final Predicate<NativeExpedition> open) {
        final List<NativeSettlementPo> out = new ArrayList<>();
        for (final NativeSettlement settlement : settlements) {
            final NativeSettlementPo po = new NativeSettlementPo();
            po.setId(settlement.getId());
            po.setNation(settlement.getNation());
            po.setSettlementClass(settlement.getSettlementClass());
            po.setCapital(settlement.isCapital());
            po.setLocation(settlement.getLocation());
            po.setTreasury(settlement.getTreasury());
            po.setTeachableSkill(settlement.getTeachableSkill().name());
            po.setTeachingPupilId(settlement.getTeachingPupilId().orElse(null));
            po.setTeachingUpdates(settlement.getTeachingUpdates());
            po.setTeachingStage(
                    settlement.getTeachingStage().map(Enum::name).orElse(null));
            po.setTeachingSkill(settlement.getTeachingPupilId().isPresent()
                    ? settlement.getTeachableSkill().name()
                    : null);
            final List<NativeGoodsStockPo> stock = new ArrayList<>();
            settlement.getOfferedStock().forEach((goods, amount) -> {
                final NativeGoodsStockPo goodsPo = new NativeGoodsStockPo();
                goodsPo.setGoods(goods.name());
                goodsPo.setStock(amount);
                stock.add(goodsPo);
            });
            po.setOfferedGoods(stock);
            final List<NativeGoodsStockPo> received = new ArrayList<>();
            settlement.getReceivedStock().forEach((goods, amount) -> {
                final NativeGoodsStockPo goodsPo = new NativeGoodsStockPo();
                goodsPo.setGoods(goods.name());
                goodsPo.setStock(amount);
                received.add(goodsPo);
            });
            po.setReceivedGoods(received);
            final List<String> wanted = new ArrayList<>();
            settlement.getWantedGoods()
                    .forEach(goods -> wanted.add(goods.name()));
            po.setWantedGoods(wanted);
            po.setTrade(saveTrades(settlement, open));
            po.setChiefEncounters(saveChiefEncounters(settlement));
            po.setTributes(saveTributes(settlement));
            po.setMissionaryId(settlement.getMissionaryId().orElse(null));
            po.setMissionUpdates(settlement.getMissionUpdates());
            out.add(po);
        }
        return out;
    }

}
