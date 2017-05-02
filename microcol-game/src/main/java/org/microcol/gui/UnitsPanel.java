package org.microcol.gui;

import java.util.List;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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

	public void setUnits(final List<Unit> units) {
		for (final Unit u : units) {
			Unit s = (Unit) u;
			box.getChildren().add(new Label("", new ImageView(imageProvider.getUnitImage(s.getType()))));
			final StringBuilder sb = new StringBuilder(200);
			sb.append("<html><div>");
			sb.append(localizationHelper.getUnitName(s.getType()));
			sb.append("</div><div>");
			sb.append(getText().get("unitsPanel.availableMoved"));
			sb.append(" ");
			sb.append(s.getAvailableMoves());
			sb.append("</div><div>");
			sb.append(getText().get("unitsPanel.owner"));
			sb.append(" ");
			sb.append(s.getOwner().getName());
			sb.append("</div></html>");
			box.getChildren().add(new Label(sb.toString()));
		}
		box.getChildren().add(new Label(""));
	}

	public Node getNode() {
		return box;
	}

}
