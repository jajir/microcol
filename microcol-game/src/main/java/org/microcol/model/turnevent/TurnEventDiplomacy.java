package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved notice of a diplomatic change made without the reader's decision: a
 * Crown-forced war or an alliance obligation to defend a victim.
 */
public final class TurnEventDiplomacy extends AbstractTurnEvent {
    /** Kind of the recorded change. */
    public enum Kind {
        CROWN_WAR, CROWN_WAR_ALLIANCE_ENDED, ALLY_DEFENSE
    }

    private final Kind kind;
    private final String rivalName;
    private final String victimName;

    TurnEventDiplomacy(final String playerName, final Kind kind,
            final String rivalName, final String victimName) {
        super(playerName);
        Preconditions.checkArgument(
                kind != null && rivalName != null && !rivalName.isBlank()
                        && (kind == Kind.ALLY_DEFENSE) == (victimName != null
                                && !victimName.isBlank()),
                "Invalid diplomacy notice");
        this.kind = kind;
        this.rivalName = rivalName;
        this.victimName = victimName;
    }

    public Kind getKind() {
        return kind;
    }

    public String getRivalName() {
        return rivalName;
    }

    /** @return the defended ally, only for {@link Kind#ALLY_DEFENSE} */
    public String getVictimName() {
        return victimName;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setDiplomacyKind(kind.name());
        po.setRivalName(rivalName);
        po.setDiplomacyVictimName(victimName);
        return po;
    }

    public static TurnEventDiplomacy tryLoad(final TurnEventPo po) {
        if (!TurnEventDiplomacy.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getDiplomacyKind() != null && po.getRivalName() != null,
                "Incomplete diplomacy notice");
        return new TurnEventDiplomacy(po.getPlayerName(),
                Kind.valueOf(po.getDiplomacyKind()), po.getRivalName(),
                po.getDiplomacyVictimName());
    }
}
