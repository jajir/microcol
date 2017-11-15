package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class ColonyDialog extends AbstractMessageWindow implements ColonyDialogCallback {

	final Logger logger = LoggerFactory.getLogger(ColonyDialog.class);

	private final Label colonyName;

	private final PanelColonyFields colonyFields;

	private final PanelColonyStructures colonyStructures;

	private final PanelColonyGoods goods;

	private final PanelDock panelDock;

	private final PanelOutsideColony panelOutsideColony;

	private final BooleanProperty propertyShiftWasPressed;

	Colony colony;

	@Inject
	public ColonyDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final PanelColonyFields panelColonyFields, final PanelColonyStructures panelColonyStructures,
			final PanelOutsideColony panelOutsideColony, final PanelColonyGoods panelColonyGoods,
			final PanelColonyDockBehaviour panelColonyDockBehaviour) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		getDialog().setTitle(text.get("europeDialog.caption"));

		/**
		 * Row 0
		 */
		colonyName = new Label("Colony: ");

		/**
		 * Row 1
		 */
		colonyFields = Preconditions.checkNotNull(panelColonyFields);
		colonyStructures = Preconditions.checkNotNull(panelColonyStructures);

		final HBox mapAndBuildings = new HBox();
		mapAndBuildings.getChildren().addAll(colonyStructures, colonyFields);

		/**
		 * Row 2
		 */
		final PanelProductionSummary panelProductionSummary = new PanelProductionSummary();

		panelDock = new PanelDock(imageProvider, Preconditions.checkNotNull(panelColonyDockBehaviour));

		this.panelOutsideColony = Preconditions.checkNotNull(panelOutsideColony);

		final HBox managementRow = new HBox();
		managementRow.getChildren().addAll(panelProductionSummary, panelDock, panelOutsideColony);

		/**
		 * Good row - 3
		 */
		goods = Preconditions.checkNotNull(panelColonyGoods);

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

		/**
		 * TODO there is a bug, keyboard events are not send during dragging.
		 * TODO copy of this code is in EuropeDialog
		 */
		propertyShiftWasPressed = new SimpleBooleanProperty(false);
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

	public void showColony(final Colony colony) {
		this.colony = Preconditions.checkNotNull(colony);
		colonyName.setText("Colony: " + colony.getName());
		goods.setColony(colony);
		repaint();
		getDialog().showAndWait();
	}

	@Override
	public void repaint() {
		colonyFields.setColony(colony);
		goods.repaint();
		panelDock.repaint();
		colonyStructures.repaint(colony);
		panelOutsideColony.setColony(colony);
	}

	@Override
	public void close() {
		getDialog().close();
	}

	public BooleanProperty getPropertyShiftWasPressed() {
		return propertyShiftWasPressed;
	}
}
