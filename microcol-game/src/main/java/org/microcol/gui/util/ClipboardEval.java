package org.microcol.gui.util;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.scene.input.Dragboard;

/**
 * Helps evaluate that clipboard fulfill some conditions.
 * <p>
 * Class require access to live model.
 * </p>
 */
public class ClipboardEval extends ClipboardParser {

    private final Model model;

    private ClipboardEval(final Model model, final String originalString) {
        super(originalString);
        this.model = Preconditions.checkNotNull(model);
    }

    public static ClipboardEval make(final Model model, final Dragboard db) {
        return new ClipboardEval(model, db.getString());
    }

    public ClipboardEval filterFrom(final Predicate<From> evalFrom) {
        if (getFrom().isPresent()) {
            if (!evalFrom.test(getFrom().get())) {
                removeKey(KEY_FROM);
            }
        }
        return this;
    }

    /**
     * Filter transfered unit.
     *
     * @param filter
     *            required predicated
     * @return return parsing result
     */
    public ClipboardEval filterUnit(final Predicate<Unit> filter) {
        if (getFrom().isPresent() && getUnit(model).isPresent()) {
            if (!filter.test(getUnit(model).get())) {
                removeKey(KEY_UNIT_ID);
            }
        }
        return this;
    }

    /**
     * Filter transfered goods.
     *
     * @param filter
     *            required predicated, if it's <code>true</code> good will be
     *            keep otherwise will be removed.
     * @return return parsing result
     */
    public ClipboardEval filterGoods(final Predicate<Goods> filter) {
        if (getFrom().isPresent() && getGoods().isPresent()) {
            if (!filter.test(getGoods().get())) {
                removeKey(KEY_GOODS);
            }
        }
        return this;
    }

    public ClipboardEval tryReadUnit(final BiConsumer<Unit, From> consumer) {
        if (getFrom().isPresent() && getUnit(model).isPresent()) {
            consumer.accept(getUnit(model).get(), getFrom().get());
        }
        return this;
    }

    public Optional<CargoSlot> getCargoSlot() {
        if (getFrom().isPresent()) {
            final Optional<Unit> oFromUnit = getUnit(model, KEY_SOURCE_UNIT_ID);
            if (oFromUnit.isPresent()) {
                final UnitWithCargo unitWithCargo = (UnitWithCargo) oFromUnit.get();
                final int slotIndex = getInt(KEY_CARGO_SLOT_INDEX);
                return Optional.of(unitWithCargo.getCargo().getSlotByIndex(slotIndex));
            }
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return (get(KEY_GOODS) == null && get(KEY_UNIT_ID) == null) || !getFrom().isPresent();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public Optional<Unit> getUnit() {
        return getUnit(model);
    }

}
