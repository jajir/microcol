package org.microcol.gui.screen.statistics;

import java.util.Map;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropeTradeAccount;
import org.microcol.model.EuropeanNation;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.Player;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Read-only European trade accounts. The model supplies every value, including
 * the revolutionary custom-house subset; this panel only names and shows them.
 */
final class EuropeanTradePanel extends VBox {
    private final GameModelController game;
    private final I18n i18n;
    private final VBox rows = new VBox(12);
    private final Label feedback = new Label();

    EuropeanTradePanel(final GameModelController game, final I18n i18n) {
        super(12);
        this.game = game;
        this.i18n = i18n;
        setId("europeanTrade");
        getStyleClass().add("relations-report");
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setId("tradeScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setPrefViewportWidth(400);
        feedback.setId("tradeFeedback");
        feedback.setWrapText(true);
        getChildren().addAll(scroll, feedback);
    }

    /** Refreshes the report from the live model every time it is shown. */
    void showReport() {
        rows.getChildren().clear();
        feedback.setText("");
        if (!game.isGameModelReady()) {
            feedback.setText(i18n.get(Stats.tradeUnavailable));
            return;
        }
        final Model model = game.getModel();
        final Map<EuropeanNation, Map<GoodsType, EuropeTradeAccount>> accounts = model
                .getEurope().getTradeAccounts();
        for (final EuropeanNation nation : EuropeanNation.values()) {
            final Map<GoodsType, EuropeTradeAccount> goods = accounts
                    .getOrDefault(nation, Map.of());
            for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
                if (goods.containsKey(type)) {
                    rows.getChildren()
                            .add(row(model, nation, type, goods.get(type)));
                }
            }
        }
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(new Label(i18n.get(Stats.tradeEmpty)));
        }
    }

    private VBox row(final Model model, final EuropeanNation nation,
            final GoodsType type, final EuropeTradeAccount account) {
        final VBox row = new VBox(4);
        row.setId("trade-" + nation.name() + "-" + type.name());
        row.getStyleClass().add("relation-row");
        final Label title = new Label(label(model, nation) + " — "
                + i18n.get(GoodsTypeName.getNameForGoodsType(type)));
        title.getStyleClass().add("relation-names");
        title.setWrapText(true);
        title.setMaxWidth(380);
        row.getChildren().addAll(title,
                line(i18n.get(Stats.tradeBought, account.getBoughtQuantity())),
                line(i18n.get(Stats.tradeSpent, account.getBoughtGold())),
                line(i18n.get(Stats.tradeSold, account.getSoldQuantity())),
                line(i18n.get(Stats.tradeReceived, account.getSoldGold())));
        if (account.getRevolutionaryQuantity() > 0) {
            row.getChildren()
                    .add(line(i18n.get(Stats.tradeRevolutionary,
                            account.getRevolutionaryQuantity(),
                            account.getRevolutionaryGold())));
        }
        return row;
    }

    private static Label line(final String text) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(380);
        return label;
    }

    /** Rivals are named; their cargo and units are never shown here. */
    private String label(final Model model, final EuropeanNation nation) {
        return model.getPlayers().stream()
                .filter(player -> !player.isKing()
                        && player.getName().equals(nation.getPlayerName()))
                .findFirst().map(this::displayName)
                .orElse(nation.getPlayerName());
    }

    private String displayName(final Player player) {
        final String display = player.getDisplayName();
        return display.equals(player.getName()) ? display
                : display + " (" + player.getName() + ")";
    }
}
