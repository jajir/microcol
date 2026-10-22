package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.microcol.model.store.NativeArmsSalePo;
import org.microcol.model.store.NativeLocalAlarmPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.NativeTribalAngerPo;

import com.google.common.base.Preconditions;

/**
 * Persistent native relations toward each European: tribal anger per native
 * nation and local alarm per native settlement. Both are fractional values in
 * 0..100. Only the model updates them; the interface and AI read the shared
 * {@link NativeHostility}.
 */
public final class NativeRelations {

    /** Reduction of each measure at every peaceful European update. */
    static final double PEACEFUL_DECAY = 1;

    /** Divisor of mean local alarm that is added to tribal anger. */
    static final double ALARM_TO_ANGER_DIVISOR = 20;

    /** Tribal anger of an attack on a native unit. */
    private static final double UNIT_ATTACK_ANGER = 10;

    /** Local alarm of an attack on a native unit with a known home. */
    private static final double UNIT_ATTACK_ALARM = 10;

    /** Raise of both measures for an attack on a settlement. */
    private static final double SETTLEMENT_ATTACK_RAISE = 20;

    /** Extra raise of both measures when the attack destroys a settlement. */
    private static final double DESTRUCTION_RAISE = 20;

    /** Tribal anger of a disturbed burial site. */
    private static final double BURIAL_ANGER = 30;

    /** Raise of both measures for a tribute demand, paid or refused. */
    private static final double TRIBUTE_RAISE = 10;

    /** Owner-local alarm reduction of a mission with an ordinary missionary. */
    static final double MISSION_ORDINARY_REDUCTION = 2;

    /** Owner-local alarm reduction of a mission with an expert missionary. */
    static final double MISSION_EXPERT_REDUCTION = 4;

    /** Rival-local alarm raise of each mission update, per contacted rival. */
    static final double MISSION_RIVAL_RAISE = 1;

    /** Local alarm reduction of a completed trade. */
    private static final double TRADE_REDUCTION = 2;

    /** Local alarm reduction of a gift per 100 goods. */
    private static final double GIFT_REDUCTION_PER_HUNDRED = 5;

    /** Multiplier of settlement raises and peaceful reductions at a capital. */
    private static final double CAPITAL_MULTIPLIER = 2;

    /** Muskets and horses sold per tribal anger step. */
    private static final int ARMS_STEP = 50;

    /** Tribal anger of each complete arms step. */
    private static final double ARMS_STEP_ANGER = 5;

    private static final double MIN_VALUE = 0;

    private static final double MAX_VALUE = 100;

    private final List<NativeSettlement> settlements;

    private final Map<NativeNation, Map<EuropeanNation, Double>> tribalAnger;

    private final Map<Integer, Map<EuropeanNation, Double>> localAlarm;

    /**
     * Local alarm at the start of the European's last turn; the difference to
     * {@link #localAlarm} is the alarm change the player sees.
     */
    private final Map<Integer, Map<EuropeanNation, Double>> previousLocalAlarm;

    /** Muskets and horses sold below the next step, per pair; 0..49. */
    private final Map<EuropeanNation, Map<NativeNation, Integer>> armsRemainders;

    /** Origin action IDs already applied; never applied again. */
    private final Set<Long> appliedOutcomes;

    private NativeRelations(final List<NativeSettlement> settlements,
            final Map<NativeNation, Map<EuropeanNation, Double>> tribalAnger,
            final Map<Integer, Map<EuropeanNation, Double>> localAlarm,
            final Map<Integer, Map<EuropeanNation, Double>> previousLocalAlarm,
            final Map<EuropeanNation, Map<NativeNation, Integer>> armsRemainders,
            final Set<Long> appliedOutcomes) {
        this.settlements = new ArrayList<>(settlements);
        this.tribalAnger = tribalAnger;
        this.localAlarm = localAlarm;
        this.previousLocalAlarm = previousLocalAlarm;
        this.armsRemainders = armsRemainders;
        this.appliedOutcomes = appliedOutcomes;
    }

    /**
     * Saved table for a new game: every native nation and European pair starts
     * at zero.
     *
     * @return explicit zero entries for all eight nations and Europeans
     */
    public static List<NativeTribalAngerPo> freshTribalAnger() {
        final List<NativeTribalAngerPo> out = new ArrayList<>();
        for (final NativeNation nation : NativeNation.values()) {
            for (final EuropeanNation european : EuropeanNation.values()) {
                out.add(tribalPo(nation, european, MIN_VALUE));
            }
        }
        return out;
    }

    /**
     * Saved table for a new game: every settlement and European pair starts at
     * zero.
     *
     * @param settlements required saved settlements of the new world
     * @return explicit zero entries for every settlement and European
     */
    public static List<NativeLocalAlarmPo> freshLocalAlarm(
            final List<NativeSettlementPo> settlements) {
        Preconditions.checkNotNull(settlements);
        final List<NativeLocalAlarmPo> out = new ArrayList<>();
        for (final NativeSettlementPo settlement : settlements) {
            for (final EuropeanNation european : EuropeanNation.values()) {
                out.add(alarmPo(settlement.getId(), european, MIN_VALUE,
                        MIN_VALUE));
            }
        }
        return out;
    }

    /**
     * Build the relations from the saved tables. Both tables are required and
     * complete: every pair appears exactly once, with a finite value in 0..100.
     * Unknown or duplicate keys are rejected; nothing is guessed. The previous
     * alarm of a pair is optional: when absent it equals the saved alarm, so
     * the change reads as zero until an update is observed. The arms remainders
     * and applied outcome IDs are sparse: an absent list means none is retained
     * or applied yet.
     *
     * @param registry        required validated native settlements
     * @param savedAnger      required tribal anger table, possibly empty
     * @param savedAlarm      required local alarm table, possibly empty
     * @param savedArms       arms remainders, nullable meaning none
     * @param savedOutcomeIds applied outcome action IDs, nullable meaning none
     * @return validated relations
     */
    static NativeRelations make(final NativeSettlements registry,
            final List<NativeTribalAngerPo> savedAnger,
            final List<NativeLocalAlarmPo> savedAlarm,
            final List<NativeArmsSalePo> savedArms,
            final List<Long> savedOutcomeIds) {
        Preconditions.checkArgument(savedAnger != null,
                "Unsupported save: missing native tribal anger");
        Preconditions.checkArgument(savedAlarm != null,
                "Unsupported save: missing native local alarm");
        final Map<NativeNation, Map<EuropeanNation, Double>> anger = new EnumMap<>(
                NativeNation.class);
        for (final NativeTribalAngerPo entry : savedAnger) {
            Preconditions.checkArgument(
                    entry != null && entry.getNation() != null
                            && entry.getEuropean() != null
                            && isValid(entry.getValue()),
                    "Unsupported save: invalid native tribal anger");
            Preconditions
                    .checkArgument(anger
                            .computeIfAbsent(entry.getNation(),
                                    nation -> new EnumMap<>(
                                            EuropeanNation.class))
                            .put(entry.getEuropean(), entry.getValue()) == null,
                            "Unsupported save: duplicate native tribal anger");
        }
        Preconditions.checkArgument(
                savedAnger.size() == NativeNation.values().length
                        * EuropeanNation.values().length,
                "Unsupported save: incomplete native tribal anger");
        final List<NativeSettlement> all = registry.getAll();
        final Set<Integer> ids = new HashSet<>();
        all.forEach(settlement -> ids.add(settlement.getId()));
        final Map<Integer, Map<EuropeanNation, Double>> alarm = new HashMap<>();
        final Map<Integer, Map<EuropeanNation, Double>> previous = new HashMap<>();
        for (final NativeLocalAlarmPo entry : savedAlarm) {
            Preconditions.checkArgument(
                    entry != null && entry.getSettlementId() != null
                            && ids.contains(entry.getSettlementId())
                            && entry.getEuropean() != null
                            && isValid(entry.getValue())
                            && (entry.getPreviousValue() == null
                                    || isValid(entry.getPreviousValue())),
                    "Unsupported save: invalid native local alarm");
            Preconditions.checkArgument(
                    alarm.computeIfAbsent(entry.getSettlementId(),
                            id -> new EnumMap<>(EuropeanNation.class))
                            .put(entry.getEuropean(), entry.getValue()) == null,
                    "Unsupported save: duplicate native local alarm");
            previous.computeIfAbsent(entry.getSettlementId(),
                    id -> new EnumMap<>(EuropeanNation.class))
                    .put(entry.getEuropean(),
                            entry.getPreviousValue() == null ? entry.getValue()
                                    : entry.getPreviousValue());
        }
        Preconditions.checkArgument(
                savedAlarm.size() == ids.size()
                        * EuropeanNation.values().length,
                "Unsupported save: incomplete native local alarm");
        final Map<EuropeanNation, Map<NativeNation, Integer>> arms = new EnumMap<>(
                EuropeanNation.class);
        for (final EuropeanNation european : EuropeanNation.values()) {
            arms.put(european, new EnumMap<>(NativeNation.class));
        }
        for (final NativeArmsSalePo entry : savedArms == null
                ? List.<NativeArmsSalePo>of()
                : savedArms) {
            Preconditions.checkArgument(entry != null
                    && entry.getNation() != null && entry.getEuropean() != null
                    && entry.getQuantity() != null && entry.getQuantity() >= 0
                    && entry.getQuantity() < ARMS_STEP,
                    "Unsupported save: invalid native arms sale remainder");
            Preconditions.checkArgument(
                    arms.get(entry.getEuropean()).put(entry.getNation(),
                            entry.getQuantity()) == null,
                    "Unsupported save: duplicate native arms sale remainder");
        }
        final Set<Long> applied = new TreeSet<>();
        for (final Long id : savedOutcomeIds == null ? List.<Long>of()
                : savedOutcomeIds) {
            Preconditions.checkArgument(id != null && id > 0 && applied.add(id),
                    "Unsupported save: invalid or duplicate native relation outcome");
        }
        return new NativeRelations(all, anger, alarm, previous, arms, applied);
    }

    /**
     * @return saved form of every tribal anger pair, in a fixed order
     */
    List<NativeTribalAngerPo> saveTribalAnger() {
        final List<NativeTribalAngerPo> out = new ArrayList<>();
        for (final NativeNation nation : NativeNation.values()) {
            for (final EuropeanNation european : EuropeanNation.values()) {
                out.add(tribalPo(nation, european,
                        tribalAnger.get(nation).get(european)));
            }
        }
        return out;
    }

    /**
     * @return saved form of every local alarm pair, in the settlement order
     */
    List<NativeLocalAlarmPo> saveLocalAlarm() {
        final List<NativeLocalAlarmPo> out = new ArrayList<>();
        for (final NativeSettlement settlement : settlements) {
            for (final EuropeanNation european : EuropeanNation.values()) {
                out.add(alarmPo(settlement.getId(), european,
                        localAlarm.get(settlement.getId()).get(european),
                        previousLocalAlarm.get(settlement.getId())
                                .get(european)));
            }
        }
        return out;
    }

    /**
     * @return saved nonzero arms remainders, in a fixed order
     */
    List<NativeArmsSalePo> saveArmsSales() {
        final List<NativeArmsSalePo> out = new ArrayList<>();
        for (final EuropeanNation european : EuropeanNation.values()) {
            for (final NativeNation nation : NativeNation.values()) {
                final int quantity = armsRemainders.get(european)
                        .getOrDefault(nation, 0);
                if (quantity > 0) {
                    final NativeArmsSalePo po = new NativeArmsSalePo();
                    po.setNation(nation);
                    po.setEuropean(european);
                    po.setQuantity(quantity);
                    out.add(po);
                }
            }
        }
        return out;
    }

    /**
     * @return saved IDs of applied outcomes, in ascending order
     */
    List<Long> saveAppliedOutcomes() {
        return new ArrayList<>(appliedOutcomes);
    }

    /**
     * Origin ID for a new completed outcome, above every applied ID so no
     * producer reuses one. The caller applies the outcome in the same committed
     * action; two IDs allocated before either is applied would collide.
     *
     * @return unused positive action ID
     */
    long nextOutcomeId() {
        return appliedOutcomes.stream().mapToLong(Long::longValue).max()
                .orElse(0L) + 1;
    }

    double getTribalAnger(final NativeNation nation,
            final EuropeanNation european) {
        return tribalAnger.get(nation).get(european);
    }

    double getLocalAlarm(final NativeSettlement settlement,
            final EuropeanNation european) {
        return localAlarm.get(settlement.getId()).get(european);
    }

    /**
     * @param settlement required settlement
     * @param european   required European
     * @return hostility from the larger of the nation's tribal anger and this
     *         settlement's local alarm toward that European
     */
    NativeHostility getHostility(final NativeSettlement settlement,
            final EuropeanNation european) {
        return NativeHostility.of(getHostilityValue(settlement, european));
    }

    /**
     * @param settlement required settlement
     * @param european   required European
     * @return the exact number behind {@link #getHostility}: the larger of the
     *         nation's tribal anger and this settlement's local alarm, 0 to 100
     */
    double getHostilityValue(final NativeSettlement settlement,
            final EuropeanNation european) {
        return Math.max(getTribalAnger(settlement.getNation(), european),
                getLocalAlarm(settlement, european));
    }

    /**
     * Local alarm change of one pair: the current alarm minus the alarm at the
     * start of the European's last turn. Committed outcomes and the turn's own
     * update move the current alarm, so their effect shows here until the next
     * turn starts.
     *
     * @param settlement required settlement
     * @param european   required European
     * @return signed change, exact and fractional; zero when nothing moved
     */
    double getLocalAlarmChange(final NativeSettlement settlement,
            final EuropeanNation european) {
        return getLocalAlarm(settlement, european)
                - previousLocalAlarm.get(settlement.getId()).get(european);
    }

    /**
     * Drop the rows of a conquered settlement (R-0174). Its local alarm no
     * longer feeds any tribe, and tribal anger of its nation is kept as it
     * stands.
     *
     * @param settlement required settlement that was conquered
     */
    void removeSettlement(final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        Preconditions.checkArgument(settlements.remove(settlement),
                "Native settlement is not registered");
        localAlarm.remove(settlement.getId());
        previousLocalAlarm.remove(settlement.getId());
    }

    /**
     * Start the alarm observation of one European at the start of its own turn,
     * before congress settles and before its update. The change shown
     * afterwards is measured from here, so an arrival that clears alarm still
     * reads as a fall.
     *
     * @param european required European whose turn has started
     */
    void observeLocalAlarm(final EuropeanNation european) {
        Preconditions.checkNotNull(european);
        for (final NativeSettlement settlement : settlements) {
            previousLocalAlarm.get(settlement.getId()).put(european,
                    localAlarm.get(settlement.getId()).get(european));
        }
    }

    /**
     * Apply one peaceful update of one European exactly once. Each measure
     * first decays, then the summed local pressure is multiplied by the
     * national and founder factor, and finally the mean of the nation's local
     * alarms is added to its tribal anger.
     *
     * @param european    required European whose relations are updated
     * @param pressure    summed positive local inputs per settlement id; absent
     *                    ids have none
     * @param localFactor multiplier of positive local alarm, 1 by default
     */
    void applyEuropeanUpdate(final EuropeanNation european,
            final Map<Integer, Double> pressure, final double localFactor) {
        Preconditions.checkNotNull(european);
        Preconditions.checkNotNull(pressure);
        for (final NativeSettlement settlement : settlements) {
            final Map<EuropeanNation, Double> row = localAlarm
                    .get(settlement.getId());
            final double decayed = clamp(row.get(european) - PEACEFUL_DECAY);
            row.put(european,
                    clamp(decayed
                            + pressure.getOrDefault(settlement.getId(), 0.0)
                                    * localFactor));
        }
        for (final NativeNation nation : NativeNation.values()) {
            final Map<EuropeanNation, Double> row = tribalAnger.get(nation);
            final double decayed = clamp(row.get(european) - PEACEFUL_DECAY);
            row.put(european, clamp(decayed + meanLocalAlarm(nation, european)
                    / ALARM_TO_ANGER_DIVISOR));
        }
    }

    /**
     * Direct tribal change, such as a later trigger from R-0154. It is never
     * reduced by a national or founder factor.
     *
     * @param nation   required native nation
     * @param european required European
     * @param amount   finite nonnegative increase before clamping
     */
    void addTribalAnger(final NativeNation nation,
            final EuropeanNation european, final double amount) {
        checkAmount(amount);
        final Map<EuropeanNation, Double> row = tribalAnger.get(nation);
        row.put(european, clamp(row.get(european) + amount));
    }

    /**
     * Raise tribal anger to at least a floor, for a successful incitement
     * (R-0166). A higher anger is kept; the national and founder factors never
     * apply to it.
     *
     * @param nation   required native nation
     * @param european required European
     * @param floor    finite value from 0 to 100
     */
    void raiseTribalAngerTo(final NativeNation nation,
            final EuropeanNation european, final double floor) {
        Preconditions.checkArgument(isValid(floor),
                "Native tribal anger floor is out of range");
        final Map<EuropeanNation, Double> row = tribalAnger.get(nation);
        row.put(european, Math.max(row.get(european), floor));
    }

    /**
     * Local positive pressure of one settlement, such as a later trigger from
     * R-0154. The national and founder factor applies here.
     *
     * @param settlement  required settlement
     * @param european    required European
     * @param amount      finite nonnegative increase before the factor
     * @param localFactor multiplier of positive local alarm
     */
    void addLocalAlarm(final NativeSettlement settlement,
            final EuropeanNation european, final double amount,
            final double localFactor) {
        checkAmount(amount);
        final Map<EuropeanNation, Double> row = localAlarm
                .get(settlement.getId());
        row.put(european, clamp(row.get(european) + amount * localFactor));
    }

    /**
     * Owner-local reduction of a mission (R-0164). Only the owner's local alarm
     * of that settlement moves: no tribal anger is reduced, the national factor
     * never applies to a reduction, and the clamp keeps the alarm in 0..100.
     * Tribal anger follows at the owner's next peaceful update.
     *
     * @param settlement required settlement of the mission
     * @param european   required owner of the mission
     * @param reduction  finite nonnegative amount to remove
     */
    void lowerLocalAlarm(final NativeSettlement settlement,
            final EuropeanNation european, final double reduction) {
        checkAmount(reduction);
        final Map<EuropeanNation, Double> row = localAlarm
                .get(settlement.getId());
        row.put(european, clamp(row.get(european) - reduction));
    }

    /**
     * Pocahontas's arrival for one European: its positive tribal anger and
     * local alarm become zero once. The caller must invoke it only at arrival.
     *
     * @param european required European whose members have just joined
     */
    void clearEuropean(final EuropeanNation european) {
        Preconditions.checkNotNull(european);
        tribalAnger.values().forEach(row -> row.put(european, MIN_VALUE));
        localAlarm.values().forEach(row -> row.put(european, MIN_VALUE));
    }

    /**
     * Whether {@link #applyOutcome} would accept this outcome now, without
     * changing anything. A transaction checks all its outcomes with this before
     * its first change, so a later refusal cannot leave a half-applied sale.
     *
     * @param european required colonial European whose action completed
     * @param outcome  required outcome not yet applied
     * @return whether its ID is unused and its references and amounts are valid
     */
    boolean canApply(final EuropeanNation european,
            final NativeRelationOutcome outcome) {
        Preconditions.checkNotNull(european);
        Preconditions.checkNotNull(outcome);
        if (appliedOutcomes.contains(outcome.getActionId())) {
            return false;
        }
        final NativeSettlement settlement = outcome.getSettlement();
        if (settlement != null && (!settlements.contains(settlement)
                || settlement.getNation() != outcome.getNation())) {
            return false;
        }
        return outcome.getKind() != NativeRelationOutcome.Kind.ARMS_SALE
                || canAddArms(european, outcome);
    }

    private boolean canAddArms(final EuropeanNation european,
            final NativeRelationOutcome outcome) {
        final int remainder = armsRemainders.get(european)
                .getOrDefault(outcome.getNation(), 0);
        try {
            Math.addExact(remainder, outcome.getQuantity());
            return true;
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    /**
     * Apply one completed outcome exactly once. Every reference and amount is
     * checked before the first change, so a refused outcome leaves all
     * measures, arms remainders and applied IDs unchanged. Raises of a
     * settlement's local alarm take the national and founder factor; direct
     * tribal changes and peaceful reductions never do.
     *
     * @param european    required colonial European whose action completed
     * @param outcome     required completed outcome
     * @param localFactor multiplier of positive local alarm of that European
     */
    void applyOutcome(final EuropeanNation european,
            final NativeRelationOutcome outcome, final double localFactor) {
        Preconditions.checkNotNull(european);
        Preconditions.checkNotNull(outcome);
        Preconditions.checkArgument(
                !appliedOutcomes.contains(outcome.getActionId()),
                "Native relation outcome already applied");
        final NativeNation nation = outcome.getNation();
        final NativeSettlement settlement = outcome.getSettlement();
        if (settlement != null) {
            Preconditions.checkArgument(settlements.contains(settlement),
                    "Unknown native settlement");
            Preconditions.checkArgument(settlement.getNation() == nation,
                    "Native settlement belongs to another nation");
        }
        // All references are valid, so only the changes below remain.
        final double multiplier = settlement != null && settlement.isCapital()
                ? CAPITAL_MULTIPLIER
                : 1;
        switch (outcome.getKind()) {
        case UNIT_ATTACK:
            addTribalAnger(nation, european, UNIT_ATTACK_ANGER);
            if (settlement != null) {
                addLocalAlarm(settlement, european, UNIT_ATTACK_ALARM,
                        localFactor);
            }
            break;
        case SETTLEMENT_ATTACK:
            raise(nation, settlement, european,
                    SETTLEMENT_ATTACK_RAISE * multiplier, localFactor);
            break;
        case SETTLEMENT_DESTROYED:
            raise(nation, settlement, european,
                    (SETTLEMENT_ATTACK_RAISE + DESTRUCTION_RAISE) * multiplier,
                    localFactor);
            break;
        case BURIAL:
            addTribalAnger(nation, european, BURIAL_ANGER);
            break;
        case TRIBUTE:
            raise(nation, settlement, european, TRIBUTE_RAISE, localFactor);
            break;
        case TRADE:
            lower(nation, settlement, european, TRADE_REDUCTION);
            break;
        case GIFT:
            lower(nation, settlement, european,
                    GIFT_REDUCTION_PER_HUNDRED * outcome.getQuantity() / 100);
            break;
        case ARMS_SALE:
            sellArms(nation, european, outcome.getQuantity());
            break;
        }
        appliedOutcomes.add(outcome.getActionId());
    }

    /** Raise tribal anger directly and the settlement's local alarm. */
    private void raise(final NativeNation nation,
            final NativeSettlement settlement, final EuropeanNation european,
            final double amount, final double localFactor) {
        addTribalAnger(nation, european, amount);
        addLocalAlarm(settlement, european, amount, localFactor);
    }

    /**
     * Peaceful reduction of the settlement's local alarm; at a capital it is
     * doubled and also reduces the nation's tribal anger.
     */
    private void lower(final NativeNation nation,
            final NativeSettlement settlement, final EuropeanNation european,
            final double reduction) {
        final boolean capital = settlement.isCapital();
        final double lowered = capital ? CAPITAL_MULTIPLIER * reduction
                : reduction;
        final Map<EuropeanNation, Double> alarmRow = localAlarm
                .get(settlement.getId());
        alarmRow.put(european, clamp(alarmRow.get(european) - lowered));
        if (capital) {
            final Map<EuropeanNation, Double> angerRow = tribalAnger
                    .get(nation);
            angerRow.put(european, clamp(angerRow.get(european) - lowered));
        }
    }

    /**
     * Sold muskets and horses join the retained remainder; each complete step
     * adds tribal anger and the new remainder is kept below the step.
     */
    private void sellArms(final NativeNation nation,
            final EuropeanNation european, final int quantity) {
        final Map<NativeNation, Integer> row = armsRemainders.get(european);
        final int total = Math.addExact(row.getOrDefault(nation, 0), quantity);
        row.put(nation, total % ARMS_STEP);
        addTribalAnger(nation, european, ARMS_STEP_ANGER * (total / ARMS_STEP));
    }

    private double meanLocalAlarm(final NativeNation nation,
            final EuropeanNation european) {
        double sum = 0;
        int count = 0;
        for (final NativeSettlement settlement : settlements) {
            if (settlement.getNation() == nation) {
                sum += localAlarm.get(settlement.getId()).get(european);
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    private static void checkAmount(final double amount) {
        Preconditions.checkArgument(Double.isFinite(amount) && amount >= 0,
                "Native relation change must be finite and nonnegative");
    }

    private static boolean isValid(final Double value) {
        return value != null && Double.isFinite(value) && value >= MIN_VALUE
                && value <= MAX_VALUE;
    }

    private static double clamp(final double value) {
        return Math.min(MAX_VALUE, Math.max(MIN_VALUE, value));
    }

    private static NativeTribalAngerPo tribalPo(final NativeNation nation,
            final EuropeanNation european, final double value) {
        final NativeTribalAngerPo po = new NativeTribalAngerPo();
        po.setNation(nation);
        po.setEuropean(european);
        po.setValue(value);
        return po;
    }

    private static NativeLocalAlarmPo alarmPo(final int settlementId,
            final EuropeanNation european, final double value,
            final double previous) {
        final NativeLocalAlarmPo po = new NativeLocalAlarmPo();
        po.setSettlementId(settlementId);
        po.setEuropean(european);
        po.setValue(value);
        po.setPreviousValue(previous);
        return po;
    }

}
