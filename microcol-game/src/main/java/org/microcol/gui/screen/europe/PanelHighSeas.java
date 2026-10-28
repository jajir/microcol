package org.microcol.gui.screen.europe;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.UnitTypes;
import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.JettisonFlow;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.TitledPanel;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.Unit;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.VoyageReversalOffer;
import org.microcol.model.unit.Ship;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 *
 * @param <T> message key type used for ship travel text
 */
public final class PanelHighSeas<T extends Enum<T> & MessageKeyResource>
        implements JavaFxComponent, Repaintable, UpdatableLanguage {

    private boolean isShownShipsTravelingToEurope;

    private final EuropeCallback europeDialog;

    private final HBox shipsContainer;
    private final ScrollPane scroll;
    private final ToggleGroup selection = new ToggleGroup();
    private final VBox voyageDetail = new VBox(4);
    private int selectedShipId = -1;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final TitledPanel titledPanel;

    private final EventBus eventBus;

    private final I18n i18n;

    private final DialogMessage dialogMessage;

    private final VoyageReversalDialog reversalDialog;

    private final JettisonFlow jettisonFlow;

    private T titleKey;

    private T onMouseEnteredKey;

    @Inject
    public PanelHighSeas(final EuropeCallback europeDialog,
            final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final EventBus eventBus, final I18n i18n,
            final DialogMessage dialogMessage,
            final VoyageReversalDialog reversalDialog,
            final JettisonFlow jettisonFlow) {
        this.europeDialog = Preconditions.checkNotNull(europeDialog);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.reversalDialog = Preconditions.checkNotNull(reversalDialog);
        this.jettisonFlow = Preconditions.checkNotNull(jettisonFlow);

        shipsContainer = new HBox();
        shipsContainer.setMinWidth(Region.USE_PREF_SIZE);
        shipsContainer.getStyleClass().add("voyage-ships");
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(
                shipsContainer, this::isItCorrectObject);
        shipsContainer.setOnDragEntered(backgroundHighlighter::onDragEntered);
        shipsContainer.setOnDragExited(backgroundHighlighter::onDragExited);
        shipsContainer.setOnDragOver(this::onDragOver);
        shipsContainer.setOnDragDropped(this::onDragDropped);
        shipsContainer.minHeightProperty().set(60);

        titledPanel = new TitledPanel();
        titledPanel.getStyleClass().add("ships-container");
        scroll = new ScrollPane(shipsContainer);
        scroll.getStyleClass().add("harbor-scroll");
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        shipsContainer.minWidthProperty()
                .bind(scroll.widthProperty().subtract(4));
        scroll.setMinHeight(104);
        scroll.setPrefHeight(104);
        scroll.setMaxHeight(104);
        titledPanel.getChildren().addAll(scroll, voyageDetail);
        titledPanel.setOnMouseEntered(this::onMouseEntered);
        titledPanel.setOnMouseExited(this::onMouseExited);
    }

    private void onMouseEntered(
            @SuppressWarnings("unused") final MouseEvent event) {
        if (onMouseEnteredKey != null) {
            eventBus.post(new StatusBarMessageEvent(i18n.get(onMouseEnteredKey),
                    Source.EUROPE));
        }
    }

    private void onMouseExited(
            @SuppressWarnings("unused") final MouseEvent event) {
        eventBus.post(new StatusBarMessageEvent(Source.EUROPE));
    }

    public void addStyle(final String style) {
        titledPanel.getStyleClass().add(style);
    }

    @Override
    public void repaint() {
        selection.getToggles().clear();
        voyageDetail.getChildren().clear();
        shipsContainer.getChildren().clear();
        showShips();
        if (selection.getSelectedToggle() == null
                && !selection.getToggles().isEmpty()) {
            ((ToggleButton) selection.getToggles().get(0)).fire();
        }
        updateLanguage(i18n);
    }

    private void onDragOver(final DragEvent event) {
        if (isItCorrectObject(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private boolean isItCorrectObject(final Dragboard db) {
        if (!isShownShipsTravelingToEurope && db.hasString()) {
            return ClipboardEval.make(gameModelController.getModel(), db)
                    .filterUnit(this::isDepartingShip).getUnit().isPresent();
        } else {
            return false;
        }
    }

    private boolean isDepartingShip(final Unit unit) {
        return unit.isShip() && unit.isAtEuropePort()
                && unit.getOwner() == gameModelController.getHumanPlayer();
    }

    private void onDragDropped(final DragEvent event) {
        final Optional<Unit> oUnit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .filterUnit(this::isDepartingShip).getUnit();
        if (oUnit.isPresent()) {
            final Unit unit = oUnit.get();
            try {
                unit.placeToHighSeas(false);
                europeDialog.repaint();
                event.setDropCompleted(true);
            } catch (IllegalStateException rejected) {
                event.setDropCompleted(false);
                dialogMessage.setTitle(i18n.get(Europe.title));
                dialogMessage.setText(i18n.get(Europe.voyageUnavailable));
                dialogMessage.showAndWait();
            }
            event.consume();
        }
    }

    private void showShips() {
        gameModelController.getModel().getHighSea()
                .getUnitsTravelingTo(gameModelController.getHumanPlayer(),
                        isShownShipsTravelingToEurope)
                .forEach(unit -> {
                    final PlaceHighSea voyage = (PlaceHighSea) unit.getPlace();
                    final Label remaining = new Label(
                            voyage.getRemainigTurns() == 0
                                    && !voyage.isTravelToEurope()
                                            ? i18n.get(Europe.voyageWaiting)
                                            : i18n.get(Europe.voyageTurns,
                                                    voyage.getRemainigTurns()));
                    remaining.setId("voyage-remaining-" + unit.getId());
                    remaining.setWrapText(true);
                    remaining.setMinHeight(Region.USE_PREF_SIZE);
                    remaining.setMaxWidth(200);
                    final Button reverse = ActionButtons.button(ButtonIcon.BACK,
                            i18n.get(Europe.voyageReverse));
                    reverse.setId("voyage-reverse-" + unit.getId());
                    // Turning an outbound ship toward a closed home Europe is
                    // refused by the model, so the control is unavailable.
                    reverse.setDisable(!gameModelController.getModel()
                            .isGameRunning()
                            || gameModelController.getModel()
                                    .getCurrentPlayer() != unit.getOwner()
                            || !voyage.isTravelToEurope()
                                    && !unit.getOwner().canTradeInEurope());
                    reverse.setOnAction(event -> reverseCourse((Ship) unit));
                    final Button jettison = ActionButtons.button(
                            ButtonIcon.JETTISON,
                            i18n.get(Europe.voyageJettison));
                    jettison.setId("voyage-jettison-" + unit.getId());
                    jettison.setDisable(!gameModelController.getModel()
                            .isGameRunning()
                            || gameModelController.getModel()
                                    .getCurrentPlayer() != unit.getOwner());
                    jettison.setOnAction(event -> jettisonCargo((Ship) unit));
                    final Label identity = new Label(
                            i18n.get(UnitTypes.getUnitName(unit.getType()))
                                    + " #" + unit.getId());
                    identity.setId("voyage-identity-" + unit.getId());
                    final ImageView icon = new ImageView(
                            imageProvider.getUnitImage(unit));
                    icon.setFitWidth(45);
                    icon.setFitHeight(45);
                    icon.setPreserveRatio(true);
                    final ToggleButton ship = ActionButtons
                            .toggle(ButtonIcon.SHIP, "#" + unit.getId());
                    ship.setGraphic(icon);
                    ship.setId("voyage-ship-" + unit.getId());
                    ship.setContentDisplay(ContentDisplay.TOP);
                    ship.setToggleGroup(selection);
                    ship.setMinWidth(Region.USE_PREF_SIZE);
                    ship.setTooltip(new Tooltip(
                            identity.getText() + " · " + remaining.getText()));
                    ship.setAccessibleText(
                            identity.getText() + " · " + remaining.getText());
                    ship.setOnAction(event -> {
                        ship.setSelected(true);
                        selectedShipId = unit.getId();
                        voyageDetail.getChildren().setAll(
                                new HBox(12, identity, remaining),
                                new HBox(4, reverse, jettison));
                    });
                    shipsContainer.getChildren().add(ship);
                    if (selectedShipId == unit.getId()) {
                        ship.fire();
                    }
                });
    }

    /**
     * Discards goods through the shared flow. The list is redrawn afterwards
     * even when the flow was canceled or refused, so it shows the live cargo.
     */
    private void jettisonCargo(final Ship ship) {
        jettisonFlow.jettison(gameModelController.getModel(), ship);
        europeDialog.repaint();
    }

    private void reverseCourse(final Ship ship) {
        try {
            final VoyageReversalOffer offer = gameModelController.getModel()
                    .offerVoyageReversal(ship);
            if (reversalDialog.confirm(offer)) {
                gameModelController.getModel().reverseVoyage(offer);
                europeDialog.repaint();
            }
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            europeDialog.repaint();
            dialogMessage.setTitle(i18n.get(Europe.title));
            dialogMessage.setText(i18n.get(Europe.voyageReverseStale));
            dialogMessage.showAndWait();
        }
    }

    /**
     * @param isShownShipsTravelingToEurope the isShownShipsTravelingToEurope to
     *                                      set
     */
    public void setShownShipsTravelingToEurope(
            final boolean isShownShipsTravelingToEurope) {
        this.isShownShipsTravelingToEurope = isShownShipsTravelingToEurope;
        scroll.setId(isShownShipsTravelingToEurope ? "europe-incoming-scroll"
                : "europe-outgoing-scroll");
    }

    @Override
    public Region getContent() {
        return titledPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        titledPanel.setTitle(i18n.get(titleKey) + " · "
                + shipsContainer.getChildren().size());
    }

    /**
     * @param titleKey the titleKey to set
     */
    public void setTitleKey(T titleKey) {
        this.titleKey = Preconditions.checkNotNull(titleKey);
    }

    /**
     * @param onMouseOverKey the onMouseOverKey to set
     */
    public void setOnMouseEnteredKey(T onMouseOverKey) {
        this.onMouseEnteredKey = Preconditions.checkNotNull(onMouseOverKey);
    }

}
