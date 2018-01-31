package org.microcol.gui.colony;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PanelColonyDockBehaviour extends AbstractPanelDockBehavior {

	final Logger logger = LoggerFactory.getLogger(PanelColonyDockBehaviour.class);

	private final ColonyDialogCallback colonyDialogCallback;
	private final ViewUtil viewUtil;
	private final Text text;

	@Inject
	PanelColonyDockBehaviour(final ColonyDialogCallback colonyDialogCallback,
			final GameModelController gameModelController, final ViewUtil viewUtil, final Text text,
			final ImageProvider imageProvider) {
		super(gameModelController, imageProvider);
		this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
	}

	@Override
	public List<Unit> getUnitsInPort() {
		return colonyDialogCallback.getColony().getUnitsInPort();
	}

	@Override
	public void consumeGoods(final CargoSlot cargoSlot, final GoodAmount goodAmount,
			final Optional<TransferFrom> transferFrom) {

		GoodAmount tmp = goodAmount;
		logger.debug("wasShiftPressed " + colonyDialogCallback.getPropertyShiftWasPressed().get());
		if (colonyDialogCallback.getPropertyShiftWasPressed().get()) {
			// synchronously get information about transfered amount
			ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text, goodAmount.getAmount());
			tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
		}
		// TODO following code doesn't look readable
		if (transferFrom.get() instanceof ClipboardReader.TransferFromColonyWarehouse) {
			cargoSlot.storeFromColonyWarehouse(tmp, colonyDialogCallback.getColony());
		} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
			cargoSlot.storeFromCargoSlot(tmp,
					((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
		} else {
			throw new IllegalArgumentException("Unsupported source transfer '" + transferFrom + "'");
		}

		colonyDialogCallback.repaint();
	}

	@Override
	public void consumeUnit(final Unit unit, final Optional<TransferFrom> transferFrom) {
		colonyDialogCallback.repaint();
	}

}