package org.microcol.gui.europe;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractPanelDockBehavior;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFrom;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PanelEuropeDockBehavior extends AbstractPanelDockBehavior {

	final Logger logger = LoggerFactory.getLogger(PanelEuropeDockBehavior.class);

	private final EuropeDialogCallback europeDialogCallback;
	private final GameModelController gameModelController;
	private final Text text;
	private final ViewUtil viewUtil;

	@Inject
	PanelEuropeDockBehavior(EuropeDialogCallback europeDialogCallback, GameModelController gameModelController,
			Text text, ViewUtil viewUtil, ImageProvider imageProvider) {
		super(gameModelController, imageProvider);
		this.europeDialogCallback = Preconditions.checkNotNull(europeDialogCallback);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.text = Preconditions.checkNotNull(text);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
	}

	@Override
	public List<Unit> getUnitsInPort() {
		return gameModelController.getModel().getEurope().getPort()
				.getShipsInPort(gameModelController.getCurrentPlayer());
	}

	@Override
	public void consumeGoods(final CargoSlot cargoSlot, final GoodAmount goodAmount,
			final Optional<TransferFrom> transferFrom) {
		GoodAmount tmp = goodAmount;
		logger.debug("wasShiftPressed " + europeDialogCallback.getPropertyShiftWasPressed().get());
		if (europeDialogCallback.getPropertyShiftWasPressed().get()) {
			ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text, cargoSlot.maxPossibleGoodsToMoveHere(10000, goodAmount.getAmount()));
			tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
			if (tmp.isZero()) {
				return;
			}
		}
		if (transferFrom.get() instanceof ClipboardReader.TransferFromEuropeShop) {
			try {
				cargoSlot.storeFromEuropePort(tmp);
			} catch (NotEnoughtGoldException e) {
				new DialogNotEnoughGold(viewUtil, text);
			}
		} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
			cargoSlot.storeFromCargoSlot(tmp,
					((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
		} else {
			throw new IllegalArgumentException("Unsupported source transfer '" + transferFrom + "'");
		}
		europeDialogCallback.repaintAfterGoodMoving();
	}

	@Override
	public void consumeUnit(final Unit unit, final Optional<TransferFrom> transferFrom) {
		europeDialogCallback.repaintAfterGoodMoving();
	}

}