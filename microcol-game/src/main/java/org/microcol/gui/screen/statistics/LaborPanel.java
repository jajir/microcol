package org.microcol.gui.screen.statistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.LaborMember;
import org.microcol.model.LaborReport;
import org.microcol.model.Model;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Labor report (R-0198): the living colonists of the human nation by type, then
 * the people of the chosen type with their current place. Every showing
 * rebuilds the report from the live model; the selected type is only the view
 * state.
 */
final class LaborPanel extends VBox {
    private final GameModelController game;
    private final I18n i18n;
    private final LaborNavigator navigator;
    private final Label note = label("laborNote", "");
    private final Label total = label("laborTotal", "");
    private final ToggleGroup typeGroup = new ToggleGroup();
    private final FlowPane types = new FlowPane(8, 8);
    private final VBox members = new VBox(12);
    private final Label feedback = label("laborFeedback", "");
    private UnitType selectedType;

    /** The game the shown rows were built from; Show is valid only for it. */
    private Model shownModel;

    LaborPanel(final GameModelController game, final I18n i18n,
            final LaborNavigator navigator) {
        super(12);
        this.game = game;
        this.i18n = i18n;
        this.navigator = navigator;
        setId("labor");
        getStyleClass().add("relations-report");
        types.setId("laborTypes");
        final ScrollPane scroll = new ScrollPane(members);
        scroll.setId("laborScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(300);
        scroll.setPrefViewportWidth(400);
        getChildren().addAll(note, total, types, scroll, feedback);
    }

    /** Rebuilds the counts and the chosen type from the live model. */
    void showReport() {
        types.getChildren().clear();
        typeGroup.getToggles().clear();
        members.getChildren().clear();
        feedback.setText("");
        if (!game.isGameModelReady()) {
            shownModel = null;
            total.setText("");
            note.setText("");
            feedback.setText(i18n.get(LaborText.unavailable));
            return;
        }
        note.setText(i18n.get(LaborText.note));
        shownModel = game.getModel();
        final LaborReport report = shownModel
                .getLaborReport(game.getHumanPlayer());
        total.setText(i18n.get(LaborText.total, report.getTotal()));
        if (report.getTypes().isEmpty()) {
            selectedType = null;
            members.getChildren()
                    .add(label("laborEmpty", i18n.get(LaborText.empty)));
            return;
        }
        if (selectedType == null || report.getCount(selectedType) == 0) {
            selectedType = report.getTypes().get(0);
        }
        for (final UnitType type : report.getTypes()) {
            types.getChildren().add(typeButton(type, report.getCount(type)));
        }
        for (final LaborMember member : report.getMembers(selectedType)) {
            members.getChildren().add(row(member));
        }
    }

    private ToggleButton typeButton(final UnitType type, final int count) {
        final ToggleButton button = new ToggleButton(i18n.get(LaborText.typeRow,
                i18n.get(UnitTypes.getUnitName(type)), count));
        button.setId("laborType-" + type.name());
        button.setToggleGroup(typeGroup);
        button.setSelected(type.equals(selectedType));
        button.setOnAction(event -> {
            selectedType = type;
            showReport();
        });
        return button;
    }

    private HBox row(final LaborMember member) {
        final int id = member.getUnitId();
        final VBox text = new VBox(4,
                label("laborMember-" + id, i18n.get(LaborText.personLine, id)),
                label("laborLocation-" + id, LaborLocationText.describe(i18n,
                        member.getLocation())));
        final String flags = flags(member);
        if (!flags.isEmpty()) {
            text.getChildren().add(label("laborFlags-" + id, flags));
        }
        final Button show = new Button(i18n.get(LaborText.show));
        show.setId("laborShow-" + id);
        show.setDisable(!member.getLocation().isNavigable());
        show.setOnAction(event -> open(member));
        final HBox row = new HBox(12, text, show);
        row.setId("laborRow-" + id);
        row.getStyleClass().add("relation-row");
        return row;
    }

    /** Veteran rank and equipment describe the person; neither adds a count. */
    private String flags(final LaborMember member) {
        final List<String> flags = new ArrayList<>();
        if (member.isVeteran()) {
            flags.add(i18n.get(LaborText.veteran));
        }
        if (member.isNativeConvert()) {
            flags.add(i18n.get(LaborText.nativeConvert));
        }
        if (!member.getEquipment().isEmpty()) {
            flags.add(i18n.get(LaborText.equipment, equipment(member)));
        }
        return String.join(", ", flags);
    }

    private String equipment(final LaborMember member) {
        final List<String> items = new ArrayList<>();
        member.getEquipment().entrySet().stream()
                .sorted(Map.Entry
                        .comparingByKey(Comparator.comparing(GoodsType::name)))
                .forEach(entry -> items.add(i18n
                        .get(GoodsTypeName.getNameForGoodsType(entry.getKey()))
                        + " " + entry.getValue()));
        return String.join(", ", items);
    }

    /**
     * Opens the place of the person. A person that has left the report or a
     * place without destination is reported after the list is rebuilt.
     */
    private void open(final LaborMember shown) {
        final LaborNavigator.Result result = navigator.open(shownModel, shown);
        if (result == LaborNavigator.Result.OPENED) {
            return;
        }
        showReport();
        feedback.setText(i18n.get(result == LaborNavigator.Result.UNAVAILABLE
                ? LaborText.personUnavailable
                : LaborText.noDestination));
    }

    private static Label label(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMaxWidth(380);
        return label;
    }
}
