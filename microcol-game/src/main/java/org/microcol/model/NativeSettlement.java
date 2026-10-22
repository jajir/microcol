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
    /**
     * Outcome of the one chief encounter of each visiting European nation
     * (R-0158), in European order; a nation absent here has not met the chief
     * yet.
     */
    private final Map<EuropeanNation, NativeChiefOutcome> chiefEncounters = new EnumMap<>(
            EuropeanNation.class);
    /**
     * Turn of the last tribute demand of each European here (R-0162), in
     * European order; a European absent here has not demanded tribute yet.
     */
    private final Map<EuropeanNation, Integer> tributeTurns = new EnumMap<>(
            EuropeanNation.class);
    /** Missionary whose mission this settlement holds, or null for none. */
    private Integer missionaryId;
    /** Nonhostile owner updates of the current mission cycle, 0..4. */
    private int missionUpdates;
    /**
     * Counts every change of the mission in this session (R-0165), so a contest
     * quote taken before the change is stale even when its values match again.
     * Not saved: a loaded game starts with new quotes.
     */
    private int missionRevision;

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
     * @param european required European nation
     * @return outcome of that nation's chief encounter here, empty before it
     */
    public Optional<NativeChiefOutcome> findChiefEncounter(
            final EuropeanNation european) {
        return Optional.ofNullable(chiefEncounters.get(european));
    }

    /**
     * Keep the one outcome of a nation's first chief encounter here. The model
     * checks the nation has none yet; restoring a save checks it as well.
     *
     * @param european required European nation
     * @param outcome  required drawn outcome
     */
    void recordChiefEncounter(final EuropeanNation european,
            final NativeChiefOutcome outcome) {
        Preconditions.checkNotNull(outcome);
        Preconditions.checkState(findChiefEncounter(european).isEmpty(),
                "Native chief encounter is already recorded");
        chiefEncounters.put(european, outcome);
    }

    /**
     * @return outcome of every recorded chief encounter, in European order,
     *         unmodifiable
     */
    Map<EuropeanNation, NativeChiefOutcome> getChiefEncounters() {
        return Collections.unmodifiableMap(chiefEncounters);
    }

    /**
     * @param european required European nation
     * @return turn of that nation's last tribute demand here, empty before it
     */
    public Optional<Integer> findTributeTurn(final EuropeanNation european) {
        Preconditions.checkNotNull(european);
        return Optional.ofNullable(tributeTurns.get(european));
    }

    /**
     * Keep the turn of a European's tribute demand here (R-0162). The model
     * checks that the wait has passed; restoring a save checks it as well.
     *
     * @param european required European nation
     * @param turn     nonnegative game turn of the demand
     */
    void recordTribute(final EuropeanNation european, final int turn) {
        Preconditions.checkNotNull(european);
        Preconditions.checkArgument(turn >= 0,
                "Native tribute turn cannot be negative");
        tributeTurns.put(european, turn);
    }

    /**
     * @return turn of every European's last tribute demand here, in European
     *         order, unmodifiable
     */
    Map<EuropeanNation, Integer> getTributeTurns() {
        return Collections.unmodifiableMap(tributeTurns);
    }

    /**
     * @return identity of the missionary whose mission this settlement holds,
     *         empty when none is established
     */
    public Optional<Integer> getMissionaryId() {
        return Optional.ofNullable(missionaryId);
    }

    /**
     * @return nonhostile owner updates counted in the current mission cycle,
     *         from zero to one less than the required count
     */
    public int getMissionUpdates() {
        return missionUpdates;
    }

    /**
     * @return number of changes made to the mission in this session (R-0165)
     */
    int getMissionRevision() {
        return missionRevision;
    }

    /**
     * Hold a new mission for a missionary the caller has validated (R-0164).
     * The settlement holds one mission at a time, so it must be free.
     *
     * @param missionary required identity of the missionary
     */
    void establishMission(final int missionary) {
        Preconditions.checkState(missionaryId == null,
                "Native mission is already established");
        missionaryId = missionary;
        missionUpdates = 0;
        missionRevision++;
    }

    /**
     * Drop the mission when its missionary leaves the game (R-0149), so no
     * settlement keeps an identity that no longer exists.
     */
    void clearMission() {
        missionaryId = null;
        missionUpdates = 0;
        missionRevision++;
    }

    /**
     * Restore a saved mission. The caller has validated the count.
     *
     * @param missionary required identity of the missionary
     * @param updates    counted nonhostile updates, below the required count
     */
    void restoreMission(final int missionary, final int updates) {
        missionaryId = missionary;
        missionUpdates = updates;
    }

    /**
     * Count one nonhostile owner update of the mission. Progress is never
     * banked: the fifth update ends the cycle and the count restarts at zero,
     * so each cycle gives exactly one boundary.
     *
     * @return whether this update was the fifth of the cycle
     */
    boolean addMissionUpdate() {
        Preconditions.checkState(missionaryId != null,
                "No native mission update is due");
        missionRevision++;
        missionUpdates++;
        if (missionUpdates < NativeMissionView.REQUIRED_UPDATES) {
            return false;
        }
        missionUpdates = 0;
        return true;
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
