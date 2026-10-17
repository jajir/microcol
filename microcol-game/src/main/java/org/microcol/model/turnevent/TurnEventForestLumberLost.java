package org.microcol.model.turnevent;

import java.util.Optional;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * A persisted notice that lumber from a cleared forest was lost, because the
 * nearest colony's warehouse was too full or because no colony was within
 * reach. The colony is reported only when one received lumber; otherwise the
 * notice carries the cleared square alone.
 */
public final class TurnEventForestLumberLost extends AbstractTurnEvent {

    private final Goods goods;

    private final Location location;

    private final Integer colonyId;

    private final String colonyName;

    TurnEventForestLumberLost(final String playerName, final Goods goods,
            final Location location, final Integer colonyId,
            final String colonyName) {
        super(playerName);
        Preconditions.checkNotNull(goods, "Lost goods are required");
        Preconditions.checkArgument(
                goods.getType() == GoodsType.LUMBER && goods.getAmount() > 0,
                "Invalid lost lumber %s", goods);
        Preconditions.checkNotNull(location, "Cleared square is required");
        Preconditions.checkArgument(
                (colonyId == null) == (colonyName == null) && (colonyId == null
                        || (colonyId >= 0 && !colonyName.isBlank())),
                "Invalid recipient colony");
        this.goods = goods;
        this.location = location;
        this.colonyId = colonyId;
        this.colonyName = colonyName;
    }

    /** @return the lumber that was discarded, never more than was cleared */
    public Goods getGoods() {
        return goods;
    }

    /** @return the square whose forest was cleared */
    public Location getLocation() {
        return location;
    }

    /** @return the colony whose warehouse was too full, when there was one */
    public Optional<Integer> getColonyId() {
        return Optional.ofNullable(colonyId);
    }

    /** @return the name that colony had when the forest was cleared */
    public Optional<String> getColonyName() {
        return Optional.ofNullable(colonyName);
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setGoods(goods);
        out.setClearedX(location.getX());
        out.setClearedY(location.getY());
        out.setColonyId(colonyId);
        out.setColonyName(colonyName);
        return out;
    }

    public static TurnEventForestLumberLost tryLoad(final TurnEventPo po) {
        if (!TurnEventForestLumberLost.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getClearedX() != null && po.getClearedY() != null,
                "Missing cleared square");
        return new TurnEventForestLumberLost(po.getPlayerName(), po.getGoods(),
                Location.of(po.getClearedX(), po.getClearedY()),
                po.getColonyId(), po.getColonyName());
    }

}
