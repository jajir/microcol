package org.microcol.gui.dialog;

import java.util.Optional;

import org.microcol.gui.UnitTypes;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.DisbandOffer;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation for the exact, model-provided terms of one disbanding. Losing
 * excess above a full warehouse needs its own acknowledgement before the player
 * may confirm.
 */
public final class DisbandDialog extends AbstractMessageWindow {

    private final I18n i18n;

    private Optional<Boolean> answer = Optional.empty();

    @Inject
    DisbandDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param unit  the unit to disband
     * @param offer terms to confirm
     * @return empty when the player canceled; otherwise whether the player
     *         acknowledged the excess loss
     */
    public Optional<Boolean> confirm(final Unit unit,
            final DisbandOffer offer) {
        answer = Optional.empty();
        final VBox root = new VBox(12);
        root.setPadding(new Insets(12));
        root.setPrefWidth(520);
        final Label title = new Label(i18n.get(Buttons.disbandTitle));
        title.setId("disbandTitle");
        final Label question = new Label(i18n.get(Buttons.disbandQuestion,
                i18n.get(UnitTypes.getUnitName(unit.getType()))));
        question.setId("disbandQuestion");
        question.setWrapText(true);
        question.setMinHeight(Region.USE_PREF_SIZE);
        final Label destination = new Label(offer.getReceivingColony().map(
                colony -> i18n.get(Buttons.disbandToColony, colony.getName()))
                .orElseGet(() -> i18n.get(Buttons.disbandNoColony)));
        destination.setId("disbandDestination");
        destination.setWrapText(true);
        destination.setMinHeight(Region.USE_PREF_SIZE);
        final Label tools = equipmentLine("disbandToolsLine",
                Buttons.disbandToolsLine, offer, GoodsType.TOOLS);
        final Label horses = equipmentLine("disbandHorsesLine",
                Buttons.disbandHorsesLine, offer, GoodsType.HORSE);
        final Label muskets = equipmentLine("disbandMusketsLine",
                Buttons.disbandMusketsLine, offer, GoodsType.MUSKET);
        final CheckBox overflow = new CheckBox(
                i18n.get(Buttons.disbandOverflowAcknowledge));
        overflow.setId("disbandOverflowAcknowledge");
        overflow.setWrapText(true);
        overflow.setVisible(offer.hasExcessLoss());
        overflow.setManaged(offer.hasExcessLoss());
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Buttons.disbandConfirm),
                i18n.get(Buttons.disbandCancel));
        buttons.getButtonYes().setId("disbandConfirm");
        buttons.getButtonNo().setId("disbandCancel");
        if (offer.hasExcessLoss()) {
            buttons.getButtonYes().disableProperty()
                    .bind(overflow.selectedProperty().not());
        }
        buttons.getButtonYes().setOnAction(event -> {
            answer = Optional.of(overflow.isSelected());
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().addAll(title, question, destination, tools, horses,
                muskets, overflow, buttons.getContent());
        init(root);
        setTitle(i18n.get(Buttons.disbandTitle));
        showAndWait();
        return answer;
    }

    private Label equipmentLine(final String id, final Buttons key,
            final DisbandOffer offer, final GoodsType type) {
        final Label line = new Label(
                i18n.get(key, offer.getRetained(type), offer.getLost(type)));
        line.setId(id);
        line.setWrapText(true);
        return line;
    }

    @Override
    protected void onCancelDialog() {
        answer = Optional.empty();
        close();
    }

}
