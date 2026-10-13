package org.microcol.gui.screen.europe;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
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

    public PanelPortPierUnit(final Unit unit, final ImageProvider imageProvider,
            final DialogEuropeEquipment equipmentDialog, final I18n i18n,
            final EuropeCallback callback) {
        this.unit = unit;
        this.equipmentDialog = equipmentDialog;
        this.i18n = i18n;
        this.callback = callback;
        final Image image = imageProvider.getUnitImage(unit);
        final ImageView imageIcon = new ImageView(image);
        paneImage = new Pane(imageIcon);
        paneImage.getStyleClass().add("unit-icon");
        paneImage.setId("europe-pier-unit-" + unit.getId());
        paneImage.setFocusTraversable(unit instanceof UnitFreeColonist);
        if (unit instanceof UnitFreeColonist) {
            paneImage.setAccessibleText(i18n.get(Europe.equipmentOpen));
            Tooltip.install(paneImage,
                    new Tooltip(i18n.get(Europe.equipmentOpen)));
        }
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
    }

    private void showEquipmentMenu() {
        if (!(unit instanceof UnitFreeColonist)) {
            return;
        }
        paneImage.requestFocus();
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        equipmentMenu.getItems().clear();
        addEquipmentChoice(colonist, GoodsType.MUSKET,
                !colonist.isHoldingGuns());
        addEquipmentChoice(colonist, GoodsType.HORSE, !colonist.isMounted());
        addEquipmentChoice(colonist, GoodsType.TOOLS,
                !colonist.isHoldingTools());
        equipmentMenu.show(paneImage, Side.BOTTOM, 0, 0);
    }

    private void addEquipmentChoice(final UnitFreeColonist colonist,
            final GoodsType type, final boolean buy) {
        final MenuItem item = new MenuItem(
                i18n.get(DialogEuropeEquipment.actionKey(type, buy)));
        item.setId("europe-equipment-" + type.name());
        item.setOnAction(event -> {
            equipmentDialog.show(colonist, type, buy);
            callback.repaintAfterGoodMoving();
        });
        equipmentMenu.getItems().add(item);
    }

}
