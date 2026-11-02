package org.akropia.gui.screen.colony;

import java.util.List;

import org.akropia.gui.dialog.ChooseGoodsDialog;
import org.akropia.gui.dock.AbstractPanelDockBehavior;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.ClipboardEval;
import org.akropia.gui.util.From;
import org.akropia.model.CargoSlot;
import org.akropia.model.Goods;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitWithCargo;

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
            final GameModelController gameModelController,
            final ImageProvider imageProvider,
            final ChooseGoodsDialog chooseGoods) {
        super(gameModelController, imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.colonyDialogCallback = Preconditions
                .checkNotNull(colonyDialogCallback);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
    }

    @Override
    public List<UnitWithCargo> getUnitsInPort() {
        return colonyDialogCallback.getColony().getUnitsInPort();
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected,
            final ClipboardEval eval) {
        Goods transferedGoods = eval.getGoods().get();
        transferedGoods = chooseGoods(transferedGoods,
                specialOperatonWasSelected, targetCargoSlot);
        if (transferedGoods.isZero()) {
            return;
        }
        final From transferFrom = eval.getFrom().get();
        if (From.VALUE_FROM_COLONY_WAREHOUSE == transferFrom) {
            targetCargoSlot.storeFromColonyWarehouse(transferedGoods,
                    colonyDialogCallback.getColony());
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            targetCargoSlot.storeFromCargoSlot(transferedGoods,
                    eval.getCargoSlot().get());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }
        eventBus.post(new RepaintColonyEvent());
    }

    private Goods chooseGoods(final Goods goods,
            final boolean specialOperatonWasSelected,
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