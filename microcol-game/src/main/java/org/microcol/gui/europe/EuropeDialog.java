package org.microcol.gui.europe;

import java.util.List;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodAmount;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class EuropeDialog extends AbstractMessageWindow implements DialogCallback {

	private final Logger logger = LoggerFactory.getLogger(EuropeDialog.class);

	private final PanelDock europeDock;

	private final PanelHighSeas shipsTravelingToNewWorld;

	private final PanelHighSeas shipsTravelingToEurope;

	private final PanelPortPier panelPortPier;

	private final PanelGoods panelGoods;

	private final BooleanProperty propertyShiftWasPressed;

	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameModelController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		propertyShiftWasPressed = new SimpleBooleanProperty(false);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europe.title"));

		final Label label = new Label(text.get("europe.title"));
		shipsTravelingToNewWorld = new PanelHighSeas(this, imageProvider, text.get("europe.shipsTravelingToNewWorld"),
				gameController, false);
		shipsTravelingToEurope = new PanelHighSeas(this, imageProvider, text.get("europe.shipsTravelingToEurope"),
				gameController, true);
		europeDock = new PanelDock(imageProvider, new PanelDockBehavior() {

			@Override
			public List<Unit> getUnitsInPort() {
				return gameController.getModel().getEurope().getPort()
						.getShipsInPort(gameController.getCurrentPlayer());
			}
			
			@Override
			public final void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
				logger.debug("Object was dropped on ship cargo slot.");
				final Dragboard db = event.getDragboard();
				ClipboardReader.make(gameController.getModel(), db).filterUnit(unit -> !unit.getType().isShip())
						.tryReadGood((goodAmount, transferFrom) -> {
							Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");
							GoodAmount tmp = goodAmount;
							logger.debug("wasShiftPressed " + getPropertyShiftWasPressed().get());
							if (getPropertyShiftWasPressed().get()) {
								ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text,
										goodAmount.getAmount());
								tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
							}
							if (transferFrom.get() instanceof ClipboardReader.TransferFromEuropeShop) {
								try {
									cargoSlot.buyAndStore(tmp);
								} catch (NotEnoughtGoldException e) {
									new DialogNotEnoughGold(viewUtil, text);
								}
							} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
								cargoSlot.storeFromCargoSlot(tmp,
										((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
							} else {
								throw new IllegalArgumentException(
										"Unsupported source transfer '" + transferFrom + "'");
							}
							repaintAfterGoodMoving();
							event.acceptTransferModes(TransferMode.MOVE);
							event.setDropCompleted(true);
							event.consume();
						}).tryReadUnit((unit, transferFrom) -> {
							cargoSlot.store(unit);
							repaintAfterGoodMoving();
							event.acceptTransferModes(TransferMode.MOVE);
							event.setDropCompleted(true);
							event.consume();
						});
			}

			@Override
			public void onDragDetected(final CargoSlot cargoSlot, final MouseEvent event, final Node node) {
				if (cargoSlot != null) {
					if (cargoSlot.getUnit().isPresent()) {
						final Image cargoImage = imageProvider.getUnitImage(cargoSlot.getUnit().get().getType());
						ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage)
								.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
								.addUnit(cargoSlot.getUnit().get()).build();
					} else if (cargoSlot.getGoods().isPresent()) {
						final Image cargoImage = imageProvider
								.getGoodTypeImage(cargoSlot.getGoods().get().getGoodType());
						ClipboardWritter.make(node.startDragAndDrop(TransferMode.MOVE)).addImage(cargoImage)
								.addTransferFromUnit(cargoSlot.getOwnerUnit(), cargoSlot)
								.addGoodAmount(cargoSlot.getGoods().get()).build();
					}
				}
				event.consume();
			}

			@Override
			public boolean isCorrectObject(final CargoSlot cargoSlot, final Dragboard db) {
				logger.debug("Drag over unit id '" + db.getString() + "'.");
				if (cargoSlot != null && cargoSlot.isEmpty()) {
					return !ClipboardReader.make(gameController.getModel(), db)
							.filterUnit(unit -> !unit.getType().isShip()).isEmpty();
				}
				return false;
			}

		});
		final VBox panelShips = new VBox();
		panelShips.getChildren().addAll(shipsTravelingToNewWorld, shipsTravelingToEurope, europeDock);

		panelPortPier = new PanelPortPier(gameController, this, text.get("europe.pier"), imageProvider, localizationHelper);

		final Button recruiteButton = new Button(text.get("europe.recruit"));
		recruiteButton.setOnAction(
				event -> new RecruiteUnitsDialog(viewUtil, text, imageProvider, gameController, localizationHelper));
		final Button buyButton = new Button(text.get("europe.buy"));
		buyButton.setOnAction(
				event -> new BuyUnitsDialog(viewUtil, text, imageProvider, gameController, localizationHelper, this));
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		final VBox panelButtons = new VBox();
		panelButtons.getChildren().addAll(recruiteButton, buyButton, buttonOk);

		final HBox panelMiddle = new HBox();
		panelMiddle.getChildren().addAll(panelShips, panelPortPier, panelButtons);

		panelGoods = new PanelGoods(this, gameController, imageProvider);

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(label, panelMiddle, panelGoods);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
		/**
		 * TODO there is a bug, keyboard events are not send during dragging.
		 * TODO copy of this code is n colonyDialog
		 */
		getScene().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
			if (event.getCode() == KeyCode.SHIFT) {
				propertyShiftWasPressed.set(false);
			}
		});
		getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			logger.debug("wasShiftPressed " + event);
			if (event.getCode() == KeyCode.SHIFT) {
				propertyShiftWasPressed.set(true);
			}
		});
	}

	public void show() {
		repaint();
		getDialog().showAndWait();
	}

	public void repaint() {
		europeDock.repaint();
		shipsTravelingToEurope.repaint();
		shipsTravelingToNewWorld.repaint();
		panelPortPier.repaint();
		panelGoods.repaint();
	}

	@Override
	public void repaintAfterGoodMoving() {
		europeDock.repaintCurrectShipsCrates();
		panelPortPier.repaint();
	}

	@Override
	public BooleanProperty getPropertyShiftWasPressed() {
		return propertyShiftWasPressed;
	}

}
