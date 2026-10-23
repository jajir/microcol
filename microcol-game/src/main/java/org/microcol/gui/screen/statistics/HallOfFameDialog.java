package org.microcol.gui.screen.statistics;

import java.util.Optional;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.HallOfFameEntry;
import org.microcol.model.campaign.po.HallOfFameDao;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * The Hall of Fame window (R-0197). From the main menu it browses the saved top
 * ten. After a scored game ends, or spring 1800 freezes the score, it shows
 * that result with its breakdown and its place or non-place in the same window.
 * Only one of these windows is open at a time, because the game waits for it.
 */
public final class HallOfFameDialog extends AbstractMessageWindow {

    public HallOfFameDialog(final ViewUtil viewUtil, final I18n i18n,
            final HallOfFameDao dao, final LocalizationHelper localization,
            final Optional<HallOfFameEntry> result) {
        super(viewUtil, i18n);
        setTitle(i18n.get(result.isPresent() ? HallOfFameText.finishedTitle
                : HallOfFameText.title));
        final HallOfFamePanel panel = new HallOfFamePanel(dao, i18n,
                localization);
        panel.showReport(result);
        final Button close = new Button(i18n.get(HallOfFameText.close));
        close.setId("hallOfFameClose");
        close.setDefaultButton(true);
        close.setCancelButton(true);
        close.setOnAction(event -> close());
        final VBox root = new VBox(16, panel, close);
        root.setId("hallOfFameDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(520);
        init(root);
    }
}
