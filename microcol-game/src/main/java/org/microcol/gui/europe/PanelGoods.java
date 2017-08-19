package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.model.Europe;
import org.microcol.model.GoodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Show list of all available goods.
 */
public class PanelGoods extends TitledPanel {

	private final Logger logger = LoggerFactory.getLogger(PanelGoods.class);

	private final HBox hBox;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	private Background background;

	public PanelGoods(final GameController gameController, final ImageProvider imageProvider) {
		super("zbozi");
		this.gameController = Preconditions.checkNotNull(gameController);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		hBox = new HBox();
		getContentPane().getChildren().add(hBox);

		setOnDragEntered(this::onDragEntered);
		setOnDragExited(this::onDragExited);
		setOnDragOver(this::onDragOver);
		setOnDragDropped(this::onDragDropped);
	}

	public void repaint() {
		final Europe europe = gameController.getModel().getEurope();
		hBox.getChildren().clear();
		GoodType.getGoodTypes().forEach(goodType -> {
			hBox.getChildren()
					.add(new PanelGood(imageProvider.getGoodTypeImage(goodType), europe.getGoodTradeForType(goodType)));
		});
	}

	private final void onDragEntered(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			background = getBackground();
			setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		}
	}

	@SuppressWarnings("unused")
	private final void onDragExited(final DragEvent event) {
		setBackground(background);
		background = null;
	}

	private final void onDragOver(final DragEvent event) {
		if (isItGoodAmount(event.getDragboard())) {
			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	private final void onDragDropped(DragEvent event) {
		logger.debug("Object was dropped on panel goods.");
		final Dragboard db = event.getDragboard();
		ClipboardReader.make(gameController.getModel(), db).tryReadGood((goodAmount, transferFrom) -> {
			// FIXME remove goodAmount from cargoStore
			event.acceptTransferModes(TransferMode.MOVE);
			event.setDropCompleted(true);
			event.consume();
		});
	}

	private boolean isItGoodAmount(final Dragboard db) {
		logger.debug("Drag over unit id '" + db.getString() + "'.");
		return ClipboardReader.make(gameController.getModel(), db).getGoods().isPresent();
	}

}
