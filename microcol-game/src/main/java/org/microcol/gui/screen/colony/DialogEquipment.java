package org.microcol.gui.screen.colony;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import com.google.inject.Inject;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.GoodsTypeName;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.gui.util.ViewUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Confirm model-provided equipment costs and revalidate after player input.
 */
public final class DialogEquipment extends AbstractMessageWindow {

    private UnitFreeColonist unit;
    private GoodsType type;
    private boolean equip;
    private Map<GoodsType, Integer> offered;

    private final Label caption;
    private final Label cost;
    private final Label message;
    private final Button confirm;
    private final Button cancel;

    private final I18n i18n;

    @Inject
    public DialogEquipment(ViewUtil viewUtil, I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;

        this.caption = new Label();
        this.cost = new Label();
        this.message = new Label();
        this.confirm = ActionButtons.button(ButtonIcon.MILITARY);
        this.cancel = ActionButtons.button(ButtonIcon.CANCEL);

        VBox root = new VBox(12);
        root.setPrefWidth(500);
        root.setId("equipmentDialog");

        caption.setId("equipmentCaption");
        cost.setId("equipmentCost");
        message.setId("equipmentMessage");

        confirm.setId("equipmentConfirm");
        cancel.setId("equipmentCancel");

        HBox buttons = new HBox(8, confirm, cancel);
        buttons.setId("equipmentButtons");

        root.getChildren().addAll(caption, cost, message, buttons);

        caption.setWrapText(true);
        cost.setWrapText(true);
        cost.setMinHeight(80);
        message.setWrapText(true);
        message.setMinHeight(100);

        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> handleConfirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> close());

        init(root);
    }

    public void show(UnitFreeColonist unit, GoodsType type, boolean equip) {
        this.unit = unit;
        this.type = type;
        this.equip = equip;

        caption.setText(i18n.get(actionKey(type, equip)));
        confirm.setText(i18n.get(ColonyMsg.equipmentConfirm));
        cancel.setText(i18n.get(ColonyMsg.equipmentCancel));
        message.setText("");

        refresh();
        javafx.application.Platform.runLater(() -> {
            if (confirm.isDisabled()) {
                cancel.requestFocus();
            } else {
                confirm.requestFocus();
            }
        });
        showAndWait();
    }

    private void refresh() {
        Optional<UnitFreeColonist.EquipmentProblem> problem = unit
                .getEquipmentProblem(type, equip);
        if (problem.isPresent() && problem
                .get() == UnitFreeColonist.EquipmentProblem.UNIT_UNAVAILABLE) {
            offered = Map.of();
            cost.setText("");
            confirm.setDisable(true);
        } else {
            offered = unit.getEquipmentGoodsChange(type, equip);
            StringBuilder sb = new StringBuilder();
            offered.forEach((gt, qty) -> {
                String name = i18n.get(GoodsTypeName.getNameForGoodsType(gt));
                if (qty < 0) {
                    sb.append(i18n.get(ColonyMsg.equipmentCost, -qty, name))
                            .append("\n");
                } else if (qty > 0) {
                    sb.append(i18n.get(ColonyMsg.equipmentReturn, qty, name))
                            .append("\n");
                }
            });
            cost.setText(sb.toString().trim());
            confirm.setDisable(problem.isPresent());
        }
        message.setText(problem
                .map(p -> i18n.get(ColonyMsg.valueOf("equipment" + p.name())))
                .orElse(""));
    }

    private void handleConfirm() {
        Optional<UnitFreeColonist.EquipmentProblem> problem = unit
                .getEquipmentProblem(type, equip);
        if (problem.isPresent()) {
            refresh();
            return;
        }

        Map<GoodsType, Integer> currentPreview = unit
                .getEquipmentGoodsChange(type, equip);
        if (!currentPreview.equals(offered)) {
            refresh();
            message.setText(i18n.get(ColonyMsg.equipmentChanged));
            return;
        }

        try {
            if (type == GoodsType.MUSKET) {
                if (equip)
                    unit.equipWithMuskets();
                else
                    unit.unequipWithMuskets();
            } else if (type == GoodsType.HORSE) {
                if (equip)
                    unit.equipWithHorses();
                else
                    unit.unequipWithHorses();
            } else if (type == GoodsType.TOOLS) {
                if (equip)
                    unit.equipWithTools();
                else
                    unit.unequipWithTools();
            } else {
                throw new IllegalArgumentException("Unsupported GoodsType");
            }
            close();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            refresh();
            message.setText(i18n.get(ColonyMsg.equipmentChanged));
        }
    }

    public static ColonyMsg actionKey(GoodsType type, boolean equip) {
        if (type == GoodsType.MUSKET) {
            return equip ? ColonyMsg.equipMuskets : ColonyMsg.unequipMuskets;
        } else if (type == GoodsType.HORSE) {
            return equip ? ColonyMsg.equipHorses : ColonyMsg.unequipHorses;
        } else if (type == GoodsType.TOOLS) {
            return equip ? ColonyMsg.equipTools : ColonyMsg.unequipTools;
        } else {
            throw new IllegalArgumentException("Unsupported GoodsType");
        }
    }
}
