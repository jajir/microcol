package org.akropia.gui.screen.colony;

import org.akropia.gui.Loc;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.dialog.DialogMessage;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ProductionChoices;
import org.akropia.gui.util.ViewUtil;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;
import org.akropia.model.unit.UnitFreeColonist;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

/**
 * Colony-local indoor work and the independent clear-specialty confirmation.
 */
public final class ColonyWorkMenu {
    private final EventBus events;
    private final ColonyDialogCallback callback;
    private final GameModelController controller;
    private final ImageProvider images;
    private final I18n i18n;
    private final ViewUtil view;
    private final DialogMessage message;

    @Inject
    public ColonyWorkMenu(final EventBus events,
            final ColonyDialogCallback callback,
            final GameModelController controller, final ImageProvider images,
            final I18n i18n, final ViewUtil view, final DialogMessage message) {
        this.events = events;
        this.callback = callback;
        this.controller = controller;
        this.images = images;
        this.i18n = i18n;
        this.view = view;
        this.message = message;
        events.register(this);
    }

    @Subscribe
    private void configure(final ConfigureColonyWorkMenuEvent event) {
        final Colony colony = callback.getColony();
        final Unit worker = event.worker;
        final Menu work = new Menu(i18n.get(
                event.includeWork ? Loc.workForColony : Loc.workInBuildings,
                colony.getName()));
        work.setUserData("mapJob");
        work.setId(
                (event.includeWork ? "workForColony-" : "indoorWorkForColony-")
                        + colony.getId());
        final java.util.List<ColonyJobChoice> choices = colony
                .getJobChoices(worker).stream()
                .filter(choice -> choice
                        .getKind() == ColonyJobChoice.Kind.INDOOR)
                .collect(java.util.stream.Collectors.toList());
        final org.akropia.model.GoodsType current = worker
                .isAtPlaceConstructionSlot()
                        ? worker.getPlaceConstruction().getConstructionSlot()
                                .getConstructionType().getProductionPerTurn()
                                .map(org.akropia.model.Goods::getType)
                                .orElse(null)
                        : null;
        final VBox table = ProductionChoices.table(choices, current, images,
                i18n, "colony-work-", choice -> {
                    event.menu.hide();
                    try {
                        choice.execute();
                        events.post(new RepaintColonyEvent());
                    } catch (IllegalStateException
                            | IllegalArgumentException rejected) {
                        showRejectedDrop();
                    }
                });
        final CustomMenuItem production = new CustomMenuItem(table, false);
        production.getStyleClass().add("production-menu-item");
        work.getItems().add(production);
        work.setOnShown(shown -> javafx.application.Platform.runLater(
                () -> org.akropia.gui.util.ProductionChoices.focus(table)));
        event.menu.getItems().add(Math.min(event.includeWork ? 3 : 4,
                event.menu.getItems().size()), work);
        if (worker instanceof UnitFreeColonist
                && ((UnitFreeColonist) worker).hasCivilianSpecialty()) {
            final MenuItem clear = new MenuItem(i18n.get(ColonyMsg.clearOpen));
            clear.setUserData("mapJob");
            clear.setId("jobClearSpecialty");
            clear.setOnAction(
                    action -> new ClearSpecialty(colony, worker).showAndWait());
            event.menu.getItems().add(clear);
        }
    }

    @Subscribe
    private void rejected(final ColonyWorkDropRejectedEvent event) {
        showRejectedDrop();
    }

    private void showRejectedDrop() {
        message.setText(i18n.get(ColonyMsg.jobDropRejected),
                i18n.get(ColonyMsg.jobChanged));
        message.showAndWait();
    }

    private final class ClearSpecialty extends AbstractMessageWindow {
        private final Label detail = new Label();
        private final Label problem = new Label();
        private final Button confirm = ActionButtons.button(ButtonIcon.CONFIRM);
        private final Colony colony;
        private final Unit worker;
        private final org.akropia.model.Model model;

        ClearSpecialty(final Colony colony, final Unit worker) {
            super(ColonyWorkMenu.this.view, ColonyWorkMenu.this.i18n);
            this.colony = colony;
            this.worker = worker;
            model = controller.getModel();
            final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
            cancel.setText(i18n.get(ColonyMsg.jobCancel));
            cancel.setId("jobCancel");
            cancel.setCancelButton(true);
            cancel.setOnAction(event -> close());
            confirm.setId("jobConfirm");
            confirm.setText(i18n.get(ColonyMsg.clearConfirm));
            confirm.setDefaultButton(true);
            confirm.setOnAction(event -> {
                try {
                    if (!sameContext()) {
                        throw new IllegalStateException("Colony changed");
                    }
                    colony.clearSpecialty(worker);
                    close();
                    events.post(new RepaintColonyEvent());
                } catch (IllegalStateException
                        | IllegalArgumentException rejected) {
                    refresh(true);
                }
            });
            detail.setId("jobDetails");
            problem.setId("jobMessage");
            detail.setWrapText(true);
            problem.setWrapText(true);
            final VBox root = new VBox(12, detail, problem,
                    new ModalActions(confirm, cancel));
            root.setPadding(new javafx.geometry.Insets(16));
            root.setPrefWidth(560);
            root.setId("clearSpecialtyDialog");
            init(root);
            setTitle(i18n.get(ColonyMsg.clearOpen));
            refresh(false);
        }

        private boolean sameContext() {
            return controller.getModel() == model
                    && callback.getColony() == colony && colony.isValid()
                    && model.tryGetUnitById(worker.getId())
                            .orElse(null) == worker;
        }

        private void refresh(final boolean changed) {
            detail.setText(i18n.get(ColonyMsg.clearSummary,
                    i18n.get(UnitTypes.getUnitName(worker.getType())),
                    worker.getId(),
                    i18n.get(UnitTypes.getUnitName(UnitType.COLONIST))));
            final java.util.Optional<org.akropia.model.ClearSpecialtyProblem> reason = sameContext()
                    ? colony.getClearSpecialtyProblem(worker)
                    : java.util.Optional.of(
                            org.akropia.model.ClearSpecialtyProblem.UNAVAILABLE);
            problem.setText((changed ? i18n.get(ColonyMsg.clearChanged) + "\n"
                    : "")
                    + reason.map(value -> i18n
                            .get(ColonyMsg.valueOf("clear" + value.name())))
                            .orElse(""));
            confirm.setDisable(reason.isPresent());
        }
    }
}
