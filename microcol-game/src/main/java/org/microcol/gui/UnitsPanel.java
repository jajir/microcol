package org.microcol.gui;

import java.util.List;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Display one unit description.
 *
 */
public class UnitsPanel implements Localized {

	private final ImageProvider imageProvider;

	private final LocalizationHelper localizationHelper;

	private final VBox box;

	@Inject
	public UnitsPanel(final ImageProvider imageProvider, final StatusBarMessageController statusBarMessageController,
			final LocalizationHelper localizationHelper) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		box = new VBox();
		box.setOnMouseEntered(e -> {
			statusBarMessageController.fireEvent(new StatusBarMessageEvent(getText().get("unitsPanel.description")));
		});
	}

	public void clear() {
		box.getChildren().clear();
	}

	public void setUnits(final Player humanPlayer, final List<Unit> units) {
		for (final Unit unit : units) {
			box.getChildren().add(makeUnitPanel(humanPlayer, unit));
		}
		box.getChildren().add(new Label(""));
	}

	private Node makeUnitPanel(final Player humanPlayer, final Unit unit) {
		VBox box = new VBox();
		box.getChildren().add(makeUnitImage(humanPlayer, unit));
		if (isUnitOwnedBy(unit, humanPlayer) && unit.getType().getCargoCapacity() > 0) {
			box.getChildren().add(makeGoodsPanel(unit));
		}
		return box;
	}

	private HBox makeUnitImage(final Player humanPlayer, final Unit unit) {
		HBox box = new HBox();
		box.getChildren().add(new ImageView(imageProvider.getUnitImage(unit.getType())));
		box.getChildren().add(makeUnitDescription(humanPlayer, unit));
		return box;
	}

	private Region makeUnitDescription(final Player humanPlayer, final Unit unit) {
		VBox box = new VBox();
		final StringBuilder sb = new StringBuilder(200);
		sb.append(localizationHelper.getUnitName(unit.getType()));
		sb.append("\n");
		if (isUnitOwnedBy(unit, humanPlayer)) {
			sb.append(getText().get("unitsPanel.availableMoves"));
			sb.append(" ");
			sb.append(unit.getAvailableMoves());
			sb.append("\n");
		}
		sb.append(getText().get("unitsPanel.owner"));
		sb.append(" ");
		sb.append(unit.getOwner().getName());
		box.getChildren().add(new Label(sb.toString()));
		return box;
	}

	private boolean isUnitOwnedBy(final Unit unit, final Player player) {
		return unit.getOwner().equals(player);
	}

	private HBox makeGoodsPanel(final Unit unit) {
		HBox box = new HBox();
		// TODO JJ localize it
		box.getChildren().add(new Label("With:"));
		unit.getHold().getSlots().stream().filter(cargoSlot -> !cargoSlot.isEmpty()).forEach(cargoSlot -> {
			box.getChildren().add(new ImageView(imageProvider.getUnitImage(cargoSlot.getUnit().get().getType())));
		});
		return box;
	}

	public Node getNode() {
		return box;
	}

}
