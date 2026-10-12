package org.microcol.gui.screen.colony;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyNameProblem;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class DialogColonyName extends AbstractMessageWindow {

    private final I18n i18n;
    private final TextField name = new TextField();
    private final Label message = new Label();
    private final Label caption = new Label();
    private final Button save = new Button();
    private final Set<String> suggestions = new HashSet<>();
    private Model model;
    private Colony colony;
    private Player player;
    private Unit founder;
    private Location location;
    private Colony result;

    @Inject
    public DialogColonyName(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        name.setId("colonyNameInput");
        name.setAccessibleText(i18n.get(ColonyMsg.nameLabel));
        message.setId("colonyNameError");
        message.setWrapText(true);
        message.setMinHeight(55);
        message.getStyleClass().add("name-error");
        caption.setId("colonyNameCaption");
        final Label label = new Label(i18n.get(ColonyMsg.nameLabel));
        label.setLabelFor(name);
        final Button generate = new Button(i18n.get(ColonyMsg.nameGenerate));
        generate.setId("colonyNameGenerate");
        generate.setOnAction(event -> {
            suggestions.add(name.getText());
            final String suggestion = model.suggestColonyName(suggestions);
            suggestions.add(suggestion);
            name.setText(suggestion);
            name.requestFocus();
            name.selectAll();
        });
        final Button cancel = new Button(i18n.get(ColonyMsg.nameCancel));
        cancel.setId("colonyNameCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        save.setId("colonyNameSave");
        save.setDefaultButton(true);
        save.setOnAction(event -> saveName());
        name.textProperty()
                .addListener((observable, previous, current) -> validate());
        final VBox root = new VBox(12, caption, label, name, generate, message,
                new HBox(12, cancel, save));
        root.setId("colonyNameDialog");
        root.setPrefWidth(500);
        init(root);
    }

    public Optional<Colony> showForFounding(final Model model,
            final Player player, final Unit founder) {
        this.founder = founder;
        location = founder.getLocation();
        return show(model, player, null);
    }

    public void showForRename(final Model model, final Player player,
            final Colony colony) {
        founder = null;
        show(model, player, colony);
    }

    private Optional<Colony> show(final Model model, final Player player,
            final Colony colony) {
        this.model = model;
        this.player = player;
        this.colony = colony;
        result = null;
        suggestions.clear();
        caption.setText(i18n.get(
                colony == null ? ColonyMsg.nameFound : ColonyMsg.nameRename));
        setTitle(caption.getText());
        save.setText(i18n.get(
                colony == null ? ColonyMsg.nameFound : ColonyMsg.nameSave));
        name.setText(colony == null ? model.suggestColonyName(suggestions)
                : colony.getName());
        suggestions.add(name.getText());
        validate();
        Platform.runLater(() -> {
            name.requestFocus();
            name.selectAll();
        });
        showAndWait();
        return Optional.ofNullable(result);
    }

    private boolean validate() {
        if (model == null) {
            return false;
        }
        final Optional<ColonyNameProblem> problem = model
                .getColonyNameProblem(name.getText(), colony);
        name.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"),
                problem.isPresent());
        message.setText(problem.map(
                value -> i18n.get(ColonyMsg.valueOf("name" + value.name())))
                .orElse(""));
        save.setDisable(problem.isPresent());
        return problem.isEmpty();
    }

    private void saveName() {
        if (!validate()) {
            return;
        }
        try {
            if (colony == null) {
                if (!founder.isAtPlaceLocation()
                        || !location.equals(founder.getLocation())) {
                    throw new IllegalStateException(
                            "Founding location changed");
                }
                result = model.buildColony(player, founder, name.getText());
            } else {
                model.renameColony(player, colony, name.getText());
                result = colony;
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            message.setText(i18n.get(ColonyMsg.nameChanged));
            return;
        }
        close();
    }
}
