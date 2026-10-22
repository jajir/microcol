package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved notice of a native convert (R-0149): it joined an owned colony as an
 * unassigned resident, or it returned to its tribe after the unassigned expiry.
 * Las Casas's join (R-0182) frees the converts already held; one notice covers
 * the whole join.
 */
public final class TurnEventNativeConvert extends AbstractTurnEvent {
    /** What happened to the convert. */
    public enum Kind {
        ACQUIRED, RETURNED, FREED
    }

    private final Kind kind;

    TurnEventNativeConvert(final String playerName, final Kind kind) {
        super(playerName);
        Preconditions.checkArgument(kind != null,
                "Invalid native convert notice");
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setNativeConvertKind(kind.name());
        return po;
    }

    public static TurnEventNativeConvert tryLoad(final TurnEventPo po) {
        if (!TurnEventNativeConvert.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(po.getNativeConvertKind() != null,
                "Incomplete native convert notice");
        return new TurnEventNativeConvert(po.getPlayerName(),
                Kind.valueOf(po.getNativeConvertKind()));
    }
}
