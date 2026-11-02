package org.akropia.gui.dialog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.screen.europe.Europe;
import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.NavalCombatOutcome;
import org.akropia.model.Unit;
import org.akropia.model.event.UnitAttackedEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;

/**
 * Acknowledges immutable naval losses and prizes after the battle animation.
 */
public final class DialogNavalOutcome extends AbstractWarningDialog {
    private final I18n i18n;

    @Inject
    public DialogNavalOutcome(ViewUtil viewUtil, I18n i18n) {
        super(viewUtil, i18n, Dialog.fight_navalTitle);
        this.i18n = i18n;
    }

    public void showOutcome(UnitAttackedEvent event) {
        NavalCombatOutcome outcome = event.getNavalOutcome().orElseThrow();
        Unit loser = event.getDefeated();
        Unit winner = outcome.getWinnerId() == event.getAttacker().getId()
                ? event.getAttacker()
                : event.getDefender();
        String ship = loser.getOwner().getDisplayName() + ": "
                + i18n.get(UnitTypes.getUnitName(loser.getType())) + " #"
                + loser.getId();
        String destination = outcome.isEuropeRepairDestination()
                ? i18n.get(Europe.voyageDestinationEurope)
                : outcome.getRepairDestinationName();
        String message = outcome.isSunk()
                ? i18n.get(Dialog.fight_navalSunk, ship)
                : i18n.get(Dialog.fight_navalDamaged, ship, destination);
        message += "\n" + i18n.get(Dialog.fight_navalCaptured,
                winner.getOwner().getDisplayName(), outcome.getWinnerId(),
                goods(outcome.getCapturedGoods()));
        message += "\n" + (outcome.isSunk()
                ? i18n.get(Dialog.fight_navalLost, outcome.getLostPassengers(),
                        goods(outcome.getLostGoods()))
                : i18n.get(Dialog.fight_navalPreserved,
                        outcome.getPreservedPassengers(),
                        goods(outcome.getRemainingGoods())));
        Label text = new Label(message);
        text.setId("navalBattleOutcome");
        text.setWrapText(true);
        text.setMaxWidth(560);
        text.setPrefWidth(560);
        getContext().setPadding(new Insets(12));
        getContext().setSpacing(12);
        getContext().getChildren()
                .setAll(new Label(i18n.get(Dialog.fight_navalTitle)), text);
        setTitle(i18n.get(Dialog.fight_navalTitle));
        showAndWait();
    }

    private String goods(List<Goods> items) {
        Map<GoodsType, Integer> totals = new LinkedHashMap<>();
        items.forEach(item -> totals.merge(item.getType(), item.getAmount(),
                Integer::sum));
        return totals.isEmpty() ? i18n.get(Dialog.fight_none)
                : totals.entrySet().stream()
                        .map(item -> item.getValue() + " "
                                + i18n.get(GoodsTypeName
                                        .getNameForGoodsType(item.getKey())))
                        .collect(Collectors.joining(", "));
    }
}
