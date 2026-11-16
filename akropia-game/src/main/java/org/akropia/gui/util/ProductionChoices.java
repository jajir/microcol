package org.akropia.gui.util;

import java.util.List;
import java.util.function.Consumer;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.colony.ColonyMsg;
import org.akropia.i18n.I18n;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The same production rows in the colony detail and unit menus. */
public final class ProductionChoices {
    private ProductionChoices() {
    }

    public static RadioButton row(final VBox output,
            final ColonyJobChoice choice, final GoodsType current,
            final ToggleGroup group, final ImageProvider images,
            final I18n i18n, final String prefix) {
        final GoodsType goodsType = choice.getGoodsType();
        final String name = i18n
                .get(GoodsTypeName.getNameForGoodsType(goodsType));
        final ImageView icon = new ImageView(
                images.getGoodsTypeImage(goodsType));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        final Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        final Label amount = new Label(choice.getPotential()
                .map(goods -> Integer.toString(goods.getAmount())).orElse("—"));
        amount.setId(prefix + "yield-" + goodsType.name());
        final HBox content = new HBox(8, icon, new Label(name), space, amount);
        content.setAlignment(Pos.CENTER_LEFT);
        content.prefWidthProperty().bind(output.widthProperty().subtract(40));
        final RadioButton row = new RadioButton();
        row.setGraphic(content);
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setId(prefix + "output-" + goodsType.name());
        row.setUserData(goodsType);
        row.getStyleClass().add("field-output-choice");
        row.setToggleGroup(group);
        row.setSelected(goodsType == current);
        row.setAccessibleText(name + choice.getPotential()
                .map(goods -> ", "
                        + i18n.get(ColonyMsg.yieldSummary, goods.getAmount()))
                .orElse(""));
        row.setAccessibleHelp(describe(choice, i18n));
        row.setDisable(choice.getUnavailableReason().isPresent());
        row.getProperties().put("jobChoice", choice);
        row.setSkin(new FramedRadioButtonSkin(row));
        return row;
    }

    public static VBox table(final List<ColonyJobChoice> choices,
            final GoodsType current, final ImageProvider images,
            final I18n i18n, final String prefix,
            final Consumer<ColonyJobChoice> execute) {
        final VBox output = new VBox(4);
        output.setPrefWidth(340);
        final Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        final HBox header = new HBox(8,
                new Label(i18n.get(ColonyMsg.outputLabel)), space,
                new Label(i18n.get(ColonyMsg.outputYield)));
        header.getStyleClass().add("field-output-header");
        output.getChildren().add(header);
        final Label detail = new Label();
        detail.setId("jobDetails");
        detail.setWrapText(true);
        detail.setMaxWidth(340);
        final boolean indoor = choices.stream().anyMatch(
                choice -> choice.getKind() == ColonyJobChoice.Kind.INDOOR);
        detail.setVisible(indoor);
        detail.setManaged(indoor);
        if (indoor) {
            detail.setPrefHeight(125);
        }
        final ToggleGroup group = new ToggleGroup();
        for (final GoodsType goods : GoodsType.GOOD_TYPES) {
            choices.stream().filter(choice -> choice.getGoodsType() == goods)
                    .forEach(choice -> {
                        final RadioButton row = row(output, choice, current,
                                group, images, i18n, prefix);
                        row.setOnAction(event -> execute.accept(choice));
                        row.addEventFilter(
                                javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                                event -> {
                                    if (event
                                            .getButton() == javafx.scene.input.MouseButton.PRIMARY
                                            && row.isSelected()) {
                                        event.consume();
                                        execute.accept(choice);
                                    }
                                });
                        row.setOnMouseEntered(event -> detail
                                .setText(row.getAccessibleHelp()));
                        row.focusedProperty().addListener(
                                (observable, previous, focused) -> {
                                    if (focused) {
                                        detail.setText(row.getAccessibleHelp());
                                    }
                                });
                        if (row.isDisabled()) {
                            final VBox unavailable = new VBox(row);
                            unavailable.setFocusTraversable(true);
                            unavailable
                                    .setAccessibleText(row.getAccessibleText());
                            unavailable
                                    .setAccessibleHelp(row.getAccessibleHelp());
                            unavailable.setOnMouseEntered(event -> detail
                                    .setText(row.getAccessibleHelp()));
                            unavailable.focusedProperty().addListener(
                                    (observable, previous, focused) -> {
                                        if (focused) {
                                            detail.setText(
                                                    row.getAccessibleHelp());
                                        }
                                    });
                            output.getChildren().add(unavailable);
                        } else {
                            output.getChildren().add(row);
                        }
                    });
        }
        output.getChildren().add(detail);
        output.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.SPACE
                            || event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        event.consume();
                    }
                });
        output.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.SPACE
                            || event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        final javafx.scene.Node focused = output.getScene()
                                .getFocusOwner();
                        if (focused instanceof RadioButton
                                && !focused.isDisabled()) {
                            execute.accept((ColonyJobChoice) focused
                                    .getProperties().get("jobChoice"));
                        }
                        event.consume();
                    }
                });
        return output;
    }

    public static void focus(final VBox table) {
        table.getChildren().stream().filter(
                node -> node instanceof RadioButton && !node.isDisabled())
                .findFirst().ifPresent(javafx.scene.Node::requestFocus);
    }

    private static String describe(final ColonyJobChoice choice,
            final I18n i18n) {
        final java.util.List<String> lines = new java.util.ArrayList<>();
        final String name = i18n
                .get(GoodsTypeName.getNameForGoodsType(choice.getGoodsType()));
        choice.getPotential()
                .ifPresent(goods -> lines.add(i18n.get(
                        goods.getType() == GoodsType.OWLS
                                ? ColonyMsg.jobOwlContribution
                                : ColonyMsg.jobPotential,
                        goods.getAmount(), name)));
        choice.getCurrentFieldYield().ifPresent(
                goods -> lines.add(i18n.get(ColonyMsg.jobCurrentYield,
                        goods.getAmount() + " " + name)));
        choice.getTargetLocation().ifPresent(at -> lines
                .add(i18n.get(ColonyMsg.jobTarget, at.getX(), at.getY())));
        choice.getInputNeeded().ifPresent(input -> {
            final Goods stock = choice.getCurrentStock().orElseThrow();
            lines.add(i18n.get(ColonyMsg.jobInput, input.getAmount(),
                    i18n.get(
                            GoodsTypeName.getNameForGoodsType(input.getType())),
                    stock.getAmount()));
            if (stock.getAmount() < input.getAmount()) {
                lines.add(i18n.get(ColonyMsg.jobShortage));
            }
        });
        if (choice.isSpecialty()) {
            lines.add(i18n.get(ColonyMsg.jobSpecialty, name));
        }
        choice.getUnavailableReason().ifPresent(
                reason -> lines.add(i18n.get(ColonyMsg.jobUnavailable,
                        i18n.get(ColonyMsg.valueOf("job" + reason.name())))));
        return String.join("\n", lines);
    }

}
