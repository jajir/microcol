package org.microcol.gui.screen.colony;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ColonyNameProblem;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeLandQuote;
import org.microcol.model.NativeLandStatus;
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
    private final StatusBarMessageService labels;
    private final TextField name = new TextField();
    private final Label message = new Label();
    private final Label caption = new Label();
    private final Label land = new Label();
    private final Button save = new Button();
    private final Set<String> suggestions = new HashSet<>();
    private Model model;
    private Colony colony;
    private Player player;
    private Unit founder;
    private Location location;
    private List<Location> foundingLand;
    private List<NativeLandQuote> purchases = List.of();
    private Colony result;

    @Inject
    public DialogColonyName(final ViewUtil viewUtil, final I18n i18n,
            final StatusBarMessageService labels) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.labels = labels;
        land.setId("colonyNameLand");
        land.setWrapText(true);
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
        final VBox root = new VBox(12, caption, land, label, name, generate,
                message, new HBox(12, cancel, save));
        root.setId("colonyNameDialog");
        root.setPrefWidth(500);
        init(root);
    }

    /**
     * Name a new colony after the native land decision. The listed purchases
     * are charged by the model together with the founding, only on save.
     *
     * @param model     required current model
     * @param player    required founding player
     * @param founder   required founder
     * @param land      squares from {@code Model.getFoundingLand}
     * @param purchases required unpaid quotes the player chose to buy
     * @return the founded colony, or empty when canceled
     */
    public Optional<Colony> showForFounding(final Model model,
            final Player player, final Unit founder, final List<Location> land,
            final List<NativeLandQuote> purchases) {
        this.founder = founder;
        location = founder.getLocation();
        foundingLand = List.copyOf(land);
        this.purchases = List.copyOf(purchases);
        showLand(model, player);
        return show(model, player, null);
    }

    public void showForRename(final Model model, final Player player,
            final Colony colony) {
        founder = null;
        foundingLand = null;
        purchases = List.of();
        land.setText("");
        land.setManaged(false);
        land.setVisible(false);
        show(model, player, colony);
    }

    /**
     * Known claimed land of the founding with its status, and what the chosen
     * decision means. Only quotes the model gives are shown, so an unknown
     * claimant stays hidden.
     */
    private void showLand(final Model model, final Player player) {
        final List<NativeLandQuote> known = foundingLand.stream()
                .map(at -> model.quoteNativeLand(player, at))
                .flatMap(Optional::stream).collect(Collectors.toList());
        final StringBuilder text = new StringBuilder(
                labels.foundingLandText(foundingLand, known));
        if (!purchases.isEmpty()) {
            text.append('\n').append(
                    i18n.get(Loc.nativeLandFoundingPay, purchases.stream()
                            .mapToInt(NativeLandQuote::getPrice).sum()));
        } else if (known.stream().anyMatch(
                quote -> quote.getStatus() == NativeLandStatus.UNPAID)) {
            text.append('\n').append(i18n.get(Loc.nativeLandFoundingUnpaid));
        }
        land.setText(text.toString());
        land.setManaged(!known.isEmpty());
        land.setVisible(!known.isEmpty());
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
                // Founding and any chosen land purchase succeed or fail
                // together; a refusal changes neither gold nor rights.
                result = model.buildColony(player, founder, name.getText(),
                        foundingLand, purchases);
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
