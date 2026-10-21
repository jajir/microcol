package org.microcol.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Native settlement with a stable identity. It belongs to a native nation, not
 * to any European player, and it is never a {@link Colony}.
 */
public final class NativeSettlement {

    /** Additive defense bonus that only a nation's capital receives. */
    static final int CAPITAL_DEFENSE_BONUS_PERCENT = 25;

    /** Initial treasury of an ordinary settlement. */
    static final int ORDINARY_INITIAL_TREASURY = 1000;

    /** Initial treasury of a nation's capital. */
    static final int CAPITAL_INITIAL_TREASURY = 2000;

    private final int id;
    private final NativeNation nation;
    private final NativeSettlementClass settlementClass;
    private final boolean capital;
    private final Location location;
    private final UnitType teachableSkill;
    private int treasury;
    /** Pupil of this settlement's single native course, or null for none. */
    private Integer teachingPupilId;
    /** Owner updates the pupil has completed in the current course. */
    private int teachingUpdates;
    /** Stage of the current course, or null when the place is free. */
    private NativeTeachingCourse.Stage teachingStage;
    /** Offered goods in offer order, each with its current stock. */
    private final Map<GoodsType, Integer> offeredStock;
    /** Goods this settlement wants to buy, in demand order. */
    private final List<GoodsType> wantedGoods;
    /** Trade with each European nation that has one, in European order. */
    private final Map<EuropeanNation, NativeTradeState> trade = new EnumMap<>(
            EuropeanNation.class);
    /**
     * Goods bought from the player (R-0160). Kept apart from the offered stock:
     * it never restocks, is never capped and is never confiscated by an update.
     */
    private final Map<GoodsType, Integer> receivedStock = new LinkedHashMap<>();

    NativeSettlement(final int id, final NativeNation nation,
            final boolean capital, final Location location, final int treasury,
            final UnitType teachableSkill,
            final Map<GoodsType, Integer> offeredStock,
            final List<GoodsType> wantedGoods) {
        this.id = id;
        this.nation = nation;
        this.settlementClass = nation.getSettlementClass();
        this.capital = capital;
        this.location = location;
        this.treasury = treasury;
        this.teachableSkill = teachableSkill;
        this.offeredStock = new LinkedHashMap<>(offeredStock);
        this.wantedGoods = List.copyOf(wantedGoods);
    }

    public int getId() {
        return id;
    }

    public NativeNation getNation() {
        return nation;
    }

    public NativeSettlementClass getSettlementClass() {
        return settlementClass;
    }

    public boolean isCapital() {
        return capital;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * @return skill this settlement offers to teach; chosen at map creation and
     *         saved, never redrawn by viewing or loading
     */
    public UnitType getTeachableSkill() {
        return teachableSkill;
    }

    /**
     * @return additive defense bonus for the real defenders of this settlement
     */
    public int getDefenseBonusPercent() {
        return settlementClass.getDefenseBonusPercent()
                + (capital ? CAPITAL_DEFENSE_BONUS_PERCENT : 0);
    }

    /**
     * @return treasury this settlement starts with; later updates belong to the
     *         trade owner
     */
    public int getInitialTreasury() {
        return capital ? CAPITAL_INITIAL_TREASURY : ORDINARY_INITIAL_TREASURY;
    }

    /**
     * @return gold currently held, as saved; land payments add to it
     */
    public int getTreasury() {
        return treasury;
    }

    /**
     * Replace the treasury with a total the caller has already validated, such
     * as a land payment added without overflow.
     *
     * @param newTreasury nonnegative gold
     */
    void setTreasury(final int newTreasury) {
        Preconditions.checkArgument(newTreasury >= 0,
                "Native treasury cannot be negative");
        treasury = newTreasury;
    }

    /**
     * @return goods offered for sale in offer order, fewer than three when the
     *         surroundings produce fewer; no slot is invented
     */
    public List<GoodsType> getOfferedGoods() {
        return List.copyOf(offeredStock.keySet());
    }

    /**
     * @param goods required goods type
     * @return stock of an offered good, zero when it is not offered
     */
    public int getStock(final GoodsType goods) {
        Preconditions.checkNotNull(goods);
        return offeredStock.getOrDefault(goods, 0);
    }

    /**
     * @return goods this settlement wants to buy, in demand order
     */
    public List<GoodsType> getWantedGoods() {
        return wantedGoods;
    }

    /**
     * Replace the stock of an offered good with an amount the caller has
     * checked against the stock cap.
     *
     * @param goods required offered goods type
     * @param stock stock from zero to the cap
     */
    void setStock(final GoodsType goods, final int stock) {
        Preconditions.checkArgument(offeredStock.containsKey(goods),
                "Native goods are not offered here");
        Preconditions.checkArgument(
                stock >= 0 && stock <= NativeMarketRules.STOCK_CAP,
                "Native stock is out of range");
        offeredStock.put(goods, stock);
    }

    /**
     * One owner update of this settlement: the treasury gains its amount,
     * capped without reducing a higher treasury, and every offered good
     * restocks up to the cap. Called once per native owner update, never by
     * viewing.
     */
    void applyOwnerUpdate() {
        treasury = NativeMarketRules.treasuryAfterUpdate(treasury, capital);
        offeredStock.replaceAll(
                (goods, stock) -> NativeMarketRules.stockAfterUpdate(stock));
    }

    /**
     * @param european required European nation
     * @return trade with that nation, empty when it has never been started
     */
    Optional<NativeTradeState> findTrade(final EuropeanNation european) {
        return Optional.ofNullable(trade.get(european));
    }

    /**
     * @param european required European nation
     * @return trade with that nation, created empty on first use
     */
    NativeTradeState tradeFor(final EuropeanNation european) {
        return trade.computeIfAbsent(european,
                key -> new NativeTradeState(null));
    }

    /**
     * @return every recorded trade, in European order, unmodifiable
     */
    Map<EuropeanNation, NativeTradeState> getTrades() {
        return Collections.unmodifiableMap(trade);
    }

    /**
     * Restore one saved trade. The caller has validated its goods and
     * expedition.
     *
     * @param european required European nation
     * @param state    required restored state
     */
    void restoreTrade(final EuropeanNation european,
            final NativeTradeState state) {
        trade.put(european, state);
    }

    /**
     * @return offered goods with their stock, in offer order, unmodifiable
     */
    Map<GoodsType, Integer> getOfferedStock() {
        return Collections.unmodifiableMap(offeredStock);
    }

    /**
     * @return goods bought from carriers, in goods order, unmodifiable
     */
    public Map<GoodsType, Integer> getReceivedStock() {
        return Collections.unmodifiableMap(receivedStock);
    }

    /**
     * @param goods required goods type
     * @return most pieces of these goods the stock can still count; the stock
     *         of goods is an int, so the room is never negative
     */
    int getReceiveRoom(final GoodsType goods) {
        Preconditions.checkNotNull(goods);
        return Integer.MAX_VALUE - receivedStock.getOrDefault(goods, 0);
    }

    /**
     * Add goods the settlement has bought from a carrier (R-0160). The model
     * checks {@link #getReceiveRoom} before any change, so the addition never
     * overflows here; the exact addition still refuses an overflow.
     *
     * @param goods  required goods bought
     * @param amount positive pieces bought, within the room
     */
    void addReceived(final GoodsType goods, final int amount) {
        Preconditions.checkArgument(amount > 0,
                "Received native goods must be positive");
        receivedStock.merge(goods, amount, Math::addExact);
    }

    /**
     * Restore saved goods bought from carriers; the caller has validated them.
     *
     * @param goods  required goods type
     * @param amount positive saved pieces
     */
    void restoreReceived(final GoodsType goods, final int amount) {
        Preconditions.checkArgument(amount > 0,
                "Received native goods must be positive");
        receivedStock.put(goods, amount);
    }

    /**
     * @return identity of the colonist learning here, empty when the teaching
     *         place is free
     */
    public Optional<Integer> getTeachingPupilId() {
        return Optional.ofNullable(teachingPupilId);
    }

    /**
     * @return owner updates completed by the current pupil, zero without one
     */
    public int getTeachingUpdates() {
        return teachingUpdates;
    }

    /**
     * @return stage of the current course, empty when the place is free
     */
    public Optional<NativeTeachingCourse.Stage> getTeachingStage() {
        return Optional.ofNullable(teachingStage);
    }

    /**
     * Start a course for a pupil who has just been accepted.
     *
     * @param pupilId required pupil identity
     */
    void startTeaching(final int pupilId) {
        Preconditions.checkState(teachingPupilId == null,
                "Native teaching place is taken");
        teachingPupilId = pupilId;
        teachingUpdates = 0;
        teachingStage = NativeTeachingCourse.Stage.LEARNING;
    }

    /**
     * Restore a saved course. The caller has validated the stage and progress.
     *
     * @param pupilId required pupil identity
     * @param updates counted owner updates
     * @param stage   required stage
     */
    void restoreTeaching(final int pupilId, final int updates,
            final NativeTeachingCourse.Stage stage) {
        Preconditions.checkArgument(stage != null,
                "Native teaching stage is missing");
        teachingPupilId = pupilId;
        teachingUpdates = updates;
        teachingStage = stage;
    }

    /**
     * Count one owner update of a learning pupil.
     */
    void addTeachingUpdate() {
        Preconditions.checkState(
                teachingStage == NativeTeachingCourse.Stage.LEARNING
                        && teachingUpdates < NativeTeachingCourse.REQUIRED_UPDATES,
                "No native teaching update is due");
        teachingUpdates++;
    }

    /**
     * The fourth owner update grants the skill; the learning is over, but the
     * colonist stays until it may leave.
     */
    void completeTeaching() {
        Preconditions.checkState(
                teachingStage == NativeTeachingCourse.Stage.LEARNING
                        && teachingUpdates == NativeTeachingCourse.REQUIRED_UPDATES,
                "Native teaching is not complete");
        teachingStage = NativeTeachingCourse.Stage.COMPLETED;
    }

    /**
     * Hostility ends a learning at once: its progress is discarded and nothing
     * is granted. The colonist stays until it may leave.
     */
    void cancelTeaching() {
        Preconditions.checkState(
                teachingStage == NativeTeachingCourse.Stage.LEARNING,
                "Only a learning course can be canceled");
        teachingStage = NativeTeachingCourse.Stage.CANCELED;
        teachingUpdates = 0;
    }

    /**
     * Free the teaching place after the pupil has left the settlement.
     */
    void clearTeaching() {
        teachingPupilId = null;
        teachingUpdates = 0;
        teachingStage = null;
    }

    /**
     * Land claimed by this settlement: squares at Chebyshev distance one up to
     * its claim radius, the same radius as its home. The settlement's own
     * square is never claimed.
     *
     * @param at required square
     * @return whether this settlement claims the square
     */
    boolean claims(final Location at) {
        final int distance = location.getDistanceChebyshev(at);
        return distance >= 1 && distance <= settlementClass.getHomeRadius();
    }

}
