package org.microcol.gui.screen.colony;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.dialog.NativeLandDialog;
import org.microcol.gui.dialog.NativeLandGuard;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.ClearSpecialtyProblem;
import org.microcol.model.Colony;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;

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
    private final NativeLandGuard nativeLand;
    private final Label caption = new Label();
    private final ListView<ColonyJobChoice> choices = new ListView<>();
    private final Label details = new Label();
    private final Label message = new Label();
    private final Button confirm = ActionButtons.button(ButtonIcon.CONFIRM);
    private final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
    private final Button clear = ActionButtons.button(ButtonIcon.REMOVE);
    private Colony colony;
    private Model model;
    private Unit worker;
    private boolean clearing;

    @Inject
    public DialogColonyJobChoices(final ViewUtil viewUtil, final I18n i18n,
            final EventBus eventBus, final GameModelController controller,
            final ColonyDialogCallback callback,
            final NativeLandGuard nativeLand) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.controller = controller;
        this.callback = callback;
        this.nativeLand = nativeLand;
        final VBox root = new VBox(10, caption, choices, details, message,
                new HBox(10, confirm, clear, cancel));
        root.setPadding(new Insets(16));
        root.setPrefWidth(640);
        root.setId("jobDialog");
        choices.setId("jobChoices");
        details.setId("jobDetails");
        message.setId("jobMessage");
        confirm.setId("jobConfirm");
        cancel.setId("jobCancel");
        clear.setId("jobClearSpecialty");
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
                    if (clearing) {
                        return;
                    }
                    details.setText(choice == null ? "" : describe(choice));
                    confirm.setDisable(choice == null
                            || choice.getUnavailableReason().isPresent());
                });
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> confirm());
        clear.setOnAction(event -> beginClearing());
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
        clear.setText(i18n.get(ColonyMsg.clearOpen));
        clearing = false;
        choices.setDisable(false);
        final boolean specialist = worker instanceof UnitFreeColonist
                && ((UnitFreeColonist) worker).hasCivilianSpecialty();
        clear.setVisible(specialist);
        clear.setManaged(specialist);
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

    private void beginClearing() {
        clearing = true;
        choices.setDisable(true);
        clear.setVisible(false);
        clear.setManaged(false);
        confirm.setText(i18n.get(ColonyMsg.clearConfirm));
        showClearing(false);
        Platform.runLater(confirm::requestFocus);
    }

    private void showClearing(final boolean changed) {
        final Optional<ClearSpecialtyProblem> problem = sameContext()
                ? colony.getClearSpecialtyProblem(worker)
                : Optional.of(ClearSpecialtyProblem.UNAVAILABLE);
        details.setText(i18n.get(ColonyMsg.clearSummary,
                i18n.get(UnitTypes.getUnitName(worker.getType())),
                worker.getId(),
                i18n.get(UnitTypes.getUnitName(UnitType.COLONIST))));
        message.setText(
                (changed ? i18n.get(ColonyMsg.clearChanged) : "") + problem
                        .map(reason -> (changed ? "\n" : "") + i18n.get(
                                ColonyMsg.valueOf("clear" + reason.name())))
                        .orElse(""));
        confirm.setDisable(problem.isPresent());
    }

    private void confirmClearing() {
        try {
            if (!sameContext()) {
                throw new IllegalStateException(
                        "Displayed colony or game changed");
            }
            colony.clearSpecialty(worker);
            close();
            eventBus.post(new RepaintColonyEvent());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            showClearing(true);
        }
    }

    private void confirm() {
        if (clearing) {
            if (!confirm.isDisable()) {
                confirmClearing();
            }
            return;
        }
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
            final Optional<Location> target = selected.getTargetLocation();
            if (target.isPresent() && model
                    .quoteNativeLand(controller.getHumanPlayer(), target.get())
                    .filter(quote -> quote
                            .getStatus() == NativeLandStatus.UNPAID)
                    .isPresent()) {
                // Close first so the land question is not stacked on this
                // modal dialog; canceling it changes nothing.
                close();
                Platform.runLater(
                        () -> executeAfterQuestion(selected, target.get()));
                return;
            }
            selected.execute();
            close();
            eventBus.post(new RepaintColonyEvent());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            refresh();
            message.setText(i18n.get(ColonyMsg.jobChanged));
        }
    }

    private void executeAfterQuestion(final ColonyJobChoice selected,
            final Location target) {
        if (controller.getModel() != model) {
            return;
        }
        final NativeLandDialog.Result land = nativeLand.proceed(model,
                controller.getHumanPlayer(), target);
        if (land == NativeLandDialog.Result.CANCELED) {
            return;
        }
        try {
            selected.execute();
            eventBus.post(new RepaintColonyEvent());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land, i18n.get(ColonyMsg.jobChanged)),
                    StatusBarMessageEvent.Source.COLONY));
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
