package org.microcol.model.turnevent;

import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved notice of a native course that ended without the player's decision: the
 * skill was granted after the required owner updates, or the natives turned
 * tense and the colonist was released without it (R-0148).
 */
public final class TurnEventNativeTeaching extends AbstractTurnEvent {
    /** How the native course ended. */
    public enum Kind {
        COMPLETED, CANCELLED_HOSTILITY
    }

    private final Kind kind;
    private final UnitType skill;

    TurnEventNativeTeaching(final String playerName, final Kind kind,
            final UnitType skill) {
        super(playerName);
        Preconditions.checkArgument(kind != null && skill != null,
                "Invalid native teaching notice");
        this.kind = kind;
        this.skill = skill;
    }

    public Kind getKind() {
        return kind;
    }

    /** @return the skill the course offered */
    public UnitType getSkill() {
        return skill;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setNativeTeachingKind(kind.name());
        po.setNativeTeachingSkill(skill.name());
        return po;
    }

    public static TurnEventNativeTeaching tryLoad(final TurnEventPo po) {
        if (!TurnEventNativeTeaching.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getNativeTeachingKind() != null
                        && po.getNativeTeachingSkill() != null,
                "Incomplete native teaching notice");
        return new TurnEventNativeTeaching(po.getPlayerName(),
                Kind.valueOf(po.getNativeTeachingKind()),
                UnitType.valueOf(po.getNativeTeachingSkill()));
    }
}
