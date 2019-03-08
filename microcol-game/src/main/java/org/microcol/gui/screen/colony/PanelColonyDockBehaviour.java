package org.microcol.gui.screen.colony;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodsDialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Define behavior of ship cargo slot.
 */
public final class PanelColonyDockBehaviour extends AbstractPanelDockBehavior {

    final Logger logger = LoggerFactory.getLogger(PanelColonyDockBehaviour.class);

    private final ColonyDialogCallback colonyDialogCallback;
    private final ChooseGoodsDialog chooseGoods;

    @Inject
    PanelColonyDockBehaviour(final ColonyDialogCallback colonyDialogCallback,
            final GameModelController gameModelController, final ImageProvider imageProvider,
            final ChooseGoodsDialog chooseGoods) {
        super(gameModelController, imageProvider);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        this.chooseGoods = Preconditions.checkNotNull(chooseGoods);
    }

    @Override
    public List<Unit> getUnitsInPort() {
        return colonyDialogCallback.getColony().getUnitsInPort();
    }

    @Override
    public void consumeGoods(final CargoSlot targetCargoSlot,
            final boolean specialOperatonWasSelected, final ClipboardEval eval) {

        final Goods transferedGoods = eval.getGoods().get();
        final From transferFrom = eval.getFrom().get();

        Goods tmp = transferedGoods;

        logger.debug("wasShiftPressed " + colonyDialogCallback.getPropertyShiftWasPressed().get());
        // FIXME this is horrible code.
        if (specialOperatonWasSelected) {
            // synchronously get information about transfered amount
            chooseGoods
                    .init(targetCargoSlot.maxPossibleGoodsToMoveHere(transferedGoods));
            tmp = chooseGoods.getActualValue();
            if (tmp.isZero()) {
                return;
            }
        }

        if (From.VALUE_FROM_COLONY_WAREHOUSE == transferFrom) {
            targetCargoSlot.storeFromColonyWarehouse(tmp, colonyDialogCallback.getColony());
        } else if (From.VALUE_FROM_UNIT == transferFrom) {
            targetCargoSlot.storeFromCargoSlot(tmp, eval.getCargoSlot().get());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source transfer '" + transferFrom + "'");
        }

        colonyDialogCallback.repaint();
    }

    @Override
    public void consumeUnit(final CargoSlot targetCargoSlot, final Unit unit,
            final From transferFrom) {
        targetCargoSlot.store(unit);
        colonyDialogCallback.repaint();
    }

}