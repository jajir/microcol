package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;

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
public class EuropeDialog extends AbstractDialog {

	private final PanelEuropeDock europeDock;

	private final PanelHighSeas shipsTravelingToNewWorld;

	private final PanelHighSeas shipsTravelingToEurope;

	private final PanelPortPier panelPortPier;

	private final PanelGoods panelGoods;
	
	private final BooleanProperty propertyShiftWasPressed;

	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		propertyShiftWasPressed = new SimpleBooleanProperty(false);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europe.title"));

		final Label label = new Label("European port");

		shipsTravelingToNewWorld = new PanelHighSeas(this, imageProvider, "Ships travelling to New World",
				gameController, false);
		shipsTravelingToEurope = new PanelHighSeas(this, imageProvider, "Ships travelling to Europe", gameController,
				true);
		europeDock = new PanelEuropeDock(viewUtil, text, gameController, imageProvider, this);
		final VBox panelShips = new VBox();
		panelShips.getChildren().addAll(shipsTravelingToNewWorld, shipsTravelingToEurope, europeDock);

		panelPortPier = new PanelPortPier(gameController, this, "Pier", imageProvider, localizationHelper);

		final Button recruiteButton = new Button("Recruite");
		final Button buyButton = new Button("Buy");
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		final VBox panelButtons = new VBox();
		panelButtons.getChildren().addAll(recruiteButton, buyButton, buttonOk);

		final HBox panelMiddle = new HBox();
		panelMiddle.getChildren().addAll(panelShips, panelPortPier, panelButtons);

		panelGoods = new PanelGoods(gameController, imageProvider);

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(label, panelMiddle, panelGoods);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
		/**
		 * TODO there is a bug, keyboard events are not send during dragging.
		 */
		getScene().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
			if (event.getCode() == KeyCode.SHIFT) {
				propertyShiftWasPressed.set(false);
			}
		});
		getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			System.out.println("wasShiftPressed " + event);
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

	public void repaintAfterGoodMoving() {
		europeDock.repaintCurrectShipsCrates();
		panelPortPier.repaint();
	}

	BooleanProperty getPropertyShiftWasPressed() {
		return propertyShiftWasPressed;
	}

}
