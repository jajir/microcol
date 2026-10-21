package org.microcol.model.turnevent;

import org.microcol.model.LostCityRumorRules;
import org.microcol.model.NativeNation;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Persisted notice of one resolved lost-city rumor. It records the actual
 * result, so Continue and the turn report never resolve it again.
 */
public final class TurnEventLostCityRumor extends AbstractTurnEvent {

    private final LostCityRumorRules.Outcome outcome;
    private final int explorerId;
    private final int gold;
    /** Nation of the disturbed burial site; null when none was near. */
    private final NativeNation nation;
    private final boolean deSotoAverted;

    public TurnEventLostCityRumor(final String playerName,
            final LostCityRumorRules.Outcome outcome, final int explorerId,
            final int gold, final NativeNation nation,
            final boolean deSotoAverted) {
        super(playerName);
        this.outcome = Preconditions.checkNotNull(outcome);
        Preconditions.checkArgument(explorerId >= 0 && gold >= 0,
                "Invalid lost-city rumor event");
        this.explorerId = explorerId;
        this.gold = gold;
        this.nation = nation;
        this.deSotoAverted = deSotoAverted;
    }

    public LostCityRumorRules.Outcome getOutcome() {
        return outcome;
    }

    public int getExplorerId() {
        return explorerId;
    }

    public int getGold() {
        return gold;
    }

    /**
     * @return nation of the disturbed burial site, or null when no native
     *         settlement was near
     */
    public NativeNation getNation() {
        return nation;
    }

    /** Whether De Soto turned a bad result into an empty one. */
    public boolean isDeSotoAverted() {
        return deSotoAverted;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setRumorOutcome(outcome.name());
        po.setRumorExplorerId(explorerId);
        po.setRumorGold(gold);
        po.setRumorNation(nation);
        po.setRumorDeSotoAverted(deSotoAverted);
        return po;
    }

    public static TurnEventLostCityRumor tryLoad(final TurnEventPo po) {
        if (!TurnEventLostCityRumor.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getRumorOutcome() != null && po.getRumorExplorerId() != null
                        && po.getRumorGold() != null
                        && po.isRumorDeSotoAverted() != null,
                "Incomplete lost-city rumor event");
        return new TurnEventLostCityRumor(po.getPlayerName(),
                LostCityRumorRules.Outcome.valueOf(po.getRumorOutcome()),
                po.getRumorExplorerId(), po.getRumorGold(), po.getRumorNation(),
                po.isRumorDeSotoAverted());
    }
}
