package org.microcol.gui.screen.colony;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Listener
@Singleton
public final class DialogColonyJobChoices extends AbstractMessageWindow {
    private final I18n i18n;
    private final EventBus eventBus;
    private final GameModelController controller;
    private final ColonyDialogCallback callback;
    private final Label caption = new Label();
    private final ListView<ColonyJobChoice> choices = new ListView<>();
    private final Label details = new Label();
    private final Label message = new Label();
    private final Button confirm = new Button();
    private final Button cancel = new Button();
    private Colony colony;
    private Model model;
    private Unit worker;

    @Inject
    public DialogColonyJobChoices(final ViewUtil viewUtil, final I18n i18n,
            final EventBus eventBus, final GameModelController controller,
            final ColonyDialogCallback callback) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.controller = controller;
        this.callback = callback;
        final VBox root = new VBox(10, caption, choices, details, message,
                new HBox(10, confirm, cancel));
        root.setPadding(new Insets(16));
        root.setPrefWidth(640);
        root.setId("jobDialog");
        choices.setId("jobChoices");
        details.setId("jobDetails");
        message.setId("jobMessage");
        confirm.setId("jobConfirm");
        cancel.setId("jobCancel");
        caption.setWrapText(true);
        details.setWrapText(true);
        message.setWrapText(true);
        choices.setPrefHeight(240);
        details.setMinHeight(180);
        message.setMinHeight(48);
        choices.setCellFactory(list -> new ListCell<ColonyJobChoice>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.prefWidthProperty()
                        .bind(choices.widthProperty().subtract(35));
            }

            @Override
            protected void updateItem(final ColonyJobChoice choice,
                    final boolean empty) {
                super.updateItem(choice, empty);
                setText(null);
                setGraphic(empty || choice == null ? null : label);
                setAccessibleText(null);
                if (!empty && choice != null) {
                    label.setText(
                            title(choice) + choice.getUnavailableReason()
                                    .map(reason -> "\n" + i18n.get(
                                            ColonyMsg.jobUnavailable,
                                            i18n.get(ColonyMsg.valueOf(
                                                    "job" + reason.name()))))
                                    .orElse(""));
                    setAccessibleText(label.getText());
                }
            }
        });
        choices.getSelectionModel().selectedItemProperty()
                .addListener((observable, old, choice) -> {
                    details.setText(choice == null ? "" : describe(choice));
                    confirm.setDisable(choice == null
                            || choice.getUnavailableReason().isPresent());
                });
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> confirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        init(root);
        getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        close();
                        event.consume();
                    } else if (event
                            .getCode() == javafx.scene.input.KeyCode.ENTER
                            && choices.isFocused()) {
                        confirm.fire();
                        event.consume();
                    }
                });
    }

    @Subscribe
    private void on(final ShowColonyJobChoicesEvent event) {
        worker = event.getWorker();
        colony = callback.getColony();
        model = controller.getModel();
        caption.setText(i18n.get(ColonyMsg.jobTitle));
        setTitle(caption.getText());
        choices.setAccessibleText(caption.getText());
        confirm.setText(i18n.get(ColonyMsg.jobConfirm));
        cancel.setText(i18n.get(ColonyMsg.jobCancel));
        message.setText(
                event.isRejectedDrop() ? i18n.get(ColonyMsg.jobDropRejected)
                        : "");
        refresh();
        Platform.runLater(choices::requestFocus);
        showAndWait();
    }

    private boolean sameContext() {
        return controller.getModel() == model && callback.getColony() == colony
                && colony != null && colony.isValid()
                && model.tryGetUnitById(worker.getId()).orElse(null) == worker;
    }

    private void refresh() {
        choices.getSelectionModel().clearSelection();
        choices.getItems().setAll(
                sameContext() ? colony.getJobChoices(worker) : List.of());
        confirm.setDisable(true);
        details.setText("");
        if (choices.getItems().isEmpty()) {
            details.setText(i18n.get(ColonyMsg.jobNoChoices));
        } else {
            choices.getSelectionModel().selectFirst();
        }
    }

    private void confirm() {
        final ColonyJobChoice selected = choices.getSelectionModel()
                .getSelectedItem();
        if (selected == null || selected.getUnavailableReason().isPresent()) {
            return;
        }
        try {
            if (!sameContext()) {
                throw new IllegalStateException(
                        "Displayed colony or game changed");
            }
            selected.execute();
            close();
            eventBus.post(new RepaintColonyEvent());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            refresh();
            message.setText(i18n.get(ColonyMsg.jobChanged));
        }
    }

    private String goodsName(final GoodsType goods) {
        return i18n.get(GoodsTypeName.getNameForGoodsType(goods));
    }

    private String goodsText(final Goods goods) {
        return goods.getAmount() + " " + goodsName(goods.getType());
    }

    private String title(final ColonyJobChoice choice) {
        switch (choice.getKind()) {
        case CURRENT_FIELD:
            return i18n.get(ColonyMsg.jobCurrentField,
                    goodsName(choice.getGoodsType()));
        case BEST_FIELD:
            return i18n.get(ColonyMsg.jobBestField,
                    goodsName(choice.getGoodsType()));
        default:
            return i18n.get(ColonyMsg.jobIndoor,
                    i18n.get(ConstructionTypeName.getNameForType(
                            choice.getConstructionType().orElseThrow())));
        }
    }

    private String describe(final ColonyJobChoice choice) {
        final List<String> lines = new ArrayList<>();
        choice.getCurrentFieldYield().ifPresent(goods -> lines
                .add(i18n.get(ColonyMsg.jobCurrentYield, goodsText(goods))));
        choice.getBestFieldYield().ifPresent(goods -> lines
                .add(i18n.get(ColonyMsg.jobBestYield, goodsText(goods))));
        choice.getTargetLocation().ifPresent(at -> lines
                .add(i18n.get(ColonyMsg.jobTarget, at.getX(), at.getY())));
        choice.getPotential()
                .ifPresent(goods -> lines.add(i18n.get(
                        goods.getType() == GoodsType.BELL
                                ? ColonyMsg.jobBellContribution
                                : ColonyMsg.jobPotential,
                        goods.getAmount(), goodsName(goods.getType()))));
        choice.getInputNeeded().ifPresent(input -> {
            final Goods stock = choice.getCurrentStock().orElseThrow();
            lines.add(i18n.get(ColonyMsg.jobInput, input.getAmount(),
                    goodsName(input.getType()), stock.getAmount()));
            if (stock.getAmount() < input.getAmount()) {
                lines.add(i18n.get(ColonyMsg.jobShortage));
            }
        });
        if (choice.isSpecialty()) {
            lines.add(i18n.get(ColonyMsg.jobSpecialty,
                    goodsName(choice.getGoodsType())));
        }
        choice.getUnavailableReason().ifPresent(
                reason -> lines.add(i18n.get(ColonyMsg.jobUnavailable,
                        i18n.get(ColonyMsg.valueOf("job" + reason.name())))));
        lines.add(i18n.get(ColonyMsg.jobStockNote));
        return String.join("\n", lines);
    }
}
