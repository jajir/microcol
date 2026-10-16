package org.microcol.gui.screen.congress;

import com.google.inject.Inject;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.stream.Collectors;

/** Read-only Congress overview until an explicit candidate confirmation. */
public final class CongressDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final I18n i18n;
    private final Label progress = label("congressProgress");
    private final Label objective = label("congressObjective");
    private final Label members = label("congressMembers");
    private final Label reward = label("congressReward");
    private final Label feedback = label("congressFeedback");
    private final Label detail = label("congressDetail");
    private final ListView<FounderOffer> candidates = new ListView<>();
    private final Button choose = new Button();
    private final Button close = new Button();
    private Model boundModel;
    private Player player;

    @Inject
    public CongressDialog(ViewUtil viewUtil, I18n i18n,
            GameModelController game) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.game = game;
        candidates.setId("congressCandidates");
        candidates.setPrefHeight(190);
        candidates.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(FounderOffer offer, boolean empty) {
                super.updateItem(offer, empty);
                setText(empty || offer == null ? null
                        : CongressText.name(i18n, offer.getFounder()) + " — "
                                + i18n.get(CongressText.valueOf(offer
                                        .getFounder().getCategory().name())));
                setId(empty || offer == null ? null
                        : "founder-" + offer.getFounder().name());
            }
        });
        candidates.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> {
                    choose.setDisable(value == null || !isCurrent()
                            || !player.canChooseFounder(value));
                    detail.setText(value != null
                            && value.getFounder() == Founder.JOHN_PAUL_JONES
                                    ? i18n.get(CongressText.jonesEffect)
                                    : "");
                });
        choose.setId("congressChoose");
        close.setId("congressClose");
        choose.setOnAction(event -> choose());
        close.setOnAction(event -> close());
        close.setCancelButton(true);
        VBox root = new VBox(12, progress, objective, candidates, detail,
                choose, members, reward, feedback, close);
        root.setId("congressDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(580);
        init(root);
    }

    private static Label label(String id) {
        Label label = new Label();
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    public void showCongress() {
        if (!game.isGameModelReady())
            return;
        boundModel = game.getModel();
        player = game.getHumanPlayer();
        setTitle(i18n.get(CongressText.title));
        choose.setText(i18n.get(CongressText.choose));
        close.setText(i18n.get(CongressText.close));
        feedback.setText("");
        refresh();
        showAndWait();
    }

    private boolean isCurrent() {
        return player != null && game.isGameModelReady()
                && game.getModel() == boundModel
                && game.getHumanPlayer() == player && boundModel.isGameRunning()
                && boundModel.getCurrentPlayer() == player;
    }

    private void refresh() {
        progress.setText(i18n.get(CongressText.progress,
                player.getCongressBells(), player.getCongressTarget()));
        objective.setText(player.getSelectedFounder()
                .map(founder -> i18n.get(CongressText.objective,
                        CongressText.name(i18n, founder)))
                .orElseGet(() -> i18n.get(player.getCongressMembers()
                        .size() == Founder.values().length
                                ? CongressText.allMembers
                                : CongressText.chooseObjective)));
        candidates.getItems().setAll(player.getFounderOffers());
        choose.setDisable(true);
        members.setText(i18n.get(CongressText.members,
                player.getCongressMembers().isEmpty()
                        ? i18n.get(CongressText.none)
                        : player.getCongressMembers().stream().map(
                                founder -> CongressText.name(i18n, founder))
                                .collect(Collectors.joining(", "))));
        reward.setText(player.isJonesDeliveryPending()
                ? i18n.get(CongressText.jonesPending)
                : player.getCongressMembers().contains(Founder.JOHN_PAUL_JONES)
                        ? i18n.get(CongressText.jonesDelivered)
                        : "");
        if (player.getFounderOffers().isEmpty()
                && player.getSelectedFounder().isEmpty()
                && player.getCongressMembers().isEmpty())
            feedback.setText(i18n.get(CongressText.firstColony));
    }

    private void choose() {
        FounderOffer offer = candidates.getSelectionModel().getSelectedItem();
        if (offer == null)
            return;
        try {
            if (!isCurrent())
                throw new IllegalStateException("Congress view changed");
            player.chooseFounder(offer);
            refresh();
            feedback.setText(i18n.get(CongressText.chosen));
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            choose.setDisable(true);
            feedback.setText(i18n.get(CongressText.changed));
        }
    }
}
