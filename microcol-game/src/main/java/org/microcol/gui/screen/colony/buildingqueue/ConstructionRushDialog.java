package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.ConstructionRushQuote;
import org.microcol.model.NotEnoughtGoldException;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Explicit confirmation of one immutable construction offer. */
final class ConstructionRushDialog extends AbstractMessageWindow {
    private final I18n i18n;
    private final Label detail = new Label();
    private final Label feedback = new Label();
    private final Button confirm = ActionButtons.button(ButtonIcon.GOLD);
    private final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
    private QueueController controller;
    private ConstructionRushQuote quote;
    private boolean purchased;

    ConstructionRushDialog(ViewUtil viewUtil, I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        detail.setId("constructionRushDetail");
        detail.setWrapText(true);
        detail.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        feedback.setId("constructionRushFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        confirm.setId("constructionRushConfirm");
        cancel.setId("constructionRushCancel");
        confirm.setDefaultButton(true);
        cancel.setCancelButton(true);
        confirm.setOnAction(event -> purchase());
        cancel.setOnAction(event -> close());
        VBox root = new VBox(16, detail, feedback,
                new HBox(12, confirm, cancel));
        root.setId("constructionRushDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(520);
        init(root);
    }

    boolean showFor(QueueController controller) {
        this.controller = controller;
        purchased = false;
        quote = controller.isCurrent()
                ? controller.getColony().getColonyBuildingQueue().getRushQuote()
                        .orElse(null)
                : null;
        setTitle(i18n.get(ConstructionText.rush));
        confirm.setText(i18n.get(ConstructionText.rushConfirm));
        cancel.setText(i18n.get(ConstructionText.rushCancel));
        boolean affordable = quote != null && controller.getColony().getOwner()
                .getGold() >= quote.getPrice();
        confirm.setDisable(!affordable);
        detail.setText(quote == null
                ? i18n.get(ConstructionText.rushUnavailable)
                : i18n.get(ConstructionText.rushDetail,
                        ConstructionText.name(i18n, quote.getItem()),
                        quote.getHammersUsed(), quote.getToolsUsed(),
                        quote.getMissingHammers(), quote.getMissingTools(),
                        quote.getPrice(), quote.getGoldAfter()));
        feedback.setText(quote != null && !affordable
                ? i18n.get(ConstructionText.rushNoGold)
                : "");
        showAndWait();
        return purchased;
    }

    private void purchase() {
        if (quote == null)
            return;
        try {
            if (!controller.isCurrent())
                throw new IllegalStateException("Colony view changed");
            controller.getColony().getColonyBuildingQueue().rush(quote);
            purchased = true;
            close();
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException | NotEnoughtGoldException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(ConstructionText.rushChanged));
        }
    }
}
