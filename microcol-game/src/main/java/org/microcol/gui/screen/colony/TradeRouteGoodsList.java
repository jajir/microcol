package org.microcol.gui.screen.colony;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.TradeRouteStop;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Ordered list of at most six distinct goods for one stop and direction. The
 * top entry has priority; Move up and Move down change that order explicitly.
 * The list only stages the player's choice: the model validates and stores it.
 */
final class TradeRouteGoodsList {

    private final I18n i18n;
    private final Runnable changed;
    private final Label heading = new Label();
    private final ListView<GoodsType> list = new ListView<>();
    private final ComboBox<GoodsType> choice = new ComboBox<>();
    private final Button add = new Button();
    private final Button remove = new Button();
    private final Button up = new Button();
    private final Button down = new Button();
    private final VBox box;

    /**
     * @param idPrefix required prefix of the control IDs, for example
     *                 {@code tradeRouteUnload}
     * @param i18n     required translations
     * @param changed  required callback after every staged change
     */
    TradeRouteGoodsList(final String idPrefix, final I18n i18n,
            final Runnable changed) {
        this.i18n = i18n;
        this.changed = changed;
        list.setId(idPrefix + "List");
        choice.setId(idPrefix + "Choice");
        add.setId(idPrefix + "Add");
        remove.setId(idPrefix + "Remove");
        up.setId(idPrefix + "Up");
        down.setId(idPrefix + "Down");
        for (final Button button : List.of(up, down, remove)) {
            button.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        }
        heading.setWrapText(true);
        heading.setMaxWidth(350);
        list.setPrefSize(350, 140);
        list.setCellFactory(view -> new ListCell<GoodsType>() {
            @Override
            protected void updateItem(final GoodsType item,
                    final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : (getIndex() + 1) + ". " + name(item));
            }
        });
        choice.setConverter(new StringConverter<GoodsType>() {
            @Override
            public String toString(final GoodsType type) {
                return type == null ? "" : name(type);
            }

            @Override
            public GoodsType fromString(final String value) {
                throw new UnsupportedOperationException();
            }
        });
        choice.setPrefWidth(150);
        choice.setOnAction(event -> updateButtons());
        list.getSelectionModel().selectedIndexProperty()
                .addListener((observable, previous, current) -> {
                    updateButtons();
                });
        add.setOnAction(event -> addChosen());
        remove.setOnAction(event -> removeSelected());
        up.setOnAction(event -> moveSelected(-1));
        down.setOnAction(event -> moveSelected(1));
        box = new VBox(6, heading, list, new HBox(6, choice, add),
                new HBox(6, up, down, remove));
        refreshChoices();
        updateButtons();
    }

    private String name(final GoodsType type) {
        return i18n.get(GoodsTypeName.getNameForGoodsType(type));
    }

    VBox getNode() {
        return box;
    }

    /** Sets the fixed texts in the language current when the dialog opens. */
    void translate(final String headingText) {
        heading.setText(headingText);
        add.setText(i18n.get(TradeRouteText.addGoods));
        remove.setText(i18n.get(TradeRouteText.removeGoods));
        up.setText(i18n.get(TradeRouteText.moveUp));
        down.setText(i18n.get(TradeRouteText.moveDown));
        list.setAccessibleText(headingText);
        choice.setAccessibleText(headingText);
        list.refresh();
    }

    /** @return the staged goods, top (highest priority) first */
    List<GoodsType> getGoods() {
        return new ArrayList<>(list.getItems());
    }

    /** Replaces the staged goods without reporting a change. */
    void setGoods(final List<GoodsType> goods) {
        list.getItems().setAll(goods);
        list.getSelectionModel().clearSelection();
        refreshChoices();
        updateButtons();
    }

    void setEnabled(final boolean enabled) {
        box.setDisable(!enabled);
    }

    private void refreshChoices() {
        choice.getItems()
                .setAll(GoodsType.BUYABLE_GOOD_TYPES.stream()
                        .filter(type -> !list.getItems().contains(type))
                        .collect(Collectors.toList()));
        choice.setValue(null);
    }

    private void updateButtons() {
        final int selected = list.getSelectionModel().getSelectedIndex();
        add.setDisable(choice.getValue() == null
                || list.getItems().size() >= TradeRouteStop.MAX_CARGO_GOODS);
        remove.setDisable(selected < 0);
        up.setDisable(selected <= 0);
        down.setDisable(selected < 0 || selected >= list.getItems().size() - 1);
    }

    private void addChosen() {
        final GoodsType type = choice.getValue();
        if (type == null
                || list.getItems().size() >= TradeRouteStop.MAX_CARGO_GOODS
                || list.getItems().contains(type)) {
            return;
        }
        list.getItems().add(type);
        list.getSelectionModel().select(list.getItems().size() - 1);
        refreshChoices();
        updateButtons();
        changed.run();
    }

    private void removeSelected() {
        final int selected = list.getSelectionModel().getSelectedIndex();
        if (selected < 0) {
            return;
        }
        list.getItems().remove(selected);
        list.refresh();
        refreshChoices();
        updateButtons();
        changed.run();
    }

    private void moveSelected(final int delta) {
        final int index = list.getSelectionModel().getSelectedIndex();
        final int target = index + delta;
        if (index < 0 || target < 0 || target >= list.getItems().size()) {
            return;
        }
        final GoodsType type = list.getItems().remove(index);
        list.getItems().add(target, type);
        list.getSelectionModel().select(target);
        list.refresh();
        updateButtons();
        changed.run();
    }
}
