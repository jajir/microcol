package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Display one unit description.
 *
 */
public class UnitsPanel extends JPanel implements Localized {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final ImageProvider imageProvider;

	private final LocalizationHelper localizationHelper;

	@Inject
	public UnitsPanel(final ImageProvider imageProvider, final StatusBarMessageController statusBarMessageController,
			final LocalizationHelper localizationHelper) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.setLayout(new GridBagLayout());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController
						.fireEvent(new StatusBarMessageEvent(getText().get("unitsPanel.description")));
			}
		});
	}

	public void clear() {
		removeAll();
	}

	public void setUnits(final List<Unit> units) {
		int i = 0;
		for (final Unit u : units) {
			Unit s = (Unit) u;
			add(new JLabel(new ImageIcon(imageProvider.getUnitImage(s.getType()))), new GridBagConstraints(0, i, 1, 2,
					0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
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
			add(new JLabel(sb.toString()), new GridBagConstraints(1, i, 1, 1, 1D, 0D, GridBagConstraints.NORTHWEST,
					GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			i += 1;
		}
		add(new JLabel(""), new GridBagConstraints(1, i + 1, 2, 1, 1D, 1D, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		validate();
	}

}
