package org.microcol.gui.screen.colony;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Draft export choices are applied only after explicit confirmation. */
public final class DialogCustomHouse extends AbstractMessageWindow {
    private final I18n i18n;
    private final GameModelController game;
    private final Map<GoodsType, CheckBox> choices = new LinkedHashMap<>();
    private final Label rule = new Label();
    private final Label feedback = new Label();
    private final Button confirm = new Button();
    private final Button cancel = new Button();
    private Model boundModel;
    private Colony colony;
    private Player owner;
    private Set<GoodsType> expected;

    @Inject
    public DialogCustomHouse(ViewUtil viewUtil, I18n i18n,
            GameModelController game) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.game = game;
        rule.setId("customHouseRule");
        rule.setWrapText(true);
        rule.setMinHeight(Region.USE_PREF_SIZE);
        feedback.setId("customHouseFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        GridPane grid = new GridPane();
        grid.setHgap(35);
        grid.setVgap(10);
        int index = 0;
        for (GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            CheckBox choice = new CheckBox();
            choice.setId("customHouse-" + type.name());
            choices.put(type, choice);
            grid.add(choice, index % 2, index / 2);
            index++;
        }
        confirm.setId("customHouseConfirm");
        cancel.setId("customHouseCancel");
        confirm.setDefaultButton(true);
        cancel.setCancelButton(true);
        confirm.setOnAction(event -> apply());
        cancel.setOnAction(event -> close());
        VBox root = new VBox(16, rule, grid, feedback,
                new HBox(12, confirm, cancel));
        root.setId("customHouseDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(580);
        init(root);
    }

    public void showFor(Colony colony) {
        this.colony = colony;
        this.owner = colony.getOwner();
        this.boundModel = game.getModel();
        this.expected = colony.getExportGoods();
        setTitle(i18n.get(CustomHouseText.title, colony.getName()));
        rule.setText(
                i18n.get(CustomHouseText.rule) + (owner.isDeclaredIndependence()
                        ? "\n" + i18n.get(CustomHouseText.revolution)
                        : ""));
        choices.forEach((type, choice) -> {
            choice.setText(i18n.get(GoodsTypeName.getNameForGoodsType(type)));
            choice.setSelected(expected.contains(type));
        });
        confirm.setText(i18n.get(CustomHouseText.confirm));
        cancel.setText(i18n.get(CustomHouseText.cancel));
        confirm.setDisable(!isCurrent());
        feedback.setText(
                isCurrent() ? "" : i18n.get(CustomHouseText.unavailable));
        showAndWait();
    }

    private boolean isCurrent() {
        return game.isGameModelReady() && game.getModel() == boundModel
                && boundModel.isGameRunning() && colony.isValid()
                && colony.getOwner() == owner
                && boundModel.getCurrentPlayer() == owner
                && game.getHumanPlayer() == owner
                && colony.isContainsConstructionByType(
                        ConstructionType.CUSTOM_HOUSE);
    }

    private void apply() {
        try {
            if (!isCurrent())
                throw new IllegalStateException("Custom-house view changed");
            Set<GoodsType> selected = choices.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
            colony.configureExportGoods(expected, selected);
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(CustomHouseText.changed));
        }
    }
}
