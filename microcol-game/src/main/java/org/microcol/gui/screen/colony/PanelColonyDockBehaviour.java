package org.microcol.gui.screen.colony;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.dock.AbstractPanelDockBehavior;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Define behavior of ship cargo slot.
 */
public final class PanelColonyDockBehaviour extends AbstractPanelDockBehavior {

    private final EventBus eventBus;
    private final ColonyDialogCallback colonyDialogCallback;
    private final ChooseGoodsDialog chooseGoods;

    @Inject
    PanelColonyDockBehaviour(final EventBus eventBus,
            final ColonyDialogCallback colonyDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final ChooseGoodsDialog chooseGoods) {
        super(gameModelController, imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<UnitWithCargo> getUnitsInPort() {
        return (List) colonyDialogCallback.getColony().getUnitsInPort();
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected, final ClipboardEval eval) {
        Goods transferedGoods = eval.getGoods().get();
        transferedGoods = chooseGoods(transferedGoods, specialOperatonWasSelected, targetCargoSlot);
        if (transferedGoods.isZero()) {
            return;
        }
        final From transferFrom = eval.getFrom().get();
        if (From.VALUE_FROM_COLONY_WAREHOUSE == transferFrom) {
            targetCargoSlot.storeFromColonyWarehouse(transferedGoods,
                    colonyDialogCallback.getColony());
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            targetCargoSlot.storeFromCargoSlot(transferedGoods, eval.getCargoSlot().get());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }
        eventBus.post(new RepaintColonyEvent());
    }

    private Goods chooseGoods(final Goods goods, final boolean specialOperatonWasSelected,
            final CargoSlot targetCargoSlot) {
        return chooseGoods(chooseGoods, specialOperatonWasSelected,
                targetCargoSlot.maxPossibleGoodsToMoveHere(goods));
    }

    @Override
    public void consumeUnit(final CargoSlot targetCargoSlot, final Unit unit,
            final From transferFrom) {
        targetCargoSlot.store(unit);
        eventBus.post(new RepaintColonyEvent());
    }

}