package org.akropia.gui.screen.editor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.screen.menu.GameMenu;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.NativeNation;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.store.NativeSettlementPo;
import org.akropia.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Right editing panel: the player's nation, the tools with their variants, the
 * attributes of the selected square with their removal controls, the native
 * settlement form, the units and ship passengers, and the latest feedback.
 */
@Singleton
public class EditorRightPanel implements JavaFxComponent {

    static final String MESSAGE_ID = "editorMessage";

    static final String TILE_ID = "editorTileLabel";

    /** Fixed width of the panel; the map takes the rest of the window. */
    static final double PANEL_WIDTH = 300;

    private final VBox mainPanel = new VBox(6);

    private final ScrollPane scroll = new ScrollPane(mainPanel);

    private final VBox inspector = new VBox(6);

    private final Label nationLabel = new Label();

    private final ComboBox<EuropeanNation> nation = new ComboBox<>(
            FXCollections.observableArrayList(EuropeanNation.values()));

    private final Label toolsLabel = new Label();

    private final ToggleGroup tools = new ToggleGroup();

    private final ToggleButton toolSelect = tool("editorToolSelect",
            ButtonIcon.CENTER);
    private final ToggleButton toolTerrain = tool("editorToolTerrain",
            ButtonIcon.LAND);
    private final ToggleButton toolForest = tool("editorToolForest",
            ButtonIcon.TREE);
    private final ToggleButton toolField = tool("editorToolField",
            ButtonIcon.PLOW);
    private final ToggleButton toolRiver = tool("editorToolRiver",
            ButtonIcon.OCEAN);
    private final ToggleButton toolBonus = tool("editorToolBonus",
            ButtonIcon.GOLD);
    private final ToggleButton toolSettlement = tool("editorToolSettlement",
            ButtonIcon.DIPLOMACY);
    private final ToggleButton toolUnit = tool("editorToolUnit",
            ButtonIcon.LABOR);

    private final ComboBox<TerrainType> terrainChoice = new ComboBox<>(
            FXCollections.observableArrayList(TerrainType.TERRAINS));

    private final ComboBox<RiverSize> riverChoice = new ComboBox<>(FXCollections
            .observableArrayList(RiverSize.SMALL, RiverSize.LARGE));

    private final ComboBox<ResourceOverlay> bonusChoice = new ComboBox<>(
            FXCollections
                    .observableArrayList(Arrays.stream(ResourceOverlay.values())
                            .filter(overlay -> overlay != ResourceOverlay.NONE)
                            .collect(Collectors.toList())));

    private final ComboBox<NativeNation> tribeChoice = new ComboBox<>(
            FXCollections.observableArrayList(NativeNation.values()));

    private final CheckBox capitalChoice = new CheckBox();

    private final ComboBox<UnitType> unitChoice = new ComboBox<>(
            FXCollections.observableArrayList(ScenarioDraft.EXPEDITION_TYPES));

    private final Label message = new Label();

    private final ModelService modelService;

    private final EditorActions actions;

    private final EditorMessages messages;

    private final LocalizationHelper localization;

    private final I18n i18n;

    @Inject
    EditorRightPanel(final MouseOperationManager mouseOperationManager,
            final ModelService modelService, final EditorActions actions,
            final EditorMessages messages,
            final LocalizationHelper localization, final I18n i18n) {
        this.modelService = Preconditions.checkNotNull(modelService);
        this.actions = Preconditions.checkNotNull(actions);
        this.messages = Preconditions.checkNotNull(messages);
        this.localization = Preconditions.checkNotNull(localization);
        this.i18n = Preconditions.checkNotNull(i18n);

        nation.setId("editorNation");
        named(nation,
                value -> i18n.get(
                        GameMenu.valueOf("setupNation" + value.name().charAt(0)
                                + value.name().substring(1).toLowerCase())));
        nation.setOnAction(event -> {
            if (nation.getValue() != null) {
                actions.setNation(nation.getValue());
            }
        });

        terrainChoice.setId("editorTerrainChoice");
        named(terrainChoice, localization::getTerrainName);
        terrainChoice.setValue(TerrainType.GRASSLAND);
        riverChoice.setId("editorRiverChoice");
        named(riverChoice,
                size -> i18n.get(size == RiverSize.LARGE ? EditorText.riverLarge
                        : EditorText.riverSmall));
        riverChoice.setValue(RiverSize.SMALL);
        bonusChoice.setId("editorBonusChoice");
        named(bonusChoice, localization::getResourceOverlayName);
        bonusChoice.setValue(ResourceOverlay.ORE);
        tribeChoice.setId("editorTribeChoice");
        named(tribeChoice, NativeNation::getDisplayName);
        tribeChoice.setValue(NativeNation.ARAWAK);
        capitalChoice.setId("editorCapitalChoice");
        capitalChoice.setSelected(true);
        unitChoice.setId("editorUnitChoice");
        named(unitChoice, localization::getUnitName);
        unitChoice.setValue(UnitType.COLONIST);

        toolSelect.setUserData((MouseOperation) null);
        toolTerrain.setUserData(painting(location -> actions.change(location,
                () -> modelService.getDraft().setTerrain(location,
                        terrainChoice.getValue()))));
        toolForest.setUserData(
                (MouseOperation) location -> actions.change(location,
                        () -> modelService.getDraft().setForest(location,
                                !modelService.hasTrees(location))));
        toolField.setUserData((MouseOperation) location -> actions
                .change(location, () -> modelService.getDraft()
                        .setField(location, !modelService.hasField(location))));
        toolRiver.setUserData(painting(location -> actions.change(location,
                () -> modelService.getDraft().setRiver(location,
                        riverChoice.getValue()))));
        toolBonus.setUserData((MouseOperation) location -> actions
                .change(location, () -> modelService.getDraft()
                        .setResource(location, bonusChoice.getValue())));
        toolSettlement.setUserData(
                (MouseOperation) location -> actions.change(location,
                        () -> modelService.getDraft().placeSettlement(location,
                                tribeChoice.getValue(),
                                capitalChoice.isSelected())));
        toolUnit.setUserData((MouseOperation) location -> actions
                .change(location, () -> modelService.getDraft()
                        .addUnit(unitChoice.getValue(), location)));
        tools.selectedToggleProperty()
                .addListener((property, before, after) -> {
                    if (after == null) {
                        // Clicking the active tool keeps it; one tool is always
                        // chosen.
                        before.setSelected(true);
                        return;
                    }
                    mouseOperationManager
                            .setOperation((MouseOperation) after.getUserData());
                });
        toolSelect.setSelected(true);
        // Opening or changing a variant chooses its tool, also when the
        // default variant stays chosen.
        variant(terrainChoice, toolTerrain);
        variant(riverChoice, toolRiver);
        variant(bonusChoice, toolBonus);
        variant(tribeChoice, toolSettlement);
        variant(unitChoice, toolUnit);
        capitalChoice.setOnAction(event -> toolSettlement.setSelected(true));

        message.setId(MESSAGE_ID);
        message.setWrapText(true);
        message.textProperty().bind(modelService.messageProperty());
        inspector.setId("editorInspector");

        mainPanel.getStyleClass().add("right");
        // Each tool has its own full-width row with its variant below, so no
        // label competes for width with a list.
        mainPanel.getChildren().addAll(nationLabel, nation, toolsLabel,
                toolSelect, toolTerrain, terrainChoice, toolForest, toolField,
                toolRiver, riverChoice, toolBonus, bonusChoice, toolSettlement,
                tribeChoice, capitalChoice, toolUnit, unitChoice, inspector,
                message);
        scroll.getStyleClass().add("screen-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("editor-right");
        scroll.setMinWidth(PANEL_WIDTH);
        scroll.setPrefWidth(PANEL_WIDTH);
        scroll.setMaxWidth(PANEL_WIDTH);
        updateLanguage();
        modelService.addListener(this::show);
        show();
    }

    private ToggleButton tool(final String id, final ButtonIcon icon) {
        final ToggleButton out = ActionButtons.toggle(icon);
        out.setId(id);
        out.setToggleGroup(tools);
        out.setMaxWidth(Double.MAX_VALUE);
        return out;
    }

    private static void variant(final ComboBox<?> combo,
            final ToggleButton tool) {
        combo.setOnShowing(event -> tool.setSelected(true));
        combo.setOnAction(event -> tool.setSelected(true));
    }

    private static MouseOperation painting(final MouseOperation operation) {
        return new MouseOperation() {
            @Override
            public void execute(final Location location) {
                operation.execute(location);
            }

            @Override
            public boolean isPainting() {
                return true;
            }
        };
    }

    private static HBox row(final Node... nodes) {
        final HBox out = new HBox(6, nodes);
        out.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        for (final Node node : nodes) {
            if (node instanceof Label) {
                HBox.setHgrow(node, javafx.scene.layout.Priority.ALWAYS);
            }
        }
        return out;
    }

    /**
     * Whole texts, never cut: labels wrap within the panel, buttons and check
     * boxes keep their full label width and lists fill the row.
     */
    private static void fit(final Node node) {
        if (node instanceof Label) {
            final Label label = (Label) node;
            label.setWrapText(true);
            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinHeight(Region.USE_PREF_SIZE);
        } else if (node instanceof javafx.scene.control.ButtonBase) {
            ((Region) node).setMinWidth(Region.USE_PREF_SIZE);
            ((Region) node).setMinHeight(Region.USE_PREF_SIZE);
        } else if (node instanceof ComboBox) {
            ((Region) node).setMaxWidth(Double.MAX_VALUE);
        }
        if (node instanceof javafx.scene.Parent
                && !(node instanceof javafx.scene.control.Control)) {
            ((javafx.scene.Parent) node).getChildrenUnmodifiable()
                    .forEach(EditorRightPanel::fit);
        }
    }

    private static <T> void named(final ComboBox<T> combo,
            final Function<T, String> name) {
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : name.apply(item));
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    /**
     * Rename the controls in the current language.
     */
    void updateLanguage() {
        nationLabel.setText(i18n.get(EditorText.nation));
        toolsLabel.setText(i18n.get(EditorText.tools));
        toolSelect.setText(i18n.get(EditorText.toolSelect));
        toolTerrain.setText(i18n.get(EditorText.toolTerrain));
        toolForest.setText(i18n.get(EditorText.toolForest));
        toolField.setText(i18n.get(EditorText.toolField));
        toolRiver.setText(i18n.get(EditorText.toolRiver));
        toolBonus.setText(i18n.get(EditorText.toolBonus));
        toolSettlement.setText(i18n.get(EditorText.toolSettlement));
        toolUnit.setText(i18n.get(EditorText.toolUnit));
        capitalChoice.setText(i18n.get(EditorText.capital));
        show();
    }

    /**
     * Show the nation and the selected square of the current draft.
     */
    private void show() {
        refresh();
        fit(mainPanel);
    }

    private void refresh() {
        final ScenarioDraft draft = modelService.getDraft();
        if (nation.getValue() != draft.getNation()) {
            nation.setValue(draft.getNation());
        }
        inspector.getChildren().clear();
        final Label tile = new Label();
        tile.setId(TILE_ID);
        tile.setWrapText(true);
        inspector.getChildren().add(tile);
        if (modelService.getSelected().isEmpty()) {
            tile.setText(i18n.get(EditorText.tileNone));
            return;
        }
        final Location at = modelService.getSelected().get();
        tile.setText(
                i18n.get(EditorText.tile, at.getX(), at.getY(), localization
                        .getTerrainName(modelService.getTerrainTypeAt(at))));
        if (modelService.hasTrees(at)) {
            attribute(i18n.get(EditorText.toolForest), "editorRemoveForest",
                    () -> actions.change(at, () -> draft.setForest(at, false)));
        }
        if (modelService.hasField(at)) {
            attribute(i18n.get(EditorText.toolField), "editorRemoveField",
                    () -> actions.change(at, () -> draft.setField(at, false)));
        }
        if (modelService.getRiverAt(at) != RiverSize.NONE) {
            attribute(
                    i18n.get(modelService.getRiverAt(at) == RiverSize.LARGE
                            ? EditorText.riverLarge
                            : EditorText.riverSmall),
                    "editorRemoveRiver", () -> actions.change(at,
                            () -> draft.setRiver(at, RiverSize.NONE)));
        }
        if (modelService.getResourceAt(at) != ResourceOverlay.NONE) {
            attribute(
                    localization.getResourceOverlayName(
                            modelService.getResourceAt(at)),
                    "editorRemoveBonus", () -> actions.change(at,
                            () -> draft.setResource(at, ResourceOverlay.NONE)));
        }
        draft.getSettlementAt(at)
                .ifPresent(settlement -> settlementForm(draft, at, settlement));
        final List<UnitPo> units = draft.getUnitsAt(at);
        if (!units.isEmpty()) {
            inspector.getChildren().add(new Label(i18n.get(EditorText.units)));
        }
        for (final UnitPo unit : units) {
            unitRow(at, unit);
            if (unit.getType().isShip()) {
                passengers(draft, at, unit);
            }
        }
    }

    private void attribute(final String name, final String removeId,
            final Runnable remove) {
        final Button button = ActionButtons.button(ButtonIcon.REMOVE,
                i18n.get(EditorText.remove));
        button.setId(removeId);
        button.setOnAction(event -> remove.run());
        final Label label = new Label(name);
        inspector.getChildren().add(row(label, button));
    }

    private void settlementForm(final ScenarioDraft draft, final Location at,
            final NativeSettlementPo settlement) {
        final ComboBox<NativeNation> tribe = new ComboBox<>(
                FXCollections.observableArrayList(NativeNation.values()));
        tribe.setId("editorSettlementTribe");
        named(tribe, NativeNation::getDisplayName);
        tribe.setValue(settlement.getNation());
        final CheckBox capital = new CheckBox(i18n.get(EditorText.capital));
        capital.setId("editorSettlementCapital");
        capital.setSelected(Boolean.TRUE.equals(settlement.getCapital()));
        final ComboBox<UnitType> skill = new ComboBox<>(FXCollections
                .observableArrayList(UnitType.NATIVE_TEACHABLE_SKILLS));
        skill.setId("editorSettlementSkill");
        named(skill, localization::getUnitName);
        if (settlement.getTeachableSkill() != null) {
            skill.setValue(UnitType.valueOf(settlement.getTeachableSkill()));
        }
        final Button apply = ActionButtons.button(ButtonIcon.CONFIRM,
                i18n.get(EditorText.apply));
        apply.setId("editorSettlementApply");
        apply.setOnAction(event -> actions.updateSettlement(at,
                tribe.getValue(), capital.isSelected(), skill.getValue()));
        final Button remove = ActionButtons.button(ButtonIcon.REMOVE,
                i18n.get(EditorText.remove));
        remove.setId("editorSettlementRemove");
        remove.setOnAction(
                event -> actions.change(at, () -> draft.removeSettlement(at)));
        inspector.getChildren().addAll(
                new Label(i18n.get(EditorText.settlement)),
                new Label(i18n.get(EditorText.tribe)), tribe, capital,
                new Label(i18n.get(EditorText.skill)), skill,
                row(apply, remove));
    }

    private void unitRow(final Location at, final UnitPo unit) {
        final Label label = new Label(messages.unit(unit));
        label.setId("editorUnit-" + unit.getId());
        final Button remove = ActionButtons.button(ButtonIcon.REMOVE,
                i18n.get(EditorText.remove));
        remove.setId("editorRemoveUnit-" + unit.getId());
        remove.setOnAction(event -> actions.removeUnit(at, unit.getId()));
        inspector.getChildren().add(row(label, remove));
    }

    private void passengers(final ScenarioDraft draft, final Location at,
            final UnitPo ship) {
        inspector.getChildren().add(new Label(i18n.get(EditorText.passengers,
                messages.unitType(ship.getType()), ship.getId())));
        for (final UnitPo passenger : draft.getPassengers(ship.getId())) {
            unitRow(at, passenger);
        }
        final ComboBox<UnitType> type = new ComboBox<>(
                FXCollections.observableArrayList(ScenarioDraft.EXPEDITION_TYPES
                        .stream().filter(UnitType::isStorable)
                        .collect(Collectors.toList())));
        type.setId("editorPassengerType-" + ship.getId());
        named(type, localization::getUnitName);
        type.setValue(UnitType.COLONIST);
        final Button add = ActionButtons.button(ButtonIcon.ADD,
                i18n.get(EditorText.addPassenger));
        add.setId("editorAddPassenger-" + ship.getId());
        add.setOnAction(event -> actions.addPassenger(at, ship.getId(),
                type.getValue()));
        inspector.getChildren().addAll(type, add);
    }

    @Override
    public Region getContent() {
        return scroll;
    }

}
