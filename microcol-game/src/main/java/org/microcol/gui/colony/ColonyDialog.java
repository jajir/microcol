package org.microcol.gui.colony;

import java.util.List;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockBehavior;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class ColonyDialog extends AbstractDialog implements ColonyDialogCallback {

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
			public void onDragDropped(CargoSlot cargoSlot, DragEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDragDetected(CargoSlot cargoSlot, MouseEvent event, Node node) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isCorrectObject(CargoSlot cargoSlot, Dragboard db) {
				// TODO Auto-generated method stub
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
