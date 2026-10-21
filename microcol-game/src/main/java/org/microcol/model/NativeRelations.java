package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final double MIN_VALUE = 0;

    private static final double MAX_VALUE = 100;

    private final List<NativeSettlement> settlements;

    private final Map<NativeNation, Map<EuropeanNation, Double>> tribalAnger;

    private final Map<Integer, Map<EuropeanNation, Double>> localAlarm;

    private NativeRelations(final List<NativeSettlement> settlements,
            final Map<NativeNation, Map<EuropeanNation, Double>> tribalAnger,
            final Map<Integer, Map<EuropeanNation, Double>> localAlarm) {
        this.settlements = settlements;
        this.tribalAnger = tribalAnger;
        this.localAlarm = localAlarm;
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
                out.add(alarmPo(settlement.getId(), european, MIN_VALUE));
            }
        }
        return out;
    }

    /**
     * Build the relations from the saved tables. Both tables are required and
     * complete: every pair appears exactly once, with a finite value in 0..100.
     * Unknown or duplicate keys are rejected; nothing is guessed.
     *
     * @param registry   required validated native settlements
     * @param savedAnger required tribal anger table, possibly empty
     * @param savedAlarm required local alarm table, possibly empty
     * @return validated relations
     */
    static NativeRelations make(final NativeSettlements registry,
            final List<NativeTribalAngerPo> savedAnger,
            final List<NativeLocalAlarmPo> savedAlarm) {
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
        for (final NativeLocalAlarmPo entry : savedAlarm) {
            Preconditions.checkArgument(
                    entry != null && entry.getSettlementId() != null
                            && ids.contains(entry.getSettlementId())
                            && entry.getEuropean() != null
                            && isValid(entry.getValue()),
                    "Unsupported save: invalid native local alarm");
            Preconditions.checkArgument(
                    alarm.computeIfAbsent(entry.getSettlementId(),
                            id -> new EnumMap<>(EuropeanNation.class))
                            .put(entry.getEuropean(), entry.getValue()) == null,
                    "Unsupported save: duplicate native local alarm");
        }
        Preconditions.checkArgument(
                savedAlarm.size() == ids.size()
                        * EuropeanNation.values().length,
                "Unsupported save: incomplete native local alarm");
        return new NativeRelations(all, anger, alarm);
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
                        localAlarm.get(settlement.getId()).get(european)));
            }
        }
        return out;
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
        return NativeHostility
                .of(Math.max(getTribalAnger(settlement.getNation(), european),
                        getLocalAlarm(settlement, european)));
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
            final EuropeanNation european, final double value) {
        final NativeLocalAlarmPo po = new NativeLocalAlarmPo();
        po.setSettlementId(settlementId);
        po.setEuropean(european);
        po.setValue(value);
        return po;
    }

}
