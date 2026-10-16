package org.microcol.gui.screen.europe;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.RecruitmentOffer;
import org.microcol.model.NotEnoughtGoldException;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Paid passage uses saved candidates independently of university hiring. */
public final class RecruiteUnitsDialog extends AbstractMessageWindow {

    private final Label caption = new Label();
    private final HBox candidates = new HBox(16);
    private final ImageProvider images;
    private final GameModelController controller;
    private final LocalizationHelper names;
    private final DialogNotEnoughGold insufficientGold;
    private final DialogMessage rejection;
    private final I18n i18n;
    private final EuropeCallback europe;
    private Player buyer;
    private Model openedModel;
    private boolean actionTaken;

    @Inject
    public RecruiteUnitsDialog(final ViewUtil viewUtil,
            final ImageProvider images, final GameModelController controller,
            final LocalizationHelper names,
            final DialogNotEnoughGold insufficientGold, final I18n i18n,
            final DialogMessage rejection, final EuropeCallback europe) {
        super(viewUtil, i18n);
        this.images = images;
        this.controller = controller;
        this.names = names;
        this.insufficientGold = insufficientGold;
        this.i18n = i18n;
        this.rejection = rejection;
        this.europe = europe;
        final VBox root = new VBox(12);
        root.setId("recruitment-dialog");
        candidates.setPadding(new Insets(10));
        final ButtonBarOk buttons = new ButtonBarOk(i18n);
        buttons.getButtonOk().setOnAction(event -> close());
        root.getChildren().addAll(caption, candidates, buttons.getContent());
        init(root);
    }

    @Override
    public void showAndWait() {
        buyer = controller.getHumanPlayer();
        openedModel = controller.getModel();
        actionTaken = false;
        try {
            refresh();
        } catch (IllegalStateException e) {
            showRejection(Europe.recruitUnavailable);
            buyer = null;
            openedModel = null;
            return;
        }
        try {
            super.showAndWait();
        } finally {
            buyer = null;
            openedModel = null;
        }
    }

    private void refresh() {
        candidates.getChildren().clear();
        buyer.getRecruitmentOffers()
                .forEach(offer -> candidates.getChildren()
                        .add(new RecruiteUnitPanel(offer, buyer, images, names,
                                i18n, this)));
    }

    void recruit(final RecruitmentOffer offer) {
        if (buyer == null || actionTaken) {
            return;
        }
        actionTaken = true;
        try {
            if (controller.getModel() != openedModel) {
                throw new IllegalStateException(
                        "Recruitment belongs to a replaced game");
            }
            buyer.recruit(offer);
        } catch (NotEnoughtGoldException e) {
            insufficientGold.showAndWait();
            actionTaken = false;
            refresh();
            return;
        } catch (IllegalStateException e) {
            showRejection(Europe.recruitChanged);
            close();
            return;
        }
        europe.repaint();
        close();
    }

    private void showRejection(final Europe text) {
        rejection.setTitle(i18n.get(Europe.recruitUnitDialogTitle));
        rejection.setText(i18n.get(text));
        rejection.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        setTitle(i18n.get(Europe.recruitUnitDialogTitle));
        caption.setText(i18n.get(Europe.recruitUnitDialogTitle));
    }
}
