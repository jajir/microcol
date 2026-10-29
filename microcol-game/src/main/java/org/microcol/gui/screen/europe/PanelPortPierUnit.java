package org.microcol.gui.screen.europe;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.DialogOrdainMissionary;
import org.microcol.gui.dialog.OrdinationMsg;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.unit.UnitActionType;
import javafx.scene.control.Label;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public final class PanelPortPierUnit extends HBox {

    private final ContextMenu equipmentMenu = new ContextMenu();
    private final Pane paneImage;
    private final Unit unit;
    private final DialogEuropeEquipment equipmentDialog;
    private final I18n i18n;
    private final EuropeCallback callback;
    private final GameModelController gameModelController;
    private final DialogMessage orderMessage;
    private final DialogOrdainMissionary ordinationDialog;

    public PanelPortPierUnit(final Unit unit, final ImageProvider imageProvider,
            final DialogEuropeEquipment equipmentDialog, final I18n i18n,
            final EuropeCallback callback,
            final GameModelController gameModelController,
            final DialogMessage orderMessage,
            final DialogOrdainMissionary ordinationDialog) {
        this.gameModelController = gameModelController;
        this.orderMessage = orderMessage;
        this.ordinationDialog = ordinationDialog;
        this.unit = unit;
        this.equipmentDialog = equipmentDialog;
        this.i18n = i18n;
        this.callback = callback;
        final Image image = imageProvider.getUnitImage(unit);
        final ImageView imageIcon = new ImageView(image);
        paneImage = new Pane(imageIcon);
        paneImage.setMaxSize(45, 45);
        paneImage.getStyleClass().add("unit-icon");
        paneImage.setId("europe-pier-unit-" + unit.getId());
        paneImage.setFocusTraversable(true);
        paneImage.setAccessibleText(i18n.get(Europe.pierOrders));
        paneImage.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY
                    && event.isStillSincePress()) {
                showEquipmentMenu();
                event.consume();
            }
        });
        paneImage.setOnContextMenuRequested(event -> {
            showEquipmentMenu();
            event.consume();
        });
        paneImage.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    || event.getCode() == KeyCode.SPACE
                    || event.getCode() == KeyCode.CONTEXT_MENU
                    || (event.isShiftDown()
                            && event.getCode() == KeyCode.F10)) {
                showEquipmentMenu();
                event.consume();
            }
        });
        paneImage.setOnDragDetected(e -> {
            ClipboardWritter.make(imageIcon.startDragAndDrop(TransferMode.MOVE))
                    .addImage(image).addTransferFromEuropePortPier()
                    .addUnit(unit).build();
            e.consume();
        });
        getChildren().addAll(paneImage);
        if (unit.getAction().getType() == UnitActionType.sentry) {
            final Label marker = new Label("S");
            marker.setId("sentry-marker-" + unit.getId());
            marker.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE,
                    javafx.scene.layout.Region.USE_PREF_SIZE);
            marker.getStyleClass().add("sentry-marker");
            marker.setAccessibleText(i18n.get(Buttons.buttonSentry));
            marker.setAccessibleHelp(i18n.get(Buttons.sentryExplanation));
            getChildren().add(marker);
        }
        if (unit.isBoardingPriority() || unit.isBoardingOptedOut()) {
            final Label marker = new Label(i18n
                    .get(unit.isBoardingPriority() ? Europe.immigrantFrontMarker
                            : Europe.immigrantAshoreMarker));
            marker.setId("immigrant-marker-" + unit.getId());
            marker.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE,
                    javafx.scene.layout.Region.USE_PREF_SIZE);
            marker.getStyleClass().add("sentry-marker");
            getChildren().add(marker);
        }
    }

    private void showEquipmentMenu() {
        paneImage.requestFocus();
        equipmentMenu.getItems().clear();
        if (unit instanceof UnitFreeColonist) {
            final UnitFreeColonist colonist = (UnitFreeColonist) unit;
            addEquipmentChoice(colonist, GoodsType.MUSKET,
                    !colonist.isHoldingGuns());
            addEquipmentChoice(colonist, GoodsType.HORSE,
                    !colonist.isMounted());
            addEquipmentChoice(colonist, GoodsType.TOOLS,
                    !colonist.isHoldingTools());
            addOrdinationChoice();
        }
        if (unit.isImmigrant()) {
            addBoardingChoices();
        }
        final boolean sentry = unit.getAction()
                .getType() == UnitActionType.sentry;
        final MenuItem order = new MenuItem(i18n.get(
                sentry ? Buttons.buttonActivateSentry : Buttons.buttonSentry));
        order.setId("europe-sentry-" + unit.getId());
        order.setDisable(!(sentry
                ? gameModelController.getModel().canActivateSentry(unit)
                : gameModelController.getModel().canSetSentry(unit)));
        order.setOnAction(event -> {
            try {
                if (sentry) {
                    gameModelController.getModel().activateSentry(unit);
                } else {
                    gameModelController.getModel().setSentry(unit);
                }
            } catch (IllegalStateException
                    | IllegalArgumentException rejected) {
                orderMessage.setTitle(i18n.get(Europe.title));
                orderMessage.setText(i18n.get(Buttons.unitOrderUnavailable));
                orderMessage.showAndWait();
            }
            callback.repaintAfterGoodMoving();
        });
        equipmentMenu.getItems().add(order);
        equipmentMenu.show(paneImage, Side.BOTTOM, 0, 0);
    }

    /**
     * Willingness and front-of-queue choices. Each choice is authoritative in
     * the model; a rejected stale choice explains itself and changes nothing.
     */
    private void addBoardingChoices() {
        final Model model = gameModelController.getModel();
        final boolean willing = unit.isWillingToBoard();
        final MenuItem boarding = new MenuItem(
                i18n.get(willing ? Europe.immigrantStayAshore
                        : Europe.immigrantBoardAgain));
        boarding.setId("europe-boarding-" + unit.getId());
        boarding.setDisable(!(willing ? model.canOptOutOfBoarding(unit)
                : model.canRestoreBoarding(unit)));
        boarding.setOnAction(event -> changeBoarding(() -> {
            if (willing) {
                model.optOutOfBoarding(unit);
            } else {
                model.restoreBoarding(unit);
            }
        }));
        equipmentMenu.getItems().add(boarding);
        final MenuItem front = new MenuItem(
                i18n.get(Europe.immigrantFrontOfQueue));
        front.setId("europe-boarding-front-" + unit.getId());
        front.setDisable(!model.canPrioritizeBoarding(unit));
        front.setOnAction(
                event -> changeBoarding(() -> model.prioritizeBoarding(unit)));
        equipmentMenu.getItems().add(front);
    }

    private void changeBoarding(final Runnable change) {
        try {
            change.run();
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            orderMessage.setTitle(i18n.get(Europe.title));
            orderMessage.setText(i18n.get(Europe.immigrantBoardingUnavailable));
            orderMessage.showAndWait();
        }
        callback.repaintAfterGoodMoving();
    }

    private void addOrdinationChoice() {
        final MenuItem ordain = new MenuItem(
                i18n.get(OrdinationMsg.ordainMenu));
        ordain.setId("europe-ordain-" + unit.getId());
        ordain.setDisable(unit.isCrownSeized());
        ordain.setOnAction(event -> {
            ordinationDialog.show(unit);
            callback.repaintAfterGoodMoving();
        });
        equipmentMenu.getItems().add(ordain);
    }

    private void addEquipmentChoice(final UnitFreeColonist colonist,
            final GoodsType type, final boolean buy) {
        final MenuItem item = new MenuItem(
                i18n.get(DialogEuropeEquipment.actionKey(type, buy)));
        item.setId("europe-equipment-" + type.name());
        // A Crown-seized pier unit is inaccessible to its former owner
        // (R-0187).
        item.setDisable(colonist.isCrownSeized());
        item.setOnAction(event -> {
            equipmentDialog.show(colonist, type, buy);
            callback.repaintAfterGoodMoving();
        });
        equipmentMenu.getItems().add(item);
    }

}
