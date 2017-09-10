package org.microcol.gui.colony;

import java.util.List;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.europe.ChooseGoodAmount;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.GoodAmount;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class ColonyDialog extends AbstractDialog implements ColonyDialogCallback {

	private final Logger logger = LoggerFactory.getLogger(ColonyDialog.class);

	private final Label colonyName;

	private final PanelColonyLayout colonyLayout;

	private final PanelColonyStructures colonyStructures;

	private final PanelColonyGoods goods;

	private final GameController gameController;

	private final PanelDock panelDock;
	
	private final PanelOutsideColony panelOutsideColony;

	private Colony colony;

	@Inject
	public ColonyDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europeDialog.caption"));

		/**
		 * Row 0
		 */
		colonyName = new Label("Colony: ");

		/**
		 * Row 1
		 */
		colonyLayout = new PanelColonyLayout(imageProvider, gameController, this);

		colonyStructures = new PanelColonyStructures(localizationHelper, imageProvider, gameController, this);

		final HBox mapAndBuildings = new HBox();
		mapAndBuildings.getChildren().addAll(colonyStructures, colonyLayout);

		/**
		 * Row 2
		 */
		final PanelProductionSummary panelProductionSummary = new PanelProductionSummary();

		panelDock = new PanelDock(imageProvider, new PanelDockBehavior() {

			@Override
			public List<Unit> getUnitsInPort() {
				return colony.getUnitsInPort();
			}

			@Override
			public void onDragDropped(final CargoSlot cargoSlot, final DragEvent event) {
				logger.debug("Object was dropped on ship cargo slot.");
				final Dragboard db = event.getDragboard();
				ClipboardReader.make(gameController.getModel(), db).filterUnit(unit -> !UnitType.isShip(unit.getType()))
						.tryReadGood((goodAmount, transferFrom) -> {
//							Preconditions.checkArgument(transferFrom.isPresent(), "Good origin is not known.");
//							GoodAmount tmp = goodAmount;
//							logger.debug("wasShiftPressed " + getPropertyShiftWasPressed().get());
//							if (getPropertyShiftWasPressed().get()) {
//								ChooseGoodAmount chooseGoodAmount = new ChooseGoodAmount(viewUtil, text,
//										goodAmount.getAmount());
//								tmp = new GoodAmount(goodAmount.getGoodType(), chooseGoodAmount.getActualValue());
//							}
//							if (transferFrom.get() instanceof ClipboardReader.TransferFromEuropeShop) {
//								try {
//									cargoSlot.buyAndStore(tmp);
//								} catch (NotEnoughtGoldException e) {
//									new DialogNotEnoughGold(viewUtil, text);
//								}
//							} else if (transferFrom.get() instanceof ClipboardReader.TransferFromCargoSlot) {
//								cargoSlot.storeFromCargoSlot(tmp,
//										((ClipboardReader.TransferFromCargoSlot) transferFrom.get()).getCargoSlot());
//							} else {
//								throw new IllegalArgumentException(
//										"Unsupported source transfer '" + transferFrom + "'");
//							}
//							repaint();
//							event.acceptTransferModes(TransferMode.MOVE);
//							event.setDropCompleted(true);
//							event.consume();
						}).tryReadUnit((unit, transferFrom) -> {
							cargoSlot.store(unit);
							repaint();
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
							.filterUnit(unit -> !UnitType.isShip(unit.getType())).isEmpty();
				}
				return false;
			}
		});

		panelOutsideColony = new PanelOutsideColony(imageProvider, gameController, this);

		final HBox managementRow = new HBox();
		managementRow.getChildren().addAll(panelProductionSummary, panelDock, panelOutsideColony);

		/**
		 * Good row - 3
		 */
		goods = new PanelColonyGoods(imageProvider);

		/**
		 * Last row 4
		 */
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(colonyName, mapAndBuildings, managementRow, goods, buttonOk);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
	}
	
	public void showColony(final Colony colony) {
		this.colony = Preconditions.checkNotNull(colony);
		colonyName.setText("Colony: " + colony.getName());
		repaint();
		getDialog().showAndWait();
	}
	
	@Override
	public void repaint(){
		colonyLayout.setColony(colony);
		goods.setEurope(gameController.getModel().getEurope());
		panelDock.repaint();
		colonyStructures.repaint(colony);
		panelOutsideColony.setColony(colony);
	}
	
}
