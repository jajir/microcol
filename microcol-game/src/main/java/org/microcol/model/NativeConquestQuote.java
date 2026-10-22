package org.microcol.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Terms of one attack on a native settlement (R-0174), frozen for confirmation.
 * It names the attacker, the settlement, the actual native defenders in battle
 * order, and the frozen strengths of every one of those battles, plus the two
 * bonuses shown to the player. Confirmation is valid only while a fresh quote
 * equals this one, so a changed later defender is refused before any draw.
 */
public final class NativeConquestQuote {

    private final NativeSettlement settlement;
    private final Unit attacker;
    private final List<Unit> defenders;
    private final List<CombatStrengths> battles;
    private final int defenseBonusPercent;
    private final int spanishBonusPercent;
    private final NativeHostility hostility;

    NativeConquestQuote(final NativeSettlement settlement, final Unit attacker,
            final List<Unit> defenders, final List<CombatStrengths> battles,
            final int defenseBonusPercent, final int spanishBonusPercent,
            final NativeHostility hostility) {
        this.settlement = Objects.requireNonNull(settlement);
        this.attacker = Objects.requireNonNull(attacker);
        this.defenders = List.copyOf(defenders);
        this.battles = List.copyOf(battles);
        this.defenseBonusPercent = defenseBonusPercent;
        this.spanishBonusPercent = spanishBonusPercent;
        this.hostility = Objects.requireNonNull(hostility);
    }

    /**
     * @return the settlement attacked
     */
    public NativeSettlement getSettlement() {
        return settlement;
    }

    /**
     * @return the unit that would attack
     */
    public Unit getAttacker() {
        return attacker;
    }

    /**
     * @return the actual defenders on the settlement square, strongest first
     */
    public List<Unit> getDefenders() {
        return defenders;
    }

    /**
     * @return frozen strengths of each battle, aligned with
     *         {@link #getDefenders}
     */
    public List<CombatStrengths> getBattles() {
        return battles;
    }

    /**
     * @return the strengths of the first battle, the one the player is shown
     */
    public CombatStrengths getFirstBattle() {
        return battles.get(0);
    }

    /**
     * @return the settlement's defense bonus applied to its defenders
     */
    public int getDefenseBonusPercent() {
        return defenseBonusPercent;
    }

    /**
     * @return the Spanish attack bonus, 50 for a Spanish attacker and 0 for any
     *         other, applied to native settlements only
     */
    public int getSpanishBonusPercent() {
        return spanishBonusPercent;
    }

    /**
     * @return the attitude of the settlement toward the attacker's nation
     */
    public NativeHostility getHostility() {
        return hostility;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NativeConquestQuote)) {
            return false;
        }
        final NativeConquestQuote that = (NativeConquestQuote) other;
        return settlement == that.settlement && attacker == that.attacker
                && defenderIds().equals(that.defenderIds())
                && battles.equals(that.battles)
                && defenseBonusPercent == that.defenseBonusPercent
                && spanishBonusPercent == that.spanishBonusPercent
                && hostility == that.hostility;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settlement.getId(), attacker.getId(), defenderIds(),
                battles, defenseBonusPercent, spanishBonusPercent, hostility);
    }

    private List<Integer> defenderIds() {
        return defenders.stream().map(Unit::getId).collect(Collectors.toList());
    }
}
