package org.microcol.model;

import java.util.Map;
import java.util.Set;

import org.microcol.model.store.PlayerPo;

import com.google.common.base.Preconditions;

/**
 * Native nation as a participant of the shared turn order. It owns braves
 * through the same unit operations as a European player, but it has no
 * colonies, Europe trade, Congress, tax or immigration. Its turn is only the
 * shared unit preparation, so the nation is never a disguised European player.
 */
public final class PlayerNative extends Player {

    private final NativeNation nation;

    /** Model whose settlements this nation's own updates maintain. */
    private final Model nationModel;

    PlayerNative(final String name, final NativeNation nation,
            final int initialGold, final Model model,
            final Map<String, Object> extraData, final Set<Location> visible) {
        super(name, true, initialGold, model, false, extraData, visible, null,
                null, null);
        this.nation = Preconditions.checkNotNull(nation);
        this.nationModel = Preconditions.checkNotNull(model);
        Preconditions.checkArgument(name.equals(nation.name()),
                "Native player name must be its nation");
    }

    /**
     * @return the native nation this participant stands for
     */
    public NativeNation getNativeNation() {
        return nation;
    }

    @Override
    public boolean isNative() {
        return true;
    }

    /**
     * Native preparation is the unit update and one market update (R-0159), the
     * latter once per nation turn. Ships, Europe, colonies, relations, tax and
     * immigration phases do not apply to a native nation.
     *
     * @return always true: a native nation never waits for a European answer
     */
    @Override
    boolean startTurn() {
        getAllUnits().forEach(unit -> unit.startTurn());
        nationModel.updateNativeMarkets(nation);
        return true;
    }

    @Override
    public PlayerPo save() {
        final PlayerPo out = super.save();
        out.setNativeNation(nation);
        return out;
    }

}
