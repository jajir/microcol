package org.microcol.gui.screen.statistics;

import java.util.List;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.colony.buildingqueue.ConstructionText;
import org.microcol.i18n.I18n;
import org.microcol.model.ColonyAdvisorReport;
import org.microcol.model.ColonyAdvisorReport.ColonyStatus;
import org.microcol.model.ColonyAdvisorReport.GoodsLine;
import org.microcol.model.ColonyAdvisorReport.Project;
import org.microcol.model.ColonyAdvisorReport.ProjectState;
import org.microcol.model.ColonyAdvisorReport.Uncertainty;
import org.microcol.model.Model;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Colony advisor (R-0200): for each owned colony its residents, the expected
 * new colonists or famine, the goods of the next update and the active project.
 * Every showing rebuilds the figures from the live model; the panel keeps no
 * figure of its own. Only the Open button leaves the report, and it changes
 * nothing.
 * <p>
 * Sentences wrap within the visible width of the scroll pane. The goods table
 * keeps every column at its natural width, so headings and amounts never break
 * inside a word; when the table is wider than the visible width, the same
 * scroll pane scrolls it horizontally.
 * </p>
 */
final class ColonyAdvisorPanel extends VBox {
    private final GameModelController game;
    private final I18n i18n;
    private final ColonyAdvisorNavigator navigator;
    private final Label note = label("colonyAdvisorNote", "");
    private final VBox colonies = new VBox(12);
    private final ScrollPane scroll = new ScrollPane(colonies);
    private final Label feedback = label("colonyAdvisorFeedback", "");

    /** The game the shown rows were built from; Open is valid only for it. */
    private Model shownModel;

    ColonyAdvisorPanel(final GameModelController game, final I18n i18n,
            final ColonyAdvisorNavigator navigator) {
        super(12);
        this.game = game;
        this.i18n = i18n;
        this.navigator = navigator;
        setId("colonyAdvisor");
        getStyleClass().add("colony-advisor");
        scroll.setId("colonyAdvisorScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(330);
        scroll.setPrefViewportWidth(400);
        getChildren().addAll(note, scroll, feedback);
    }

    /** Rebuilds every colony section from the live model. */
    void showReport() {
        colonies.getChildren().clear();
        feedback.setText("");
        if (!game.isGameModelReady()) {
            shownModel = null;
            note.setText("");
            feedback.setText(i18n.get(ColonyAdvisorText.unavailable));
            return;
        }
        note.setText(i18n.get(ColonyAdvisorText.note));
        shownModel = game.getModel();
        final ColonyAdvisorReport report = shownModel
                .getColonyAdvisorReport(game.getHumanPlayer());
        if (report.getColonies().isEmpty()) {
            colonies.getChildren().add(label("colonyAdvisorEmpty",
                    i18n.get(ColonyAdvisorText.empty)));
            return;
        }
        for (final ColonyStatus colony : report.getColonies()) {
            colonies.getChildren().add(section(colony));
        }
    }

    private VBox section(final ColonyStatus colony) {
        final int id = colony.getColonyId();
        final Project project = colony.getProject();
        final VBox section = new VBox(6);
        section.setId("colonySection-" + id);
        section.getStyleClass().add("relation-row");
        section.getChildren().add(heading(colony, section));
        section.getChildren().add(
                visible(label("colonyGrowth-" + id, growth(colony)), section));
        if (colony.getUncertainty() != Uncertainty.NONE) {
            section.getChildren()
                    .add(visible(
                            label("colonyUncertain-" + id,
                                    uncertainty(colony.getUncertainty())),
                            section));
        }
        section.getChildren().add(goodsTable(colony));
        section.getChildren()
                .add(visible(
                        label("colonyProject-" + id,
                                project(project, colony.getUncertainty())),
                        section));
        if (project.getItem().isPresent()) {
            section.getChildren().add(visible(label("colonyProjectStock-" + id,
                    i18n.get(ColonyAdvisorText.projectStock,
                            project.getHammers(), project.getRequiredHammers(),
                            project.getTools(), project.getRequiredTools())),
                    section));
        }
        return section;
    }

    /**
     * The colony name with its residents and the Open button beside it, kept
     * within the visible width so Open is reachable without scrolling sideways.
     */
    private HBox heading(final ColonyStatus colony, final VBox section) {
        final int id = colony.getColonyId();
        final Label name = label("colonyName-" + id,
                i18n.get(ColonyAdvisorText.colonyRow, colony.getName(),
                        colony.getPopulation()));
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);
        final Button open = new Button(i18n.get(ColonyAdvisorText.open));
        open.setId("colonyOpen-" + id);
        open.setMinWidth(Region.USE_PREF_SIZE);
        open.setOnAction(event -> open(colony));
        final HBox heading = new HBox(10, name, open);
        heading.setId("colonyHeading-" + id);
        heading.setAlignment(Pos.CENTER_LEFT);
        return visible(heading, section);
    }

    /**
     * Limits a sentence or row to the visible width of the scroll pane inside
     * the section, so it wraps there even when the goods table scrolls
     * sideways.
     */
    private <T extends Region> T visible(final T region, final VBox section) {
        region.maxWidthProperty()
                .bind(Bindings
                        .createDoubleBinding(
                                () -> Math.max(0,
                                        scroll.getViewportBounds().getWidth()
                                                - section.getInsets().getLeft()
                                                - section.getInsets()
                                                        .getRight()),
                                scroll.viewportBoundsProperty(),
                                section.insetsProperty()));
        return region;
    }

    /** Names the expected change of the residents at the next update. */
    private String growth(final ColonyStatus colony) {
        if (colony.isFamine()) {
            return i18n.get(ColonyAdvisorText.growthFamine);
        }
        if (colony.getNewColonists() > 0) {
            return i18n.get(ColonyAdvisorText.growthBirths,
                    colony.getNewColonists());
        }
        return i18n.get(ColonyAdvisorText.growthNone);
    }

    /** Names why the next update cannot be read exactly; empty when it can. */
    private String uncertainty(final Uncertainty uncertainty) {
        switch (uncertainty) {
        case LOST_COLONIST:
            return i18n.get(ColonyAdvisorText.uncertainLost);
        case COLONY_LOST:
            return i18n.get(ColonyAdvisorText.uncertainColonyLost);
        case STORAGE_CHANGE:
            return i18n.get(ColonyAdvisorText.uncertainStorage);
        default:
            return "";
        }
    }

    /**
     * One row per good. The stock and its projected change and next stock come
     * first, then the output, use and spending that explain the change. A
     * figure that depends on the uncertain update shows a question mark instead
     * of an amount; the stock is always the current one.
     */
    private GridPane goodsTable(final ColonyStatus colony) {
        final int id = colony.getColonyId();
        final Uncertainty uncertainty = colony.getUncertainty();
        final boolean lostColonist = uncertainty == Uncertainty.LOST_COLONIST
                || uncertainty == Uncertainty.COLONY_LOST;
        final boolean storageChange = uncertainty == Uncertainty.STORAGE_CHANGE;
        final GridPane table = new GridPane();
        table.setId("colonyGoods-" + id);
        table.setHgap(12);
        table.setVgap(4);
        table.setMinWidth(Region.USE_PREF_SIZE);
        final List<String> headers = List.of(
                i18n.get(ColonyAdvisorText.tableGood),
                i18n.get(ColonyAdvisorText.tableStock),
                i18n.get(ColonyAdvisorText.tableChange),
                i18n.get(ColonyAdvisorText.tableNext),
                i18n.get(ColonyAdvisorText.tableOutput),
                i18n.get(ColonyAdvisorText.tableUsed),
                i18n.get(ColonyAdvisorText.tableOther));
        for (int column = 0; column < headers.size(); column++) {
            table.add(cell("colonyHeader-" + id + "-" + column,
                    headers.get(column)), column, 0);
        }
        int row = 1;
        for (final GoodsLine line : colony.getGoods()) {
            final String type = line.getType().name();
            table.add(
                    cell("colonyGood-" + id + "-" + type,
                            i18n.get(GoodsTypeName
                                    .getNameForGoodsType(line.getType()))),
                    0, row);
            table.add(cell("colonyStock-" + id + "-" + type,
                    String.valueOf(line.getStock())), 1, row);
            table.add(cell("colonyChange-" + id + "-" + type,
                    lostColonist || storageChange ? unknown()
                            : signed(line.getChange())),
                    2, row);
            table.add(cell("colonyNext-" + id + "-" + type,
                    lostColonist || storageChange ? unknown() : next(line)), 3,
                    row);
            table.add(cell("colonyOutput-" + id + "-" + type,
                    lostColonist ? unknown() : output(line)), 4, row);
            table.add(
                    cell("colonyUsed-" + id + "-" + type,
                            lostColonist ? unknown()
                                    : String.valueOf(line.getConsumed())),
                    5, row);
            table.add(
                    cell("colonyOther-" + id + "-" + type,
                            lostColonist
                                    || storageChange
                                            ? unknown()
                                            : signed(-(line.getSpent()
                                                    + line.getExported()))),
                    6, row);
            row++;
        }
        return table;
    }

    private String output(final GoodsLine line) {
        if (line.getBlocked() == 0) {
            return String.valueOf(line.getProduced());
        }
        return i18n.get(ColonyAdvisorText.outputBlocked, line.getProduced(),
                line.getBlocked());
    }

    private String next(final GoodsLine line) {
        if (line.getLost() == 0) {
            return String.valueOf(line.getNext());
        }
        return i18n.get(ColonyAdvisorText.nextLost, line.getNext(),
                line.getLost());
    }

    private String unknown() {
        return i18n.get(ColonyAdvisorText.unknownValue);
    }

    /** A plus or minus sign makes the direction readable without colour. */
    private static String signed(final int amount) {
        return amount > 0 ? "+" + amount : String.valueOf(amount);
    }

    private String project(final Project project,
            final Uncertainty uncertainty) {
        final String name = project.getItem()
                .map(item -> ConstructionText.name(i18n, item)).orElse("");
        if (project.getItem().isPresent()
                && uncertainty == Uncertainty.LOST_COLONIST) {
            return i18n.get(ColonyAdvisorText.projectUncertain, name);
        }
        if (project.getItem().isPresent()
                && uncertainty == Uncertainty.COLONY_LOST) {
            return i18n.get(ColonyAdvisorText.projectColonyLost, name);
        }
        final ProjectState state = project.getState();
        switch (state) {
        case WAITING_HAMMERS:
            return i18n.get(ColonyAdvisorText.projectWaitingHammers, name);
        case WAITING_TOOLS:
            return i18n.get(ColonyAdvisorText.projectWaitingTools, name,
                    project.getMissingTools());
        case COMPLETES:
            return i18n.get(ColonyAdvisorText.projectCompletes, name);
        case BLOCKED:
            return i18n.get(ColonyAdvisorText.projectBlocked, name);
        default:
            return i18n.get(ColonyAdvisorText.projectNone);
        }
    }

    /**
     * Opens the colony through the navigator. A colony that is no longer one of
     * the game's own colonies reports unavailable after the list is rebuilt.
     */
    private void open(final ColonyStatus shown) {
        final ColonyAdvisorNavigator.Result result = navigator.open(shownModel,
                shown);
        if (result == ColonyAdvisorNavigator.Result.OPENED) {
            return;
        }
        showReport();
        feedback.setText(i18n.get(ColonyAdvisorText.colonyUnavailable));
    }

    /** A sentence that wraps within the width it is given. */
    private static Label label(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        return label;
    }

    /**
     * A table cell on one line that never shrinks below its text, so a heading
     * or amount is not broken inside a word.
     */
    private static Label cell(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }
}
